(ns ui.scene
  (:require ["phaser" :refer (Scene)]
            [utility.core :as ut]
            [utility.events :as events]))


; you need to set ES5 flag in your shadow-cljs.edn config file
; still no support for ES6 in clojurescript

; apply requires that args are a js-array
(def super-args
   (array #js {:key "ui" :active true}))

(def background "./assets/background.png")
(def push "./assets/html/push.html")
(def discard"./assets/html/discard.html")
(def sort-url "./assets/html/sort.html")
(def remainder "./assets/html/rem.html")

(defn def-pos [[x y] key]
  (case key
    :discard [(/ x 1.6) (/ y 1.2)]
    :push [(/ x 2.65) (/ y 1.2)]
    :sort [(/ x 2) (/ y 1.2)]
    :draw [(/ x 1.1) (/ y 2.5)]
    [x y]))

(defn preld [this]
  ;(ut/load-image! this "background" background)
  (ut/load-html-file this "push" push)
  (ut/load-html-file this "discard" discard)
  (ut/load-html-file this "sort" sort-url)
  (ut/load-html-file this "rem" remainder))

;myButton.addEventListener ('click', callback);

(defn add-click-callback! [element function]
  (. element (addEventListener "click" function)))


(def input-state (atom {:push false
                        :discard false
                        :rank false
                        :suit false}))

(defn toggle-state! [state key]
  (if (key @state)
    (swap! state #(assoc % key false))
    (swap! state #(assoc % key true))))

(defn toggle-suit-state []
  (toggle-state! input-state :suit))

(defn toggle-rank-state []
  (toggle-state! input-state :rank))

(defn toggle-push-state []
  (toggle-state! input-state :push))

(defn toggle-discard-state []
  (toggle-state! input-state :discard))

(defn emit-state! []
  (events/emit-event! events/ui-message @input-state))

(defn update-event! [key] 
  (case key
    :push  (toggle-push-state)
    :discard (toggle-discard-state)
    :suit (toggle-suit-state)
    :rank (toggle-rank-state)
    nil)
  (emit-state!))

(defn add-button [this [x y] key]
  (ut/add-html-dom this x y key))

(defn add-cb-to-el! [element id function]
  (-> element
      (. (getChildByID id))
      (add-click-callback! function)))

(defn add-discard-callback! [this size]
  (-> this
      (add-button (def-pos size :discard) "discard")
      (add-cb-to-el! "discard" #(update-event! :discard))))

(defn add-push-callback! [this size]
  (-> this
      (add-button (def-pos size :push) "push")
      (add-cb-to-el! "push" #(update-event! :push))))

(defn add-sort-callback! [this size]
  (let [pos (def-pos size :sort)
        html (add-button this pos "sort")]
    (add-cb-to-el! html "rank" #(update-event! :suit))
    (add-cb-to-el! html "suit" #(update-event! :rank))))

(defn reset-input-state! [] 
  (let [s {:discard false
           :push false
           :rank false
           :suit false}]
    (swap! input-state (fn [_] s))))

(defn set-text-html-el! [element text]
  (set! (. element -textContent) text))

(defn add-text-to-el! [element id text]
  (-> element
      (. (getChildByID id))
      (set-text-html-el! text)))

(defn add-box [this [x y] key]
  (ut/add-html-dom this x y key))

(defn add-deck-counter [this size]
  (add-box this (def-pos size :draw) "rem"))

(defn print-the-data [counter data]
  (let [r (str "Deck: "data)]
   (add-text-to-el! counter "rem" r)))

(defn creat [this]
  (let [pos (-> this (ut/get-canvas) (ut/canvas-to-size))
        cr events/card-message
        r events/remaining-cards-in-deck
        counter (add-deck-counter this pos)]
    (events/add-event-listener! cr #(reset-input-state!))
    (events/add-event-listener! r #(print-the-data counter %))
    (add-push-callback! this pos)
    (add-discard-callback! this pos)
    (add-sort-callback! this pos)))

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

