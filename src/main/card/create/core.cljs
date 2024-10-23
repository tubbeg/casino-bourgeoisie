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
            [card.create.order :as ord]))


(defn add-system-functions [system]
  (-> system
      (sy/add-system-fn dr/on-drag-fn)
      (sy/add-system-fn ord/order-cards)))

(defn create-world [this deck]
  (-> (e/create-system)
      (cr/create-deck this [400 400] deck 5)
      (add-system-functions)))

(defn creat [this state deck]
  (-> (create-world this deck)
      (ct/update-scene-state! state))
  (letfn [(drag [p go x y] (dr/dragging! go x y state))
          (dragend [p go x y] (dr/dragend! go state))]
    (ut/in-on-drag! this drag)
    (ut/in-on-dragend! this dragend)))

