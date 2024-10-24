(ns card.preload.core
  (:require [utility.core :as ut]
            [card.types :as c]
            [schema.core :as s]
            [card.default-deck.core :as default]))


(defn card-rank-to-str [v]
  (case v
    :jack "J"
    :queen "Q"
    :king "K"
    :ace "A"
    10 "T"
    (str v)))

(defn card-type [s t]
  (case t
    :clubs (str s "C" )
    :hearts (str s "H")
    :diamonds (str s "D")
    :spades (str s "S")))

(defn card-path-str-builder [card]
  (let [c (-> c/Card (s/validate card))
        k (-> c (keys) (first))
        v (-> c (vals) (first))]
    (-> (card-rank-to-str v)
        (card-type k)
        (str ".svg"))))

(let [a 10]
  (+ a 10))

(defn load-card! [card this]
  (let [s (card-path-str-builder card)
        id (str card)
        svg-config #js{:scale 55}]
    (ut/load-svg this id s svg-config)))

(defn load-deck! [deck this]
  (doseq [i (-> deck (count) (range))]
    (-> deck
        (nth i)
        (load-card! this))))
(defn preld [this]
  (println "Loading")
  (ut/set-base-url! this "./assets/poker")
  (load-deck! default/deck this))

