(ns card.create.card 
  (:require [brute.entity :as e]
            [utility.core :as ut]
            [card.types :as t]
            [clojure.core.reducers :as red]))


(defn calc-x-position [origin-x order sprite margin]
  (let [w (.-width sprite)
        mult (*  order (+ w margin))]
    (+ origin-x mult)))

(defn create-card [card this [x y] order system m]
  (let [card-entity (e/create-entity)
        suit (-> card (keys) (first))
        rank (-> card (vals) (first))
        margin 15 ; pixels for padding
        txt (str card)
        sprte (ut/add-draggable-sprite!
               this x y txt card-entity)
        pos-x (calc-x-position x order sprte margin)
        score (t/rank-to-default-score rank)
        sprite-comp (t/->SpriteComponent sprte)
        rank-comp (t/->RankComponent rank)
        suit-comp (t/->SuitComponent suit) 
        slot-comp (t/->SlotComponent order [pos-x y] m)
        score-comp (t/->ScoreComponent score)]
    (println "Slot comp is " slot-comp)
    (println "Position is " [pos-x y])
    (-> system
        (e/add-entity card-entity)
        (e/add-component card-entity sprite-comp)
        (e/add-component card-entity rank-comp)
        (e/add-component card-entity suit-comp)
        (e/add-component card-entity slot-comp)
        (e/add-component card-entity score-comp))))

(defn create-deck [system this pos deck m]
  (loop [s system
         d deck
         order 1]
    (if (ut/zero-coll? d)
      s
      (-> d
          (first)
          (create-card this pos order s m)
          (recur (next d) (+ order 1))))))

