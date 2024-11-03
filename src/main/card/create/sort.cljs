(ns card.create.sort 
  (:require
    [card.create.utility :as ct]
    [brute.entity :as e]
    [utility.core :as ut]
    [card.types :as t]
    [card.create.order :as ord]))

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

(def sort? (atom {:sort {:rank false :suit false}}))

(defn update-sort! [rank? suit?]
  (let [m {:rank rank?
           :suit suit?}]
   (swap! sort? #(assoc % :sort m))))

(defn set-sort-rank! []
  (let [s (-> @sort? :sort :suit)]
    (update-sort! true s)))

(defn reset-sort-rank! []
  (let [s (-> @sort? :sort :suit)]
    (update-sort! false s)))

(defn get-tweens [world]
  (let [ents (e/get-all-entities-with-component
              world t/TweensComponent)]
    (-> world
        (e/get-component (first ents) t/TweensComponent)
        (:tweens))))

(defn no-tweens-active? [tweens-manager]
  (-> tweens-manager (ut/get-all-tweens-tm) (count) (= 0)))

(defn sort-deck? [system]
  (let [t (get-tweens system)]
    (and ;(no-tweens-active? t)
         true
         (-> @sort? :sort :rank))))

(defn set-order-has-sorted-state! []
  (swap! ord/has-sorted-state #(assoc % :sorted true)))

(defn sort-rank [system delta]
  (if (sort-deck? system)
    (let [s (sort-rank-world system)]
      (reset-sort-rank!)
      (set-order-has-sorted-state!)
      s)
    system))