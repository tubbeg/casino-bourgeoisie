(ns card.types
  (:require [schema.core :as s]
            [brute.entity :as e]
            [brute.system :as sy]))

(s/defschema Suit
  (s/enum :clubs :spades :hearts :diamonds))

(s/defschema FaceOrAce
  (s/enum :jack :queen :king :ace))

(defn card-number? [n]
  (and (> n 1) (< n 11)))

(s/defschema Number 
  (s/pred card-number?))

(s/defschema Rank
  (s/if card-number? Number FaceOrAce))

(s/defschema Card
  {Suit Rank})

(defn rank-to-int [rank]
  (-> (s/validate Rank rank)
      (case
        2 2
        3 3
        4 4
        5 5
        6 6
        7 7
        8 8
        9 9
        10 10
        :jack 11
        :queen 12
        :king 13
        :ace 14)))

(defn rank-to-default-score [rank]
  (-> (s/validate Rank rank)
      (case
       2 2
       3 3
       4 4
       5 5
       6 6
       7 7
       8 8
       9 9
       10 10
       :jack 10
       :queen 10
       :king 10
       :ace 11)))

(defrecord RankComponent [rank])
(defrecord SuitComponent [suit])
(defrecord ScoreComponent [score])
;(defrecord TextureComponent [texture])
(defrecord SlotComponent [order pos max])
(defrecord SpriteComponent [sprite])

;input

(defrecord DragComponent [bool x y])
(defrecord SelectComponent [])

; this is certainly not an ideal way to use
; class instances. There are far better ways
; to do this. But it becomes a bit tricky to fit
; an ECS into an existing game framework
(defrecord TweensComponent [tweens])
