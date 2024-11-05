(ns card.create.push 
  (:require
    [card.create.utility :as ct]
    [brute.entity :as e]
    [card.types :as t]
    [utility.core :as ut]))

(def tween-duration 100)

(def push-state (atom {:push false}))

(defn set-push-state! []
  (swap! push-state #(assoc % :push true)))

(defn reset-push-state! []
  (swap! push-state #(assoc % :push false)))

(defn push? []
  (:push @push-state))

(defn remove-comp-add-push [system entity]
  (-> system
      (ct/remove-sel-comp entity)
      (ct/remove-slot-comp entity)
      (ct/add-push-component entity)
      (ct/add-played-component entity)))

(defn push [system]
  (reset-push-state!)
  (->> system
       (ct/get-all-sel-entities)
       (reduce #(remove-comp-add-push %1 %2) system)))

(defn move-card-to-center! [system entity pos]
  (ct/add-card-tween! system entity pos tween-duration))

(defn move-pushed-card! [system entry [dx dy]]
  (let [entity (:entity entry)
        order (:order entry)
        margin 15
        sc (ct/get-sprite-comp system entity)
        x-pos (ct/calc-x-position dx order (:sprite sc) margin)]
    (ut/clear-postfx-sprite! (:sprite sc))
    (move-card-to-center! system entity [x-pos dy])
    (ct/remove-push-comp system entity)))

(defn adjust-slots-after-push [ def-pos system]
  (ct/update-slots system def-pos))

(defn move-push [push-pos def-pos] 
  (fn [system delta-time]
      (let [ents (ct/get-all-push-entities system)]
        (if (ut/zero-coll? ents)
          system
          (->> (for [i (-> ents count range)]
                 {:entity (nth ents i)
                  :order (+ i 1)})
               (reduce #(move-pushed-card! %1 %2 push-pos) system)
               (adjust-slots-after-push def-pos))))))

(defn push-cards
 [system delta-time]
   (if (push?)
     (push system)
     system))

