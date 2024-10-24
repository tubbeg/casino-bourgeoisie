(ns card.create.utility 
  (:require
    [brute.entity :as e]
    [card.types :as t]))

(defn update-scene-state! [new-state old-state]
  (swap! old-state #(assoc % :world new-state)))

(defn get-sel-comp [world n]
  (e/get-component world n t/SelectComponent))
(defn get-slot-comp [world n]
  (e/get-component world n t/SlotComponent))
(defn get-drag-comp [world n]
  (e/get-component world n t/DragComponent))
(defn get-sprite-comp [world n]
  (e/get-component world n t/SpriteComponent))
(defn get-all-drag-entities [world]
  (e/get-all-entities-with-component 
   world t/DragComponent))
(defn get-all-slot-entities [world] 
  (e/get-all-entities-with-component
   world t/SlotComponent))
(defn get-all-sel-entities [world]
  (e/get-all-entities-with-component
   world t/SelectComponent))
(defn get-all-sprite-entities [world]
  (e/get-all-entities-with-component
   world t/SpriteComponent))
(defn remove-drag-comp [world name]
  (let [comps (get-drag-comp world name)]
    (e/remove-component world name comps)))