(ns card.update.core
  (:require [brute.system :as sy]
            [utility.core :as ut]))

(comment "time doesn't really matter here,
 no physics or time dependent functions to
 speak of")


(defn update-game-state! [system state]
  (swap! state #(assoc % :world system)))

(defn update-scene [this time delta state]
  (->> this (ut/get-all-tweens-scene) (count) (println "Number of tweens: "))
  (let [system (:world @state)]
    (when (ut/not-nil? system)
      (-> system
          (sy/process-one-game-tick delta)
          (update-game-state! state)))))
