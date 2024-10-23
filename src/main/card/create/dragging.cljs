(ns card.create.dragging 
  (:require [utility.core :as ut]
            [card.create.utility :as ct]
            [brute.entity :as e]
            [card.types :as t]))

(defn on-drag-fn [system delta-time]
  (doseq [entity (ct/get-all-drag-entities system)]
    (let [sprite (e/get-component system entity t/SpriteComponent)
          drag (e/get-component system entity t/DragComponent)]
      (ut/set-x-object! (:sprite sprite) (:x drag))
      (ut/set-y-object! (:sprite sprite) (:y drag))))
  system)

(defn add-dragging-component [world name x y]
  (let [comps (e/get-component world name t/DragComponent)
        dragging (t/->DragComponent true x y)]
    (-> world
        (e/remove-component name comps)
        (e/add-component name dragging))))

(defn dragging! [go x y state]
  (let [name-option (.-name go)
        world (:world @state)]
    (when (and (ut/not-nil? world)
               (ut/not-nil? name-option))
      (let [nw (add-dragging-component world name-option x y)]
        (ct/update-scene-state! nw state)))))

(defn remove-dragging-comp [world name]
  (let [comps (e/get-component world name t/DragComponent)] 
    (e/remove-component world name comps)))

(defn dragend! [go state]
  (let [world (-> state (deref) (:world))
        entity (.-name go)]
   (when (and (ut/not-nil? world)
              (ut/not-nil? entity))
     (->  world
          (remove-dragging-comp entity)
          (ct/update-scene-state! state)))))
