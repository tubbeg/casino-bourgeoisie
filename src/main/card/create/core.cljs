(ns card.create.core
  (:require [utility.core :as ut]
            [utility.events :as events]
            [card.types :as t]
            [brute.entity :as e]
            [brute.system :as sy]
            [schema.core :as s]
            [card.default-deck.core :as default]
            [card.create.dragging :as dr]
            [card.create.card :as cr]
            [card.create.utility :as ct]
            [card.create.order :as ord]
            [card.create.select :as sel]
            [card.create.sort :as sort]
            [utility.events :as event]))



(defn add-system-functions [system]
  (-> system
      (sy/add-system-fn dr/on-drag-fn)
      (sy/add-system-fn ord/order-cards)
      (sy/add-system-fn sel/add-remove-select-components)))

(defn add-tweens [system this]
  (let [tweens (-> this (.-tweens) (t/->TweensComponent))
        entity (e/create-entity)]
   (-> system
       (e/add-entity entity)
       (e/add-component entity tweens))))

(defn create-world [this deck position]
  (-> (e/create-system)
      (cr/create-deck this position deck 5)
      (add-tweens this)
      (add-system-functions)))

(defn to-def-position [[x y]]
  [(/ x 4) (/ y 1.6)])

(defn sort-rank? [data]
  (:rank data))

(defn sort-suit? [data]
  (:suit data))

(defn discard? [data]
  (:discard data))

(defn push? [data]
  (:push data))

(defn reset-message! []
  (let [msg events/card-message
        ee events/eventEmitter]
   (event/emit-event ee msg "reset")))

(defn sort-cards [system data]
  (cond
    (sort-rank? data) (sort/sort-rank-world system)
    (sort-suit? data) system
    (discard? data) system
    (push? data) system
    :else system))

(defn handle-input-events [data state]
  (println "Data:" data)
  (reset-message!)
  (let [world (:world @state)]
    (when (ut/not-nil? world)
      (let [s (sort-cards world data)
            dbg-data1 (ct/get-debug-info-system s)
            dbg-data2 (ct/get-debug-info-system world)
            h1 (hash dbg-data1)
            h2 (hash dbg-data2)]
        (println "hash" h1 h2)
        (println "equals hash? " (= h1 h2))
        (ct/print-debug-info-system s)
        (ct/set-visibility-all-sprites! world false)
        (ct/update-scene-state! s state)
        (ct/set-visibility-all-sprites! world true)
        (let [dgb-data2 (ct/get-debug-info-system (:world @state))]
          (println "Hash2 " (hash dgb-data2))
          (println "equals2? " (= (hash dgb-data2) h1)))))))

(defn creat [this state deck]
  (let [pos (-> this
                (ut/get-canvas)
                (ut/canvas-to-size)
                (to-def-position))
        uim events/ui-message] 
    (-> (create-world this deck pos)
        (ct/update-scene-state! state))
    (letfn [(drag [p go x y] (dr/dragging! p go x y state))
            (dragend [p go x y] (dr/dragend! go state))]
      (ut/in-on-drag! this drag)
      (ut/in-on-dragend! this dragend) 
      (events/add-event-listener!
       uim #(handle-input-events % state)))))

