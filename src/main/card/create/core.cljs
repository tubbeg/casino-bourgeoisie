(ns card.create.core
  (:require [utility.core :as ut]
            [card.types :as t]
            [brute.entity :as e]
            [brute.system :as sy]
            [schema.core :as s]
            [card.default-deck.core :as default]))



(defn update-scene-state! [new-state old-state]
  (swap! old-state #(assoc % :world new-state)))


(defn create-card [ card this system]
  (let [card-entity (e/create-entity)
        suit (-> card (keys) (first))
        rank (-> card (vals) (first))
        txt (str card)
        sprte (ut/add-draggable-sprite!
               this 400 400 txt card-entity)
        order (t/rank-to-int rank)
        score (t/rank-to-default-score rank)
        sprite-comp (t/->SpriteComponent sprte)
        rank-comp (t/->RankComponent rank)
        suit-comp (t/->SuitComponent suit)
        order-comp (t/->OrderComponent order)
        score-comp (t/->ScoreComponent score)]
    (-> system
        (e/add-entity card-entity)
        (e/add-component card-entity sprite-comp)
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


(defn on-drag-fn [system delta-time]
  (doseq [entity (e/get-all-entities-with-component
                  system t/DragComponent)]
    (let [sprite (e/get-component system entity t/SpriteComponent)
          drag (e/get-component system entity t/DragComponent)]
      (ut/set-x-object! (:sprite sprite) (:x drag))
      (ut/set-y-object! (:sprite sprite) (:y drag)))))

(defn add-system-functions [system]
  (-> system
      (sy/add-system-fn on-drag-fn)))

(defn create-world [this deck]
  (-> (e/create-system)
      (create-deck this deck)
      (add-system-functions)))

(defn add-dragging-component [world name x y]
  (let [comps (e/get-component world name t/DragComponent)
        dragging (t/->DragComponent true x y)] 
    (-> world
        (e/remove-component name comps)
        (e/add-component name dragging))))

(defn remove-dragging-comp [world name x y]
  (let [compsbefore (e/get-all-components-on-entity world name)
        comps (e/get-component world name t/DragComponent)
        w (-> world
              (e/remove-component name comps))
        comps2 (e/get-all-components-on-entity w name)]
    ;(println "Entity has following components: " compsbefore)
    ;(println "Removed dragging. Entity has following components" comps2)
    w))

(defn dragging! [go x y state]
  (let [name-option (.-name go)
        world (:world @state)]
     (when (and (ut/not-nil? world)
                (ut/not-nil? name-option))
       (let [nw (add-dragging-component world name-option x y)]
         (update-scene-state! nw state)))))

(defn dragend! [go x y state]
  (let [name-option (.-name go)
        world (:world @state)]
    (when (and (ut/not-nil? world)
               (ut/not-nil? name-option))
      (let [nw (remove-dragging-comp world name-option x y)]
        (update-scene-state! nw state)))))

(defn creat [this state deck]
  (-> (create-world this deck)
      (update-scene-state! state))
  (letfn [(drag [p go x y] (dragging! go x y state))
          (dragend [p go x y] (dragend! go x y state))]
    (ut/in-on-drag! this drag)
    (ut/in-on-dragend! this dragend)))

