(ns card.create.draw
  (:require [card.create.card :as card]
            [card.create.utility :as ct]
            [brute.entity :as e]
            [card.types :as t]))

(def latest-sort-state (atom {:latest-sort :rank}))

(defn set-latest-sort-state! [sort-type]
  (swap! latest-sort-state #(assoc % :latest-sort sort-type)))

(defn full-hand? [system nr]
  (->> system
       (ct/get-all-slot-entities)
       count
       (= nr)))

(defn draw [system this hm max-hand]
  (let [sort-type (:latest-sort @latest-sort-state)]
   (card/create-hand system this hm sort-type max-hand)))

(defn create-entities-for-each-card [full-deck]
  (for [i (-> full-deck count range)] 
    (let [c (nth full-deck i)
          s (-> c keys first)
          r (-> c vals first)
          e (e/create-entity)
          rc (t/->RankComponent r)
          sc (t/->SuitComponent s)
          h (t/->HiddenComponent)]
      {:entity e
       :rank rc
       :suit sc
       :hidden h
       :score (-> r t/rank-to-default-score t/->ScoreComponent)})))

(defn add-card-entity [system s]
  (let [e (:entity s)
        r (:rank s)
        su (:suit s)
        sc (:score s)
        h (:hidden s)]
   (-> system
       (e/add-entity e)
       (e/add-component e h)
       (e/add-component e r)
       (e/add-component e su)
       (e/add-component e sc))))

(defn create-deck [system full-deck]
  (let [f #(add-card-entity %1 %2)
        m (create-entities-for-each-card full-deck)]
    (reduce f system m)))

(defn draw-cards [hm this max-hand]
  (fn [system delta-time] 
    (draw system this hm max-hand)))