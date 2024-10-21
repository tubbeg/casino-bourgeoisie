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
(defrecord OrderComponent [order])
(defrecord SpriteComponent [sprite])

;input

(defrecord DragComponent [bool x y])
(defrecord EventComponent [event])

(comment
(defrecord MyStuff [])

(def myEntity (e/create-entity))
(defrecord MyiNSTANCE [interesting stuff])
(def testINsnace (->MyiNSTANCE :stuff :stuffer))
(def testINsnace2 (MyiNSTANCE. :stufffest :stff))

(defn my-system-func [system delta-time]
  (let [entities (e/get-all-entities-with-component
                  system MyiNSTANCE)]
    (doseq [entity entities]
      (let [c (e/get-component system entity MyiNSTANCE)]
        (println "Found component" c)))))

(defn create-systems [system]
  (-> system
      (sy/add-system-fn my-system-func)))

(def sys
  (-> (e/create-system)
      (e/add-entity myEntity)
      (e/add-component myEntity (->MyiNSTANCE :stuff :stuffer))
      (create-systems)))

(sy/process-one-game-tick sys 32)
)