(ns card.create.order
  (:require [utility.core :as ut]
            [brute.entity :as e]
            [card.types :as t]
            [card.create.utility :as ct]
            [card.create.sort :as sort]))

(defn selected? [system entity]
  (-> (ct/get-sel-comp system entity)
      (nil?)
      (not)))

(defn overlap-left? [world entity]
  (let [slot (ct/get-slot-comp world entity)
        sprite (-> world (ct/get-sprite-comp entity) (:sprite))
        [origin-x _] (:pos slot)
        padding 5
        w (-> sprite (.-width))
        x (.-x sprite)]
    (< x (- origin-x w))))

(defn overlap-right?  [world entity]
  (let [slot (ct/get-slot-comp world entity)
        sprite (-> world (ct/get-sprite-comp entity) (:sprite))
        [origin-x _] (:pos slot)
        padding 5
        w (-> sprite (.-width))
        x (.-x sprite)]
    (> x (+ origin-x w))))

(defn entity-has-matching-slot-nr [world entity nr]
  (->
   world
   (ct/get-slot-comp entity)
   (:order)
   (= nr)))

(defn next-entity [nr world]
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

(defn not-dragging? [system entity]
  (-> system
      (ct/get-drag-comp entity)
      (nil?)))

(defn get-tweens [world]
  (let [ents (e/get-all-entities-with-component
              world t/TweensComponent)]
    (-> world
        (e/get-component (first ents) t/TweensComponent)
        (:tweens))))

(defn add-card-tween! [world entity [x y] duration]
  (let [sprite (-> world
                   (ct/get-sprite-comp entity)
                   (:sprite))
        tweens (get-tweens world)]
    (when (ut/not-nil? tweens)
      (ut/add-sprite-tween! tweens sprite x y duration))))

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
        sy (.-y s)]
    (-> (identical-position? [sx sy] pos) (not))))

(defn move? [world entity pos]
  (and (not-dragging? world entity)
       (entity-at-wrong-position? world entity pos)))

(defn calc-pos [world entity]
  (let [slot (ct/get-slot-comp world entity)
        [x y] (:pos slot)
        sw (-> world
               (ct/get-sprite-comp entity)
               (:sprite)
               (.-height)
               (/ 3))
        adj-y (if (selected? world entity) (- y sw) y)]
    [x adj-y]))

(defn move-card! [world entity]
  (let [pos (calc-pos world entity)
        [x y] pos
        s (ct/get-sprite-comp world entity)]
    (when (move? world entity pos)
      ;(ut/set-xy-object! (:sprite s) x y)
      (add-card-tween! world entity pos 100))))

(comment
  (defn move-card! [world entity]
    (let [pos (calc-pos world entity)]
      (when (move? world entity pos)
        (add-card-tween! world entity pos 100)))))
(defn swap-left [system entity]
  (-> system
      (next-slot-left entity)
      (next-entity system)
      (ct/swap-slots system entity)))

(defn swap-right [system entity]
  (-> system
      (next-slot-right entity)
      (next-entity system)
      (ct/swap-slots system entity)))

(defn swap-if-overlap [system entity]
  (cond
    (overlap-right? system entity) (swap-right system entity)
    (overlap-left? system entity) (swap-left system entity)
    :else system))


(def time-state (atom {:totaltime 0 :run false}))

(defn update-time-state! [dt limit]
;(println "Checknig")
  (if (> (:totaltime @time-state) limit)
    (swap! time-state #(assoc % :run true))
    (let [current (:totaltime @time-state)]
 ;     (println "Not running")
      (swap! time-state #(assoc % :totaltime (+ dt current))))))

(defn order-cards [system delta-time]
  (update-time-state! delta-time 500)
  (if (:run @time-state)
    (loop [s  system
           ents (ct/get-all-slot-entities system)]
      (if (ut/zero-coll? ents)
        s
        (let [f (first ents)
              rem (next ents)]
          (move-card! s f)
          (-> (swap-if-overlap s f)
              (recur rem)))))
    system))
