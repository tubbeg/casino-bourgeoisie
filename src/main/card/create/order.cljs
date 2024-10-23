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
        w (-> sprite (.-width) )
        x (.-x sprite)] 
      (< x (- origin-x w))))

(defn overlap-right?  [world entity]
  (let [slot (ct/get-slot-comp world entity)
        sprite (-> world (ct/get-sprite-comp entity) (:sprite))
        [origin-x _] (:pos slot)
        padding 5
        w (-> sprite (.-width) )
        x (.-x sprite)]
    (> x (+ origin-x w))))

(defn entity-has-matching-slot-nr [world entity nr]
  (->
   world
   (ct/get-slot-comp entity)
   (:order)
   (= nr)))

(defn next-entity [ nr world]
  (let [e (e/get-all-entities-with-component
           world t/SlotComponent)
        f (filter #(entity-has-matching-slot-nr world % nr) e)]
    (first f)))

(defn clip-one [n]
  (if (< n 1) 1 n))

(defn clip-max [n max]
  (if (> n max) max n))

(defn next-slot-right [world entity]
  (let [slot (ct/get-slot-comp world entity)
        max (:max slot)] 
    (-> slot (:order) (+ 1) (clip-max max))))

(defn next-slot-left [world entity]
  (let [slot (ct/get-slot-comp world entity)]
    (-> slot (:order) (- 1) (clip-one))))

(defn reset-entity-to-slot! [world entity]
  (let [slot (ct/get-slot-comp world entity)
        [x y] (:pos slot)
        sprite (-> world
                   (ct/get-sprite-comp entity)
                   (:sprite))]
    (ut/set-x-object! sprite x)
    (ut/set-y-object! sprite y)))

(defn swap-slots [ entity1 system entity2]
  (let [s1 (ct/get-slot-comp system entity1)
        s2 (ct/get-slot-comp system entity2)]
    (-> system
        (e/remove-component entity1 s1)
        (e/remove-component entity2 s2)
        (e/add-component entity1 s2)
        (e/add-component entity2 s1))))

(defn not-dragging? [system entity]
  (-> system
      (ct/get-drag-comp entity)
      (nil?)))

(defn swap-left [system entity]
  (-> system
      (next-slot-left entity)
      (next-entity system)
      (swap-slots system entity)))

(defn swap-right [system entity]
  (-> system
      (next-slot-right entity)
      (next-entity system)
      (swap-slots system entity)))

(defn swap-if-overlap [system entity]
  (cond
    (overlap-right? system entity) (swap-right system entity)
    (overlap-left? system entity) (swap-left system entity)
    :else system))

(defn order-cards [system delta-time]
  (loop [s system
         ents (ct/get-all-slot-entities system)]
    (if (ut/zero-coll? ents)
      s
      (let [f (first ents)
            rem (next ents)]
        (when (not-dragging? s f)
          (reset-entity-to-slot! s f))
        (-> (swap-if-overlap s f)
            (recur rem))))))
