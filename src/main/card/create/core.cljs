(ns card.create.core
  (:require [utility.core :as ut]
            [card.types :as t]
            [brute.entity :as e]
            [brute.system :as sy]
            [schema.core :as s]
            [card.default-deck.core :as default]
            [card.create.dragging :as dr]
            [card.create.card :as cr]
            [card.create.utility :as ct]
            [card.create.order :as ord]
            [card.create.select :as sel]))

(defn add-system-functions [system]
  (-> system
      (sy/add-system-fn dr/on-drag-fn)
      (sy/add-system-fn ord/order-cards)
      (sy/add-system-fn sel/add-remove-select-components)))

(defn add-object-factory [system this]
  (let [tweens (-> this (.-tweens) (t/->TweensComponent))
        entity (e/create-entity)]
   (-> system
       (e/add-entity entity)
       (e/add-component entity tweens))))

(defn create-world [this deck position]
  (-> (e/create-system)
      (cr/create-deck this position deck 5)
      (add-object-factory this)
      (add-system-functions)))

(defn to-def-position [[x y]]
  [(/ x 2.5) (/ y 1.5)])

(defn creat [this state deck]
  (let [pos (-> this
                (ut/get-canvas)
                (ut/canvas-to-size)
                (to-def-position))]
   (-> (create-world this deck pos)
       (ct/update-scene-state! state))
   (letfn [(drag [p go x y] (dr/dragging! p go x y state))
           (dragend [p go x y] (dr/dragend! go state))]
     (ut/in-on-drag! this drag)
     (ut/in-on-dragend! this dragend))))

