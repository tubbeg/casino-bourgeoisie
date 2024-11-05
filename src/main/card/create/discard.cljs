(ns card.create.discard 
  (:require
    [card.create.utility :as ct]
    [utility.core :as ut]
    [brute.entity :as e]
    [card.types :as t]))

(def tween-duration 100) ;ms

(def discard-input-state (atom {:discard false}))
(def time-state (atom {:time 0}))

(defn set-time-state! [nr]
  (swap! time-state #(assoc % :time nr)))

(defn reset-time-state! []
  (swap! time-state #(assoc % :time 0)))

(defn discard-time? [limit]
  (> (:time @time-state) limit))

(defn set-discard-state! []
  (swap! discard-input-state #(assoc % :discard true)))

(defn reset-discard-state! []
  (swap! discard-input-state #(assoc % :discard false)))

(defn discard? []
  (:discard @discard-input-state))

(defn move-card-outside! [system entity pos]
  (ct/add-card-tween! system entity pos tween-duration))


(defn remove-and-add-discard [system entity pos]
  (move-card-outside! system entity pos) 
  (-> system
      (ct/remove-slot-comp entity)
      (ct/remove-sel-comp entity)
      (e/add-component entity (t/->DiscardComponent))))

(defn set-discard [system pos]
  (reset-discard-state!)
  (->> (ct/get-all-sel-entities system)
       (reduce #(remove-and-add-discard %1 %2 pos) system)))

(defn remove-card [system entity]
  (-> system
      (ct/get-sprite-comp entity)
      :sprite
      ut/destroy-sprite!)
  (-> system
      (ct/remove-sprite-comp entity)
      (e/kill-entity entity)))

(defn remove-discarded-cards [system]
  (->> (ct/get-all-discard-entities system)
       (reduce #(remove-card %1 %2) system)))

(defn remove-discards [system delta-time]
  (let [next-time (+ (:time @time-state) delta-time)]
    (set-time-state! next-time)
    (if (discard-time? 300)
      (do
        (reset-time-state!) 
        (remove-discarded-cards system))
      system)))

(defn move-discards [pos def-pos]
  (fn [system delta-time]
   (if (discard?)
     (-> system
         (set-discard pos)
         (ct/update-slots def-pos))
     system)))


