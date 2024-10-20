(ns card.create.core
  (:require [utility.core :as ut]
            [card.types :as t]
            [schema.core :as s]))

(defn drag-card! [pointer gameObject dragX dragY]
  (println "This function is TRIGGGERED >:O")
  ;(println dragX dragY)
  (set! (.-x gameObject) dragX)
  (set! (.-y gameObject) dragY))

(defn displace-crds! [max-length sprites deck]
  (let []
   (loop [cards sprites
          last 0]
     (if (-> cards (count) (zero?))
       nil
       (let [f (first cards)
             r (next cards)
             c (count sprites)
             width (/ max-length c)
             displace (+ width last)]
         (ut/set-x-object! f last)
         (recur r displace))))))

(defn displace-cards! [max-length sprites deck]
  (->> deck
       (s/validate t/SpriteDeck)
       (displace-crds! max-length sprites)))

(defn drag-end-conf
  [object]
  #js{:targets object
      :x 0 
      :y 0
      :duration 200
      :displayWidth (.-width object)
      :displayHeight (.-height object)})

(defn drag-card-end! [this object sprite-coll deck]
  (when (ut/not-nil? object) 
    (->> object (drag-end-conf) (ut/add-tweens this))
    (displace-cards! 300 sprite-coll deck)))

(defn sd-to-id [deck]
  (map #(:id %) deck))

(defn create [this deck]
  (let [sc (-> deck
               (sd-to-id)
               (ut/add-draggable-sprites! 0 0 this))
        c (ut/add-container this 400 400)]
    (letfn [(dce! [p g x y] (drag-card-end! this g sc deck))]
      (ut/add-items-to-container! c sc)
      (displace-cards! 300 sc deck)
      (ut/in-on-dragend this  dce!)
      (ut/in-on-drag this drag-card!))))

(defn creat [this deck]
  (->> deck
       (s/validate t/SpriteDeck)
       (create this)))

