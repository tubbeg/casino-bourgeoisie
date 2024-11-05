(ns card.create.select 
  (:require
    [utility.core :as ut]
    [card.create.utility :as ct]
    [brute.entity :as e]
    [card.types :as t]))

(defn has-no-select-comp [system entity]
  (-> (ct/get-sel-comp system entity)
      (nil?)))

(defn has-select-comp [system entity]
  (-> system (has-no-select-comp entity) (not)))

(defn rm-sel-comp [system entity]
  (let [sel (ct/get-sel-comp system entity)
        s (-> system (ct/get-sprite-comp entity) (:sprite))]
    ;(ut/clear-tint-sprite! s)
    (ut/clear-postfx-sprite! s)
    (-> system
        (e/remove-component entity sel))))

(defn add-select? [system entity]
  (let [sprite (-> system
                   (ct/get-sprite-comp entity)
                   (:sprite))]
   (and (has-no-select-comp system entity)
        (ut/selected? sprite))))

(defn remove-select? [system entity]
  (let [sprite (-> system
                   (ct/get-sprite-comp entity)
                   (:sprite))]
    (and (has-select-comp system entity)
         (-> sprite (ut/selected?) (not)))))

(defn get-rand-tint []
  (* 16000000 (.random js/Math)))

(def my-tint 7329113.367983451)

(defn add-select [system entity]
  (let [sel (t/->SelectComponent) 
        s (-> system (ct/get-sprite-comp entity) (:sprite))]
    ;(ut/tint-sprite! s my-tint)
    (ut/add-postfx-glow-to-sprite! s)
    (ct/print-debug-info-entity system entity)
    (e/add-component system entity sel)))

(defn add-or-remove-sel-comp [system entity]
  (cond
    (add-select? system entity) (add-select system entity)
    (remove-select? system entity) (rm-sel-comp system entity)
    :else system))


(defn add-remove-select-components [system delta-time]
  (->> system
       (ct/get-all-sprite-entities) 
       (filter #(ct/selectable? system %))
       (reduce #(add-or-remove-sel-comp %1 %2) system)))