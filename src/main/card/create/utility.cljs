(ns card.create.utility
  (:require
   [brute.entity :as e]
   [card.types :as t]
   [utility.core :as ut]))

(defn update-scene-state! [new-state old-state]
  (swap! old-state #(assoc % :world new-state)))

(defn get-suit-comp [world n]
  (e/get-component world n t/SuitComponent))
(defn get-rank-comp [world n]
  (e/get-component world n t/RankComponent))
(defn get-sel-comp [world n]
  (e/get-component world n t/SelectComponent))
(defn get-slot-comp [world n]
  (e/get-component world n t/SlotComponent))
(defn get-drag-comp [world n]
  (e/get-component world n t/DragComponent))
(defn get-sprite-comp [world n]
  (e/get-component world n t/SpriteComponent))
(defn get-all-drag-entities [world]
  (e/get-all-entities-with-component
   world t/DragComponent))
(defn get-all-slot-entities [world]
  (e/get-all-entities-with-component
   world t/SlotComponent))
(defn get-all-sel-entities [world]
  (e/get-all-entities-with-component
   world t/SelectComponent))
(defn get-all-sprite-entities [world]
  (e/get-all-entities-with-component
   world t/SpriteComponent))
(defn remove-drag-comp [world name]
  (let [comps (get-drag-comp world name)]
    (e/remove-component world name comps)))



(defn swap-slots [entity1 system entity2]
  (let [s1 (get-slot-comp system entity1)
        s2 (get-slot-comp system entity2)]
    (-> system
        (e/remove-component entity1 s1)
        (e/remove-component entity2 s2)
        (e/add-component entity1 s2)
        (e/add-component entity2 s1))))


(defn new-slot-comp [system entity order pos max]
  (let [sl (t/->SlotComponent order pos max)]
    (-> system
        (e/add-component entity sl))))

(defn remove-slot-comp [system entity]
  (let [sl (get-slot-comp system entity)]
    (-> system
        (e/remove-component entity sl))))

(defn replace-slot-comp [system entity order pos max]
  (-> system
      (remove-slot-comp entity)
      (new-slot-comp entity order pos max)))

(defn get-debug-info-system [system]
  (loop [ents (get-all-sprite-entities system)
         c []]
    (if (ut/zero-coll? ents)
      (->> c (sort-by #(:order %)))
      (let [entity (first ents)
            sl (get-slot-comp system entity)
            r (get-rank-comp system entity)
            s (get-suit-comp system entity)
            m {:order (:order sl)
               :position (:pos sl)
               :rank (:rank r)
               :suit (:suit s)}]
        (recur (next ents) (conj c m))))))

(defn print-debug-info-system [system]
  (-> system (get-debug-info-system) (println)))

(defn get-debug-info [system entity]
  (let [sl (get-slot-comp system entity)
        r (get-rank-comp system entity)
        s (get-suit-comp system entity)
        m {:order (:order sl)
           :position (:pos sl)
           :rank (:rank r)
           :suit (:suit s)}]
    m))

(defn print-debug-info-entity [system entity]
  (-> system (get-debug-info entity) (println)))


(defn set-visibility-all-sprites! [system bool]
    (doseq [entity (get-all-sprite-entities system)]
      (let [s (-> system
                  (get-sprite-comp entity)
                  (:sprite))]
        (ut/set-visibility-sprite! s bool))))