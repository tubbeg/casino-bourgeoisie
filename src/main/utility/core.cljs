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

(defn add-draggable-sprite!
  ([this x y texture name]
   (let [s (-> this
               (add-sprite x y texture)
               (set-interactive!))]
     (set-draggable! this s true)
     (set-sprite-name! s name)
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

(defn set-xy-object! [obj x y]
  (set! (.-x obj) x)
  (set! (.-y obj) y))

(defn set-x-object! [obj x ]
  (set! (.-x obj) x))

(defn set-y-object! [obj y]
  (set! (.-y obj) y))

(defn zero-coll? [coll]
  (-> coll (count) (zero?)))