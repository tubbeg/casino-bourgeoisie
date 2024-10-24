(ns card.create.card 
  (:require [brute.entity :as e]
            [utility.core :as ut]
            [card.types :as t]
            [clojure.core.reducers :as red]))

(defn calc-x-position [origin-x order sprite margin]
  (let [w (.-width sprite)
        mult (*  order (+ w margin))]
    (+ origin-x mult)))

(defn switch-down-ptr [ptr this]
  (->> ptr
       (ut/ptr-duration)
       (ut/set-key-value-gameobject! this "duration")))

(defn switch-up-ptr [ptr this]
  (let [ot (ut/get-key-value-gameobject! this "duration")
        ct (ut/ptr-duration ptr)
        delta (- ct ot)
        set? (< delta 150)]
    (when set?
      (ut/switch-selected! this))))

(defn add-select! [sprite] 
    (ut/reset-selected! sprite)
    (ut/gameobject-on-pointerdown sprite switch-down-ptr)
    (ut/gameobject-on-pointerup sprite switch-up-ptr))

(defn create-card [card this [x y] order system m]
  (let [card-entity (e/create-entity)
        suit (-> card (keys) (first))
        rank (-> card (vals) (first))
        margin 15 ; pixels for padding
        txt (str card)
        sprte (ut/add-draggable-sprite!
               this x y txt "entity" card-entity)
        pos-x (calc-x-position x order sprte margin)
        score (t/rank-to-default-score rank)
        sprite-comp (t/->SpriteComponent sprte)
        rank-comp (t/->RankComponent rank)
        suit-comp (t/->SuitComponent suit)
        slot-comp (t/->SlotComponent order [pos-x y] m)
        score-comp (t/->ScoreComponent score)]
    (add-select! sprte)
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

