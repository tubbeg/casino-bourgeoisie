(ns card.create.dragging 
  (:require [utility.core :as ut]
            [card.create.utility :as ct]
            [brute.entity :as e]
            [card.types :as t]))

(defn get-go-entity [go]
   (ut/get-key-value-gameobject! go "entity"))

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

(defn drag-duration? [ptr] 
  (ut/ptr-duration-greater? ptr 30))

(defn dragging! [ptr go x y state]
  (let [name-option (get-go-entity go)
        world (:world @state)]
    (when (and (ut/not-nil? world)
               (ut/not-nil? name-option)
               (drag-duration? ptr))
      (->
       world
       (add-dragging-component name-option x y)
       (ct/update-scene-state! state)))))

(defn remove-dragging-comp [world name]
  (let [comps (e/get-component world name t/DragComponent)] 
    (e/remove-component world name comps)))

(defn dragend! [go state]
  (let [world (-> state (deref) (:world))
        entity (get-go-entity go)]
   (when (and (ut/not-nil? world)
              (ut/not-nil? entity))
     (->  world
          (remove-dragging-comp entity)
          (ct/update-scene-state! state)))))
