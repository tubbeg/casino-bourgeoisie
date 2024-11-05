(ns card.create.order
  (:require [utility.core :as ut]
            [brute.entity :as e]
            [card.types :as t]
            [card.create.utility :as ct]))

(def tween-duration 200) ; ms

(defn selected? [system entity]
  (-> (ct/get-sel-comp system entity)
      (nil?)
      (not)))

; I should be using Phasers own overlap function here
; rather than reinventing the wheel
(defn overlap-left? [world entity]
  (let [slot (ct/get-slot-comp world entity)
        sprite (-> world (ct/get-sprite-comp entity) (:sprite))
        [origin-x _] (:pos slot)
        order (:order slot)
        ;padding 5
        w (-> sprite (.-width))
        x (.-x sprite)]
    (and
     (not= order 1)
     (< x (- origin-x w)))))

(defn get-max-order [system]
  (let [f #(->> % (ct/get-slot-comp system) :order)]
    (->>
     (ct/get-all-slot-entities system)
     (sort-by f) 
     (last)
     (ct/get-slot-comp system)
     :order)))

(defn overlap-right?  [world entity]
  (let [slot (ct/get-slot-comp world entity)
        max-order (get-max-order world)
        sprite (-> world (ct/get-sprite-comp entity) (:sprite))
        [origin-x _] (:pos slot)
        order (:order slot)
        ;padding 5
        w (-> sprite (.-width))
        x (.-x sprite)]
    (and
     (not= max-order order)
     (> x (+ origin-x w)))))

(defn entity-has-matching-slot-nr [world entity nr]
  (->
   world
   (ct/get-slot-comp entity)
   :order
   (= nr)))

(defn next-entity [nr world default]
  (let [e (e/get-all-entities-with-component
           world t/SlotComponent)
        f (filter #(entity-has-matching-slot-nr world % nr) e)
        f1 (first f)]
    (when (nil? f1)
      (println "NIL ENTITY next-entity function")
      (println "Tried to find order nr" nr))
    f1))

(defn clip-one [n]
  (if (< n 1) 1 n))

(defn clip-max [n max]
  (if (> n max) max n))

(defn next-slot-right [world entity max]
  (let [slot (ct/get-slot-comp world entity)]
    (-> slot (:order) (+ 1) (clip-max max))))

(defn next-slot-left [world entity]
  (let [slot (ct/get-slot-comp world entity)]
    (-> slot (:order) (- 1) (clip-one))))

(defn not-dragging? [system entity]
  (-> system
      (ct/get-drag-comp entity)
      (nil?)))

(defn identical-position? [pos1 pos2]
  (let [[x1 y1] pos1
        [x2 y2] pos2]
    (and (= x1 x2)
         (= y1 y2))))

(defn entity-at-wrong-position? [world entity pos]
  (let [s (-> world
              (ct/get-sprite-comp entity)
              (:sprite))
        sx (.-x s)
        sy (.-y s)
        result (-> (identical-position? [sx sy] pos)
                   (not))]
    result))

(defn sprite-has-no-tween? [tween-manager sprite]
  (-> tween-manager (ut/sprite-has-tween? sprite) not))

 ;(defn no-tweens-active? [tweens-manager]
 ;  (-> tweens-manager (ut/get-all-tweens-tm) (count) (= 0)))

(defn move? [world entity pos]
  (let [tweens (ct/get-tweens world)
        s (-> world (ct/get-sprite-comp entity) :sprite)]
    (and
     (ut/not-nil? pos)
     (ut/not-nil? s)
     (not-dragging? world entity)
     (entity-at-wrong-position? world entity pos)
     (sprite-has-no-tween? tweens s))))

(defn get-height-div [world entity]
  (let [s (-> world (ct/get-sprite-comp entity))]
    (if (nil? s)
      nil
      (-> s :sprite (.-height) (/ 3)))))

(defn calc-pos [world entity]
  (let [slot (ct/get-slot-comp world entity)
        [x y] (:pos slot)
        sw (get-height-div world entity)
        adj-y (if (selected? world entity) (- y sw) y)
        pos [x adj-y]]
    (if (nil? sw)
      nil
      pos)))

(defn move-card! [world entity]
  (let [pos (calc-pos world entity)]
    (when (move? world entity pos)
      (ct/add-card-tween! world entity pos tween-duration))))

(defn swap-left [system entity]
  (-> system
      (next-slot-left entity)
      (next-entity system :left)
      (ct/swap-slots system entity)))

(defn swap-right [system entity max]
  (-> system
      (next-slot-right entity max)
      (next-entity system :right)
      (ct/swap-slots system entity)))

(defn swap-if-overlap [system entity max]
  (let [tweens (ct/get-tweens system)
        s (-> system (ct/get-sprite-comp entity) :sprite)]
    (cond
      (ut/sprite-has-tween? tweens s)  system ; very important
      (overlap-right? system entity) (swap-right system entity max)
      (overlap-left? system entity) (swap-left system entity)
      :else system)))

(defn move-card-check-overlap [system entity m]
  (move-card! system entity)
  (swap-if-overlap system entity m))

(defn order-cards [max]
  (fn [system delta-time] 
    (->>
     system
     (ct/get-all-entities-with-sprites-and-slots)
     (reduce #(move-card-check-overlap %1 %2 max) system))))