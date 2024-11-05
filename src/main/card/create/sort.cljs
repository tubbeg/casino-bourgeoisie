(ns card.create.sort 
  (:require
    [card.create.utility :as ct]
    [brute.entity :as e]
    [utility.core :as ut]
    [card.types :as t]))

(def sort? (atom {:sort {:rank false :suit false}}))

(defn sort-slot-rank [world entity]
    (-> world
        (ct/get-rank-comp entity)
        (:rank)
        (t/rank-to-int)
        (- 1)))

(defn sort-slot-suit [world entity]
  (-> world
      (ct/get-suit-comp entity)
      (:suit)
      (t/suit-to-default-int)))

(defn sort-entities-by-rank [ent-coll world]
  (sort-by #(sort-slot-rank world %) ent-coll))

(defn sort-entities-by-suit [ent-coll world]
  (let [result (sort-by #(sort-slot-suit world %) ent-coll)]
    (println result)
    result))

(defn entity-has-order? [world entity order]
  (let [slot (ct/get-slot-comp world entity)]
    (= (:order slot) order)))

(defn find-entity-with-order [world order]
  (->> t/SlotComponent
       (e/get-all-entities-with-component world)
       (filter #(entity-has-order? world % order))
       (first)))

(defn find-slot [system order]
  (->> (find-entity-with-order system order)
       (ct/get-slot-comp system)))

(defn get-slot-data [system order]
 (let [sl (-> system
              (find-slot order))] 
   [(:order sl) (:pos sl)]))

(defn transform-slot-data [ entity order system]
  (let [[o p] (get-slot-data system order)]
    {:entity entity :order o
     :pos p}))

(defn sort-entities-suit-rank [system type]
  (if (= type :suit)
    (-> (ct/get-all-slot-entities system)
        (sort-entities-by-suit system)) 
    (-> (ct/get-all-slot-entities system)
        (sort-entities-by-rank system))))

(defn create-sort-coll [world t]
  (let [ents (sort-entities-suit-rank world t)
        coll (for [i (-> ents count range)]
               {:entity (nth ents i)
                :order (+ i 1)})
        f #(->> world 
                (transform-slot-data (:entity %2) (:order %2))
                (conj %1))]
    (->> coll
         (reduce f [])
         (filter #(not= (:pos %) nil)))))

(defn sort-world [system coll]
  (-> #(ct/replace-slot-comp %1 (:entity %2) (:order %2) (:pos %2))
      (reduce system coll)))

(defn sort-rank-world [system]
  (let [coll (create-sort-coll system :rank)]
    (sort-world system coll)))

(defn sort-suit-world [system]
  (let [coll (create-sort-coll system :suit)]
    (sort-world system coll)))

(defn update-sort! [rank? suit?]
  (let [m {:rank rank?
           :suit suit?}]
   (swap! sort? #(assoc % :sort m))))

(defn set-sort-rank! []
  (let [s (-> @sort? :sort :suit)]
    (update-sort! true s)))

(defn set-sort-suit! []
  (let [r (-> @sort? :sort :rank)]
    (update-sort! r true)))

(defn reset-sort-rank! []
  (let [s (-> @sort? :sort :suit)]
    (update-sort! false s)))

(defn reset-sort-suit! []
  (let [r (-> @sort? :sort :rank)]
    (update-sort! r false)))

(defn sort-deck-by-rank? [] 
  (-> @sort? :sort :rank))

(defn sort-deck-by-suit? [] 
  (-> @sort? :sort :suit))

(defn reset-state-and-sort-by-rank [system]
  (let [s (sort-rank-world system)]
    (reset-sort-rank!)
    s))

(defn reset-state-and-sort-by-suit [system]
    (let [s (sort-suit-world system)]
      (reset-sort-suit!)
      s))

(defn sort-deck [system delta-time]
  (cond
    (sort-deck-by-rank?) (reset-state-and-sort-by-rank system)
    (sort-deck-by-suit?) (reset-state-and-sort-by-suit system)
    :else system))