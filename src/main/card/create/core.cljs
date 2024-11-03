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
            [utility.events :as event]))



(defn add-system-functions [system]
  (-> system
      (sy/add-system-fn dr/on-drag-fn)
      (sy/add-system-fn ord/order-cards)
      (sy/add-system-fn sort/sort-rank)
      (sy/add-system-fn sel/add-remove-select-components)))

(defn add-tweens [system this]
  (let [tweens (-> this (.-tweens) (t/->TweensComponent))
        entity (e/create-entity)]
   (-> system
       (e/add-entity entity)
       (e/add-component entity tweens))))

(defn create-world [this deck position]
  (-> (e/create-system)
      (cr/create-deck this position deck 5)
      (add-tweens this)
      (add-system-functions)))

(defn to-def-position [[x y]]
  [(/ x 4) (/ y 1.6)])


(defn reset-message! []
  (let [msg events/card-message
        ee events/eventEmitter]
   (event/emit-event ee msg "reset")))

(defn handle-input-events [data state]
  (println "Data:" data)
  (reset-message!)
  (sort/set-sort-rank!))

(defn creat [this state deck]
  (let [pos (-> this
                (ut/get-canvas)
                (ut/canvas-to-size)
                (to-def-position))
        uim events/ui-message] 
    (-> (create-world this deck pos)
        (ct/update-scene-state! state))
    (letfn [(drag [p go x y] (dr/dragging! p go x y state))
            (dragend [p go x y] (dr/dragend! go state))]
      (ut/in-on-drag! this drag)
      (ut/in-on-dragend! this dragend) 
      (events/add-event-listener!
       uim #(handle-input-events % state)))))

