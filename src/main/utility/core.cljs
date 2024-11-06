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

(defn disable-interactive! [object]
  (. object (disableInteractive)))

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
            ;:delay 150
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

(defn load-html-texture [^js this key path [x y]]
  (.. this -load (htmlTexture key path x y)))

(defn load-html-file [this key path]
  (.. this -load (html key path)))

;this.add.dom(x, y).createFromCache(key);
(defn add-html-dom [^js this x y k]
  (let [dom (.. this -add (dom x y))]
    (. dom (createFromCache ^js k))
    dom))

(defn set-sprite-scale! [sprite scale]
  (set! (.-scale sprite) scale))

(defn load-svg
  "Loads an svg file in the same way as loading a png.
   size-or-scale can be for example: { scale: 2.5 }
   or  { width: 300, height: 600 }"
  [this key path svg-config]
  (.. this -load (svg key path svg-config)))

(comment
"const sprite = this.add.sprite ();

sprite.preFX.addGlow ();
sprite.postFX.addGlow ();
")

(defn add-prefx-glow-to-sprite! [sprite]
  (.. sprite -preFX (addGlow)))

(defn add-postfx-glow-to-sprite! [sprite]
  (.. sprite -postFX (addGlow "0xd5df0e")))

(defn clear-postfx-sprite! [sprite]
  (.. sprite -postFX (clear)))


(defn set-visibility-sprite! [sprite bool]
  (. sprite (setVisible bool)))


(defn get-all-tweens-scene [scene]
  (.. scene -tweens -tweens))

(defn get-all-tweens-tm [tweens-manager]
  (. tweens-manager -tweens))

(defn tween-has-target? [tween game-object]
  (let [t (.-targets tween)
        f (filter #(= (hash game-object) (hash %)) t)]
    (> (count f) 0)))

(defn sprite-has-tween? [tweens-manager sprite]
  (let [t (get-all-tweens-tm tweens-manager)
        f (filter #(tween-has-target? % sprite) t)] 
    (> (count f) 0)))

(defn destroy-sprite! [sprite]
  (. sprite (destroy)))