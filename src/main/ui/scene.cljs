(ns ui.scene
  (:require ["phaser" :refer (Scene)]
             [goog.object :as gobj]
             [utility.core :as ut]))


; you need to set ES5 flag in your shadow-cljs.edn config file
; still no support for ES6 in clojurescript

; apply requires that args are a js-array
(def super-args
   (array #js {:key "ui" :active true}))

(defn preld [this]
  (ut/load-image! this "background" "./assets/background.png"))

(defn creat [this]
  (ut/add-image this 400 300 "background"))

(defn ui-scene []
  (this-as this
           (.apply Scene this super-args)
           this))

(set! (.. ui-scene -prototype)
      (js/Object.create (.-prototype Scene)))

(set! (.. ui-scene -prototype -constructor)
      ui-scene)

(set! (.. ui-scene -prototype -create)
      (fn [] (this-as this (creat this) this)))

(set! (.. ui-scene -prototype -preload)
      (fn [] (this-as this (preld this) this)))

