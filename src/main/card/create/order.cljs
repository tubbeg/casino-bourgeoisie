(ns card.create.order 
  (:require [utility.core :as ut]
            [brute.entity :as e]
            [card.types :as t]
            [card.create.utility :as ct]))

(defn overlap-left? [world entity]
  (let [slot (ct/get-slot-comp world entity)
        sprite (-> world (ct/get-sprite-comp entity) (:sprite))
        [origin-x _] (:pos slot)
        padding 5
        w (-> sprite (.-width) (/ 2))
        x (.-x sprite)] 
      (< x (- origin-x w))))

(defn overlap-right?  [world entity]
  (let [slot (ct/get-slot-comp world entity)
        sprite (-> world (ct/get-sprite-comp entity) (:sprite))
        [origin-x _] (:pos slot)
        padding 5
        w (-> sprite (.-width) (/ 2))
        x (.-x sprite)]
    (> x (+ origin-x w))))

(defn entity-has-matching-slot-nr [world entity nr]
  (->
   world
   (ct/get-slot-comp entity)
   (:order)
   (= nr)))

(defn find-entity-with-slot-order [world nr]
  (let [e (e/get-all-entities-with-component
           world t/SlotComponent)
        f (filter #(entity-has-matching-slot-nr world % nr) e)]
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

(defn set-xy-sprite-slot! [slot sprite-comp]
  (let [[x y] (:pos slot)
        sprite (:sprite sprite-comp)]
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

(defn overlap-proto [system entity overlap]
  (let [slot (ct/get-slot-comp system entity)
        next-slot (next-slot slot overlap)
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
            dragging (e/get-component s f t/DragComponent)
            sprite (-> (e/get-component s f t/SpriteComponent)
                       (:sprite))
            ;overlap (overlap-width-sprite2? sprite slot)
            ]
        (cond
          (nil? dragging) (do
                            (reset-entity-to-slot! s f)
                            (recur rem s))
          (overlap-right? s f)
          (let [nw (overlap-proto s f :right)]
            (reset-entity-to-slot! nw f)
            (recur rem nw))
          (overlap-left? s f)
          (let [nw (overlap-proto s f :left)]
            (reset-entity-to-slot! nw f)
            (recur rem nw))
          :else (recur rem s))))))