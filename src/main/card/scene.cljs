(ns card.scene
  (:require ["phaser" :refer (Scene)]
            [utility.core :as ut]
            [card.preload.core :as p]
            [card.create.core :as c]
            [card.update.core :as u]
            [brute.entity :as e]
            [brute.system :as sy]
            [card.types :as t]
            [card.default-deck.core :as default]
            [schema.core :as s]))

; you need to set ES5 flag in your shadow-cljs.edn config file.

; apply requires that args are a js-array
(def super-args
   (array #js {:key "card" :active true}))

(def scene-state (atom {:world nil}))


(def max-cards 5)

(defn crte []
  (this-as this (c/creat this scene-state default/deck max-cards) this))

(defn updte [time delta]
  (this-as this
           (u/update-scene this time delta scene-state) this))

(defn prld []
  (this-as this
           (p/preld this) this))

(defn card-scene []
  (this-as this
           (.apply Scene this super-args)
           this))

(set! (.. card-scene -prototype)
      (js/Object.create (.-prototype Scene)))

(set! (.. card-scene -prototype -constructor)
      card-scene)

(set! (.. card-scene -prototype -create)
      crte)

(set! (.. card-scene -prototype -update)
      updte)

(set! (.. card-scene -prototype -preload)
      prld)

