(ns card.create.dragging 
  (:require [utility.core :as ut]
            [card.create.utility :as ct]
            [brute.entity :as e]
            [card.types :as t]))

(def dragging-state (atom {:entity nil
                           :x nil
                           :y nil}))

(defn reset-dragging-state! []
  (swap! dragging-state #(assoc % :entity nil))
  (swap! dragging-state #(assoc % :x nil))
  (swap! dragging-state #(assoc % :y nil)))

(defn set-dragging-state [e x y]
  (swap! dragging-state #(assoc % :entity e))
  (swap! dragging-state #(assoc % :x x))
  (swap! dragging-state #(assoc % :y y)))

(def drag-end-state (atom {:entity nil}))

(defn reset-dragend-state! []
  (swap! drag-end-state #(assoc % :entity nil)))

(defn set-dragend-state! [e]
  (swap! drag-end-state #(assoc % :entity e)))

(defn cant-drag? []
  (let [e (:entity @dragging-state)
        x (:x @dragging-state)
        y (:y @dragging-state)]
    (or (nil? e) (nil? x) (nil? y))))

(defn drag-end? []
  (let [e (:entity @drag-end-state)]
     (-> e nil? not)))

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

(defn add-comp-if-dragging [system delta-time]
  (if
   (cant-drag?)
    system
    (let [s @dragging-state
          e (:entity s)
          x (:x s)
          y (:y s)]
      (if (ct/draggable? system e)
        (add-dragging-component system e x y)
        system))))

(defn remove-dragging-comp [world name]
  (let [comps (e/get-component world name t/DragComponent)]
    (e/remove-component world name comps)))

(defn remove-comp-if-dragend [system delta-time]
  (if (drag-end?)
    (let [s @drag-end-state
          e (:entity s)
          s (remove-dragging-comp system e)] 
      (reset-dragend-state!)
      (reset-dragging-state!)
      s)
    system))

(defn drag-duration? [ptr] 
  (ut/ptr-duration-greater? ptr 30))

(defn dragging! [ptr go x y]
  (let [e (get-go-entity go)]
    (when (and (ut/not-nil? e)
               (drag-duration? ptr))
      (set-dragging-state e x y))))

(defn dragend! [go]
  (let [entity (get-go-entity go)]
   (when (ut/not-nil? entity) 
     (set-dragend-state! entity))))
