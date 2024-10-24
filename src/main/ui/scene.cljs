(ns ui.scene
  (:require ["phaser" :refer (Scene)]
            [utility.core :as ut]
            [utility.events :as event]))


; you need to set ES5 flag in your shadow-cljs.edn config file
; still no support for ES6 in clojurescript

; apply requires that args are a js-array
(def super-args
   (array #js {:key "ui" :active true}))

(def background "./assets/background.png")
(def push "./assets/html/push.html")
(def discard"./assets/html/discard.html")

(defn preld [this]
  ;(ut/load-image! this "background" background)
  (ut/load-html-file this "push" push)
  (ut/load-html-file this "discard" discard))


;myButton.addEventListener ('click', callback);

(defn add-click-callback! [element function]
  (. element (addEventListener "click" function)))

(def input-state (atom {:discard false
                        :push false}))

;eventsCenter.emit('update-count', this.count)

(defn update-state! [state key value]
  (println "Updating state" key value)
  (event/emit-event event/eventEmitter "stupid" #js{:you :stupid})
  (swap! state #(assoc % key value)))

(defn creat [this]
  (let [[x y] (-> this
                  (ut/get-canvas)
                  (ut/canvas-to-size))
        push (ut/add-html-dom
              this (/ x 2.5) (/ y 1.2) "push")
        discard (ut/add-html-dom
                 this (/ x 1.7) (/ y 1.2) "discard")
        pc (. push (getChildByID "push"))
        dc (. discard (getChildByID "discard"))]
    (add-click-callback! pc #(update-state! input-state :push true))
    (add-click-callback! dc #(update-state! input-state :discard true))))

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

