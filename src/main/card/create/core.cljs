(ns card.create.core
  (:require [utility.core :as ut]
            [utility.events :as events]
            [card.types :as t]
            [brute.entity :as e]
            [brute.system :as sy]
            [schema.core :as s]
            [card.default-deck.core :as default]
            [card.create.dragging :as dr]
            [card.create.card :as cr]
            [card.create.utility :as ct]
            [card.create.order :as ord]
            [card.create.select :as sel]
            [card.create.sort :as sort]
            [card.create.discard :as discard]
            [card.create.push :as push]
            [card.create.score :as score]
            [card.create.draw :as draw]))

(defn add-system-functions
  [system m max-hand this]
  (-> system
      (sy/add-system-fn dr/on-drag-fn)
      (sy/add-system-fn (draw/draw-cards max-hand this m))
      (sy/add-system-fn (score/score-cards m))
      (sy/add-system-fn dr/add-comp-if-dragging)
      (sy/add-system-fn dr/remove-comp-if-dragend)
      (sy/add-system-fn (ord/order-cards max))
      (sy/add-system-fn sort/sort-deck)
      (sy/add-system-fn discard/remove-discards) 
      (sy/add-system-fn (discard/move-discards m))
      (sy/add-system-fn push/push-cards)
      (sy/add-system-fn (push/move-push m))
      (sy/add-system-fn sel/add-remove-select-components)))

(defn add-tweens [system this]
  (let [tweens (-> this (.-tweens) (t/->TweensComponent))
        entity (e/create-entity)]
   (-> system
       (e/add-entity entity)
       (e/add-component entity tweens))))

(defn create-world [this m deck max]
  (-> (e/create-system)
      (draw/create-deck deck)
      (add-tweens this)
      (add-system-functions m max this)))

(defn reset-message! []
  (let [msg events/card-message
        ee events/eventEmitter]
   (events/emit-event ee msg "reset")))

(defn err-msg []
  (println "NOT IMPLEMENTED"))

(defn handle-input-events [data]
  ;(println "Data:" data)
  (reset-message!) 
  (cond
    (:rank data) (sort/set-sort-rank!)
    (:suit data) (sort/set-sort-suit!)
    (:discard data) (discard/set-discard-state!)
    (:push data) (push/set-push-state!)
    :else nil))

(defn creat [this state deck max]
  (let [canv-pos (-> this
                     (ut/get-canvas)
                     (ut/canvas-to-size))
        def-pos (-> canv-pos
                    (ct/to-def-position))
        def-push (-> canv-pos
                     (ct/to-def-push-position))
        draw-pos (-> canv-pos
                     (ct/to-def-draw-position))
        m {:draw draw-pos
           :push def-push
           :origin def-pos
           :canvas canv-pos}
        uim events/ui-message] 
    (-> (create-world this m deck max)
        (ct/update-scene-state! state))
    (letfn [(drag [p go x y] (dr/dragging! p go x y))
            (dragend [p go x y] (dr/dragend! go))]
      (ut/in-on-drag! this drag)
      (ut/in-on-dragend! this dragend) 
      (events/add-event-listener!
       uim #(handle-input-events %)))))

