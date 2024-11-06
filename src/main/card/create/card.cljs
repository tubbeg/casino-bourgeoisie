(ns card.create.card 
  (:require [brute.entity :as e]
            [utility.core :as ut]
            [card.types :as t]
            [clojure.core.reducers :as red]
            [card.create.utility :as ct]
            [cljs.core :as c]
            [schema.core :as s]
            [utility.events :as events]))

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

(defn add-sprite! [this x y txt entity]
  (let [s (ut/add-draggable-sprite!
           this x y txt "entity" entity)]
    ;(ut/set-sprite-scale! s 2)
    s))

(defn rank-suit-comp-to-map [system entity]
  (let [r (ct/get-rank-comp system entity)
        s (ct/get-suit-comp system entity)]
    (->> {(:suit s) (:rank r)}
         (s/validate t/Card))))

(defn get-highest-order [system]
  (let [nr (ct/get-max-order system)]
    (if (nil? nr)
      0
      nr)))

(defn add-sprite-slot-to-entity [entry this hm system] 
  (let [[x y] (:origin hm)
        entity (:entity entry) 
        order (-> system (get-highest-order) (+ 1))
        margin 15 ; pixels for padding
        txt (-> system (rank-suit-comp-to-map entity) str) 
        [draw-x draw-y] (:draw hm)
        sprite (add-sprite! this draw-x draw-y txt entity)
        pos-x (ct/calc-x-position x order sprite margin)
        sprite-comp (t/->SpriteComponent sprite)
        slot-comp (t/->SlotComponent order [pos-x y])]
    (add-select! sprite)
    (-> system
        (e/add-component entity sprite-comp)
        (e/add-component entity slot-comp)
        (ct/remove-hidden-comp entity))))

(defn sort-ents [system t entities]
    (let [r #(-> system
                 (ct/get-rank-comp %)
                 :rank
                 t/rank-to-int)
          s #(-> system
                  (ct/get-suit-comp %)
                  :suit
                  t/suit-to-default-int)]
  (if (= t :rank) 
    (sort-by r entities)
    (sort-by s entities))))

(defn clip-zero [nr]
  (if (>= nr 0)
    nr
    0))

(defn get-nr-of-missing-cards [system max-hand]
  (->> system
       ct/get-all-sprite-entities
       count
       (- max-hand)
       clip-zero))

(defn get-drawable-entities [system t max-hand]
  (let [nr (get-nr-of-missing-cards system max-hand)]
    ;(println "Missing cards" nr)
    (->> system
         (ct/get-all-hidden-entities)
         (shuffle) ; had no idea this existed
         (take nr)
         (sort-ents system t)
         (into []))))

(defn emit-data! [draws-nr cards-in-deck]
  (let [r events/remaining-cards-in-deck]
   (when (> draws-nr 0)
     (events/emit-event! r cards-in-deck))))

(defn create-hand [system this max-hand sort-type hm]
  (let [ents (get-drawable-entities system sort-type max-hand)
        nr-of-draws (count ents)
        h (-> system ct/get-all-hidden-entities count)
        cards-in-deck (- h nr-of-draws)
        coll (for [i (range nr-of-draws)]
               {:entity (nth ents i)})]
    (emit-data! nr-of-draws cards-in-deck)
    (-> #(add-sprite-slot-to-entity %2 this hm %1)
        (reduce system coll))))


