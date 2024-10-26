(ns card.create.sort 
  (:require
    [card.create.utility :as ct]
    [brute.entity :as e]
    [utility.core :as ut]
    [card.types :as t]))

(defn sort-suit-world [world])

(defn get-all-ents-with-sprite-slots [world]
  (let [es (ct/get-all-sprite-entities world)
         e (ct/get-all-slot-entities world)]
    (-> (concat es e)
        (distinct))))

(defn sort-slot-rank [world entity]
    (-> world
        (ct/get-rank-comp entity)
        (:rank)
        (t/rank-to-int)
        (- 1)))

(defn sort-entities-by-rank [ent-coll world]
  (sort-by #(sort-slot-rank world %) ent-coll))

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
   [(:order sl) (:pos sl) (:max sl)]))



(defn create-sort-coll [world]
  (let [ents (-> (get-all-ents-with-sprite-slots world)
                 (sort-entities-by-rank world))]
    (loop [c []
           e ents
           order 1]
      (if (ut/zero-coll? e)
        c
        (let [f (first e)
              rem (next e)
              [o p m] (get-slot-data world order)
              data {:entity f :order o
                    :pos p :max m}]
          (recur (conj c data) rem (+ order 1)))))))



(defn sort-rank-world [system]
  (let [coll (create-sort-coll system)]
    (println "Sorting accordingly to")
    (println coll)
    (loop [c coll
           s system]
      (if (ut/zero-coll? c)
        s
        (let [f (first c)
              rem (next c)
              e (:entity f)
              o (:order f)
              p (:pos f)
              m (:max f)]
          (->> (ct/replace-slot-comp s e o p m)
               (recur rem)))))))


(defn sort-rank [state]
  (let [system (:world @state)]
    (when (ut/not-nil? system)
      (-> (sort-rank-world system) 
          (ct/update-scene-state! state)))))

(defn sort-suit [state]
  (let [system (:world @state)]
    (when (ut/not-nil? system)
      (-> (do (println "NOT IMPLEMENTED!!")
              system)
          (ct/update-scene-state! state)))))