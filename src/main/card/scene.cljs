(ns card.scene
  (:require ["phaser" :refer (Scene)]
            [utility.core :as ut]
            [card.preload.core :as p]
            [card.create.core :as c]
            [card.types :as t]
            [card.default-deck.core :as default]
            [schema.core :as s]))

; you need to set ES5 flag in your shadow-cljs.edn config file.

; apply requires that args are a js-array
(def super-args
   (array #js {:key "card" :active true}))

(defn take-5-rand [deck]
  [(rand-nth deck) (rand-nth deck)
   (rand-nth deck) (rand-nth deck)
   (rand-nth deck)])

(defn card-to-sprite-card [c order score]
  (let [gc {:card c
            :score score}
        sc {:id (str c)
            :card gc
            :order order}]
    (s/validate t/SpriteCard sc)))

(defn deck-to-sprite-deck [deck]
  (->>
   (for [i (-> deck (count) (range))]
     (card-to-sprite-card (nth deck i) i i))
   (into [])))

(defn create []
  (this-as this (c/creat this (deck-to-sprite-deck default/deck))
           this))

(defn card-scene []
  (this-as this
           (.apply Scene this super-args)
           this))

(set! (.. card-scene -prototype)
      (js/Object.create (.-prototype Scene)))

(set! (.. card-scene -prototype -constructor)
      card-scene)

(set! (.. card-scene -prototype -create)
      create)

(set! (.. card-scene -prototype -preload)
      (fn [] (this-as this (p/preld this) this)))

(set! (.. card-scene -prototype -update)
      (fn [] nil))
