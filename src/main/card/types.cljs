(ns card.types
  (:require [schema.core :as s]))


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

(s/defschema GameCard
  {:card Card
   :score s/Int})

(s/defschema SpriteCard
  {:id s/Str
   :card GameCard
   :order s/Int})

(s/defschema SpriteDeck
  [SpriteCard])