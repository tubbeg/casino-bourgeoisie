(ns card.update.core
  (:require [brute.system :as sy]
            [utility.core :as ut]))

(comment "time doesn't really matter here,
 no physics or time dependent functions to
 speak of")

(defn update-scene [this time delta state]
  (let [system (:world @state)]
    (when (ut/not-nil? system)
      (sy/process-one-game-tick system delta))))