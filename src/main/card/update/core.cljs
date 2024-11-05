(ns card.update.core
  (:require [brute.system :as sy]
            [utility.core :as ut]))

(comment "time is often irrelevant for this program
    
    there aren't any physics systems, or many functions
    that depend on time
          
    although animation might be affected sometimes.
  ")


(defn update-game-state! [system state]
  (swap! state #(assoc % :world system)))

(defn update-scene [this time delta state]
  ;(->> this (ut/get-all-tweens-scene) (count) (println "Number of tweens: "))
  (let [system (:world @state)]
    (when (ut/not-nil? system)
      (-> system
          (sy/process-one-game-tick delta)
          (update-game-state! state)))))
