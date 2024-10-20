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
    :ace "ACE"
    (str v)))

(defn card-type [s t]
  (case t
    :clubs (str "Clubs_" s)
    :hearts (str "Hearts_" s)
    :diamonds (str "Diamonds_" s)
    :spades (str "Spades_" s)))

(defn card-path-str-builder [card]
  (let [c (-> c/Card (s/validate card))
        k (-> c (keys) (first))
        v (-> c (vals) (first))]
    (-> (card-rank-to-str v)
        (card-type k)
        (str ".png"))))

(let [a 10]
  (+ a 10))

(defn load-card! [card this]
  (let [s (card-path-str-builder card)
        id (str card)]
    (ut/load-image! this id s)))

(defn load-deck! [deck this]
  (doseq [i (-> deck (count) (range))]
    (-> deck
        (nth i)
        (load-card! this))))

(defn preld [this]
  (println "Loading")
  (ut/set-base-url! this "./assets/KINS")
  ;(ut/load-image! this "king" "Clubs_K.png")
  ;(ut/load-image! this "ace" "Hearts_ACE.png")
  (load-deck! default/deck this))

