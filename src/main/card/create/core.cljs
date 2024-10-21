(ns card.create.core
  (:require [utility.core :as ut]
            [card.types :as t]
            [brute.entity :as e]
            [brute.system :as sy]
            [schema.core :as s]
            [card.default-deck.core :as default]))

(defn create-card [ card this system]
  (let [card-entity (e/create-entity)
        s (-> card (keys) (first))
        r (-> card (vals) (first))
        order (t/rank-to-int r)
        score (t/rank-to-default-score r)
        texture (t/->TextureComponent (str card))
        rank-comp (t/->RankComponent r)
        suit-comp (t/->SuitComponent s)
        order-comp (t/->OrderComponent order)
        score-comp (t/->ScoreComponent score)]
    (ut/add-draggable-sprite! this 400 400 (str card) card-entity)
    (-> system
        (e/add-entity card-entity)
        (e/add-component card-entity texture)
        (e/add-component card-entity rank-comp)
        (e/add-component card-entity suit-comp)
        (e/add-component card-entity order-comp)
        (e/add-component card-entity score-comp))))

(defn create-deck [system this deck]
  (loop [s system
         d deck]
    (if (ut/zero-coll? d)
      s
      (-> d
          (first)
          (create-card this s)
          (recur (next d))))))

(defn add-system-functions [system]
  system)

(defn create-world [this deck]
  (-> (e/create-system)
      (create-deck this deck)
      (add-system-functions)))

(defn update-scene-state! [new-state old-state]
  (swap! old-state #(assoc % :world new-state)))
(comment
(defn create-sprites [this world]
  (for [entity (e/get-all-entities-with-component
                world t/TextureComponent)]
    (let [texture (-> world
                      (e/get-component
                       entity t/TextureComponent)
                      (:texture))] 
      (ut/add-draggable-sprite! this 400 400 texture entity)))))

(defn creat [this state deck]
  (let [world (create-world this deck)
        ;sprites (create-sprites this world)
        ] 
    ;(println "Sprite count" (count sprites))
    (update-scene-state! world state)))

