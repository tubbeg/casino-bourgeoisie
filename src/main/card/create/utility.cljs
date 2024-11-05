(ns card.create.utility
  (:require
   [brute.entity :as e]
   [card.types :as t]
   [utility.core :as ut]))

(defn update-scene-state! [new-state old-state]
  (swap! old-state #(assoc % :world new-state)))

(defn get-played-comp [world n]
  (e/get-component world n t/PlayedComponent))
(defn get-push-comp [world n]
  (e/get-component world n t/PushComponent))
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

(defn get-all-discard-entities [world]
  (e/get-all-entities-with-component
   world t/DiscardComponent))

(defn get-all-played-entities [world]
  (e/get-all-entities-with-component
   world t/PlayedComponent))

(defn get-all-push-entities [world]
  (e/get-all-entities-with-component
   world t/PushComponent))

(defn remove-push-comp [world name]
  (let [comps (get-push-comp world name)]
    (e/remove-component world name comps)))
(defn remove-drag-comp [world name]
  (let [comps (get-drag-comp world name)]
    (e/remove-component world name comps)))
(defn remove-sel-comp [world name]
  (let [comps (get-sel-comp world name)]
    (e/remove-component world name comps)))

(defn add-played-component [system entity]
  (-> system
      (e/add-component entity (t/->PlayedComponent))))
(defn add-push-component [system entity]
  (-> system
      (e/add-component entity (t/->PushComponent))))

(defn has-sprite? [system entity]
  (let [c (get-sprite-comp system entity)]
    (-> c nil? not)))

(defn get-all-entities-with-sprites-and-slots [system]
  (let [ents (get-all-slot-entities system)]
    (filter #(has-sprite? system %) ents)))


(defn swap-slots [entity1 system entity2]
  (when (or (nil? entity1) (nil? entity2) (nil? system))
    (println "DETECTED PROBLEM" entity1 entity2))
  (let [s1 (get-slot-comp system entity1)
        s2 (get-slot-comp system entity2)]
    (-> system
        (e/remove-component entity1 s1)
        (e/remove-component entity2 s2)
        (e/add-component entity1 s2)
        (e/add-component entity2 s1))))


(defn new-slot-comp [system entity order pos]
  (let [sl (t/->SlotComponent order pos)]
    (-> system
        (e/add-component entity sl))))

(defn remove-slot-comp [system entity]
  (let [sl (get-slot-comp system entity)]
    (-> system
        (e/remove-component entity sl))))

(defn remove-sprite-comp [system entity]
  (let [sl (get-sprite-comp system entity)]
    (-> system
        (e/remove-component entity sl))))

(defn remove-played-comp [system entity]
  (let [sl (get-played-comp system entity)]
    (-> system
        (e/remove-component entity sl))))


(defn replace-slot-comp [system entity order pos]
  (-> system
      (remove-slot-comp entity)
      (new-slot-comp entity order pos)))

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


(defn get-tweens [world]
  (let [ents (e/get-all-entities-with-component
              world t/TweensComponent)]
    (-> world
        (e/get-component (first ents) t/TweensComponent)
        (:tweens))))

(defn add-card-tween! [world entity [x y] duration]
  (when (or  (nil? world) (nil? entity))
    (println "Something is wrong here!"))
  (let [sprite (-> world
                   (get-sprite-comp entity)
                   (:sprite))
        tweens (get-tweens world)]
    (when (ut/not-nil? tweens)
      (ut/add-sprite-tween! tweens sprite x y duration))))


(defn to-def-position [[x y]]
  [(/ x 4) (/ y 1.6)])

(defn to-def-push-position [[x y]]
  [(/ x 4) (/ y 2.5)])

(defn calc-x-position [origin-x order sprite margin]
  (let [w (.-width sprite)
        mult (*  order (+ w margin))]
    (+ origin-x mult)))

(defn update-slot-entity [system entry [dx dy]]
  (let [order (:order entry)
        margin 15
        sc (get-sprite-comp system (:entity entry))
        pos-x (calc-x-position dx order (:sprite sc) margin)
        pos [pos-x dy]]
    (-> system
        (replace-slot-comp (:entity entry) (:order entry) pos))))

(defn update-slots [system def-pos]
  (let [ents (get-all-slot-entities system)
        coll (for [i (-> ents count range)]
               {:entity (nth ents i)
                :order (+ i 1)})]
    (reduce #(update-slot-entity %1 %2 def-pos) system coll)))

(defn selectable? [system entity]
  (let [drag (get-drag-comp system entity)
        played (get-played-comp system entity)
        push (get-played-comp system entity)
        slot (get-slot-comp system entity)]
    (and
     (nil? drag)
     (nil? played)
     (nil? push)
     (-> slot nil? not))))

(defn draggable? [system entity]
  (let [played (get-played-comp system entity)
        push (get-played-comp system entity)
        slot (get-slot-comp system entity)]
    (and
     (nil? played)
     (nil? push)
     (-> slot nil? not))))


(defn remove-card [system entity]
  (-> system
      (get-sprite-comp entity)
      :sprite
      ut/destroy-sprite!)
  (-> system
      (remove-sprite-comp entity)
      (e/kill-entity entity)))