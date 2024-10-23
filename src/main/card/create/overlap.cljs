(ns card.create.overlap 
  (:require
   [card.create.utility :as ct]
   [brute.entity :as e]
   [card.types :as t] 
   [utility.core :as ut]))


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

(defn add-swap-component [system entity next-order]
  ;(println "Adding swap for" entity next-order)
  (->>
   next-order
   (t/->SwapComponent)
   (e/add-component system entity)))

(defn add-swap-if-overlap [s e]
  (cond
    (overlap-left? s e) (add-swap-component s e :left)
    (overlap-right? s e) (add-swap-component s e :right)
    :else s))

(defn add-swap-on-overlap [system delta-time]
  (loop [s system
         ents (ct/get-all-sprite-entities system)]
    ;(println "From adding: Swaps in system" (ct/get-all-swap-entities s))
    (if (ut/zero-coll? ents)
      (do
        ;(println "Finished adding swaps")
        ;(println "Swaps in system" (ct/get-all-swap-entities s))
        s)
      (-> s
          (add-swap-if-overlap (first ents))
          (recur (next ents))))))