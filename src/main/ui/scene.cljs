(ns ui.scene
  (:require ["phaser" :refer (Scene)]
             [goog.object :as gobj]
             [utility.core :as ut]))


; you need to set ES5 flag in your shadow-cljs.edn config file
; still no support for ES6 in clojurescript

; apply requires that args are a js-array
(def super-args
   (array #js {:key "ui" :active true}))

(def background "./assets/background.png")
(def push "./assets/html/push.html")
(def discard"./assets/html/discard.html")

(defn preld [this]
  (ut/load-image! this "background" background)
  (ut/load-html-file this "push" push)
  (ut/load-html-file this "discard" discard)
  )

(defn creat [this]
  (let [[x y] (-> this
                  (ut/get-canvas)
                  (ut/canvas-to-size))]
    ;(ut/add-image this 400 300 "background")
    (ut/add-html-dom this (/ x 2) (/ y 1.2) "push")
    (ut/add-html-dom this (/ x 1.5) (/ y 1.2) "discard")
    ))

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

