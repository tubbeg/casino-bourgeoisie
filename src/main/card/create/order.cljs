(ns card.create.order 
  (:require [utility.core :as ut]
            [brute.entity :as e]
            [card.types :as t]
            [card.create.utility :as ct]))

(defn overlap-left? [sprite slot]
  (let [[origin-x _] (:pos slot)
        padding 5
        w (-> sprite (.-width) (/ 2))
        x (.-x sprite)] 
      (< x (- origin-x w))))

(defn overlap-right? [sprite slot]
  (let [[origin-x _] (:pos slot)
        padding 5
        w (-> sprite (.-width) (/ 2))
        x (.-x sprite)]
    (> x (+ origin-x w))))

(defn entity-has-matching-slot-order [world entity slot1]
  (let [slot2 (e/get-component world entity t/SlotComponent)]
   (= (:order slot1) (:order slot2))))

(defn find-entity-with-slot-order [world slot]
  (let [e (e/get-all-entities-with-component
           world t/SlotComponent)
        f (filter #(entity-has-matching-slot-order world % slot) e)]
    (first f)))

(defn clip-one [n]
  (if (< n 1) 1 n))

(defn clip-max [n max]
  (if (> n max) max n))

(defn next-slot [slot next]
  (let [max (:max slot)
        left (-> slot (:order) (- 1) )
        right (-> slot (:order) (+ 1))
        else (:order slot)]
    (case next
      :left left
      :right right
      else)))

(defn overlap? [overlap]
  (or (= overlap :left) (= overlap :right)))

(defn set-xy-sprite-slot! [slot sprite]
  (let [[x y] (:pos slot)]
    (ut/set-x-object! sprite x)
    (ut/set-y-object! sprite y)))

(defn reset-entity-to-slot! [world entity]
  (let [slot (ct/get-slot-comp world entity)
        sprite (ct/get-sprite-comp world entity)]
    (set-xy-sprite-slot! slot sprite)))

(defn swap-slots [system entity1 entity2]
  (let [s1 (ct/get-slot-comp system entity1)
        s2 (ct/get-slot-comp system entity2)]
    (println "Swapping" entity1 entity2)
    (-> system
        (e/remove-component entity1 s1)
        (e/remove-component entity2 s2)
        (e/add-component entity1 s2)
        (e/add-component entity2 s1))))

(defn overlap-proto [system entity slot overlap]
  (let [next-slot {:order (next-slot slot overlap)}
        next-ent (find-entity-with-slot-order system next-slot)]
    (println next-slot "NEXT")
    (swap-slots system entity next-ent)))

(defn order-cards [system delta-time]
  (loop [ents (e/get-all-entities-with-component
               system t/SlotComponent)
         s system]
    (if (ut/zero-coll? ents)
      s
      (let [f (first ents)
            rem (next ents)
            slot (e/get-component s f t/SlotComponent)
            dragging (e/get-component s f t/DragComponent)
            sprite (-> (e/get-component s f t/SpriteComponent)
                       (:sprite))
            ;overlap (overlap-width-sprite2? sprite slot)
            ]
        (cond
          (nil? dragging) (do
                            (set-xy-sprite-slot! slot sprite)
                            (recur rem s))
          (overlap-right? sprite slot)
          (let [nw (overlap-proto s f slot :right)]
            ;(reset-entity-to-slot! nw f)
            (recur rem nw))
          (overlap-left? sprite slot)
          (let [nw (overlap-proto s f slot :left)]
            ;(reset-entity-to-slot! nw f)
            (recur rem nw))
          :else (recur rem s))))))