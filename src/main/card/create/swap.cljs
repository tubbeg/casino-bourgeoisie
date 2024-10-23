(ns card.create.swap
  (:require
   [card.create.utility :as ct]
   [brute.entity :as e]
   [utility.core :as ut]
   [schema.core :as s]))


(defn entity-has-matching-slot-nr [world entity nr]
  (->
   world
   (ct/get-slot-comp entity)
   (:order)
   (= nr)))

(defn next-entity [nr world]
  (let [e (ct/get-all-slot-entities world)
        f (filter #(entity-has-matching-slot-nr world % nr) e)]
    (first f)))

(defn clip-one [n]
  (if (< n 1) 1 n))

(defn clip-max [n max]
  (if (> n max) max n))

(defn next-slot [system entity]
  (let [slot (ct/get-slot-comp system entity)
        n (ct/get-swap-comp system entity)
        max (:max slot)
        left (-> slot (:order) (- 1) (clip-one))
        right (-> slot (:order) (+ 1) (clip-max max))]
    (if (= n :left)
      left
      right)))

(defn swap-slots [system entity1 entity2]
  (let [s1 (ct/get-slot-comp system entity1)
        s2 (ct/get-slot-comp system entity2)]
    (-> system
        (e/remove-component entity1 s1)
        (e/remove-component entity2 s2)
        (e/add-component entity1 s2)
        (e/add-component entity2 s1))))

(defn valid-entities? [entity1 entity2]
  (and (not= entity1 nil)
       (not= entity2 nil)
       (not= entity1 entity2)))

(defn remove-swap-comp [system entity]
  (let [swp (ct/get-swap-comp system entity)]
    (e/remove-component system entity swp)))

(defn swap-if-valid [system entity]
  (let [nxt (-> system
                (next-slot entity)
                (next-entity system))]
    (if (valid-entities? entity nxt)
      (do 
          (-> system
              (swap-slots entity nxt)
              ;(remove-swap-comp entity)
              ))
      system)))

(defn swap-entity-slots [system delta-time]
  ;(println "Swaps in swap func" (ct/get-all-swap-entities system))
  (loop [s system
         ents (ct/get-all-swap-entities system)]
    (if (ut/zero-coll? ents)
      s
      (-> (swap-if-valid s (first ents))
          (recur (next ents))))))