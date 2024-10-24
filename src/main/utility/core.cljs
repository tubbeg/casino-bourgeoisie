(ns utility.core
  (:require
   [clojure.core.async :as a]
   [cljs.core.async.interop :refer-macros [<p!]]))

; ! indicates side effects, e.g. mutable state

(defn not-nil? [n]
  (-> n (nil?) (not)))

(defn set-base-url! [this url]
  (.. this -load (setBaseURL url)))

(defn load-image! [this id url]
  (.. this -load (image id url)))

(defn add-image [this x y id]
  (.. this -add (image x y id)))

(defn add-sprite [this x y id]
  (.. this -add (sprite x y id)))

(defn add-container
  [^js this x y]
  (.. this -add (container x y)))

(defn add-to-container! [container object]
  (. container (add object)))

; you can actually add these as a standard js array
; but it's not working for some reason
(defn add-items-to-container! [container coll]
  (doseq [i (-> coll (count) (range))]
    (add-to-container! container (nth coll i))))

(defn set-draggable! [this object state]
  (.. this -input (setDraggable object state)))

(defn set-interactive! [object]
  (. object (setInteractive)))

(defn set-sprite-name! [sprite name]
  (set! (.-name sprite) name))

(defn set-key-value-gameobject! [go key value]
  (. go (setData key value)))

(defn get-key-value-gameobject! [go key]
  (. go (getData key)))

(defn add-draggable-sprite!
  ([this x y texture key value]
   (let [s (-> this
               (add-sprite x y texture)
               (set-interactive!))]
     (set-draggable! this s true)
     (set-key-value-gameobject! s key value)
     s))
  ([this x y texture]
   (let [s (-> this
               (add-sprite x y texture)
               (set-interactive!))]
     (set-draggable! this s true)
     s)))

(defn add-tweens [this config]
  (.. this -tweens (add config)))

(defn add-draggable-sprites!
  ([coll x y this identifiers]
   (->>
    (for [i (-> coll (count) (range))]
      (add-draggable-sprite! this x y (nth coll i)))
    (into [])))
  ([coll x y this]
   (->>
    (for [i (-> coll (count) (range))]
      (add-draggable-sprite! this x y (nth coll i)))
    (into []))))

(defn print-js-object-properties [object]
  (-> object (js-keys) (println)))

(defn input-on [this event function]
  (.. this -input (on event function)))

(defn in-on-dragend! [this function]
  (input-on this "dragend" function))

(defn in-on-drag! [this function]
  (input-on this "drag" function))

(defn gameobject-on-pointerdown 
  "Function needs to take 2 args:
   fn [ptr this] (logic here)
   ptr : Input mouse pointer
   this : the instance reference"
  [go function]
  (. go (on "pointerdown"
            #(this-as this (function % this)))))

(defn gameobject-on-pointerup
  "Function needs to take 2 args:
   fn [ptr this] (logic here)
   ptr : Input mouse pointer
   this : the instance reference"
  [go function]
  (. go (on "pointerup"
            #(this-as this (function % this)))))

(defn set-x-object! [obj x ]
  (set! (.-x obj) x))

(defn set-y-object! [obj y]
  (set! (.-y obj) y))

(defn set-xy-object! [obj x y]
  (set-x-object! obj x)
  (set-y-object! obj y))

(defn zero-coll? [coll]
  (-> coll (count) (zero?)))

(defn set-selected! [go]
  (set-key-value-gameobject! go "selected" true))
(defn reset-selected! [go]
  (set-key-value-gameobject! go "selected" false))
(defn selected? [go]
  (-> go (get-key-value-gameobject! "selected")))
(defn switch-selected! [go]
  (if (selected? go)
    (reset-selected! go)
    (set-selected! go)))

(defn ptr-duration [ptr]
  (.getDuration ptr))

(defn ptr-duration-greater? [ptr limit]
  (> (ptr-duration ptr) limit))

(defn ptr-duration-less? [ptr limit]
  (< (ptr-duration ptr) limit))

(defn add-sprite-tween! [tweens sprite x y duration]
  (let [t #js
           {:targets (array sprite)
            :x x
            :y y
            :displayWidth  (.-width sprite)
            :displayHeight  (.-height sprite)
            :duration duration}] 
    (. tweens (add t))))

(defn tint-sprite! [sprite tint]
  (. sprite (setTint tint)))

(defn clear-tint-sprite! [sprite]
  (. sprite (clearTint)))

(defn get-canvas [this-scene]
  (.. this-scene -sys -canvas))

(defn canvas-to-size [canvas]
  [(.-width canvas)
   (.-height canvas)])