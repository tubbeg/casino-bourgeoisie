(ns card.create.score 
  (:require
    [card.create.utility :as ct]
    [utility.core :as ut]))

(def duration-freq 6)
(def time-freq 300)
(def tween-duration 200)
(def time-state (atom {:time-counter 0
                       :time 0}))

(defn reset-time-counter-state! []
  (swap! time-state #(assoc % :time-counter 0)))

(defn reset-time! []
  (swap! time-state #(assoc % :time 0)))

(defn add-time! [dt]
  (let [next-time (-> @time-state :time (+ dt))]
    (swap! time-state #(assoc % :time next-time))))

(defn add-time-counter! [dt]
  (let [next-time (-> @time-state :time-counter (+ dt))]
    (swap! time-state #(assoc % :time-counter next-time))))

(defn move-card [system entity pos]
  (println "MOVING")
  (ct/add-card-tween! system entity pos tween-duration)
  system)

(def my-tint 7329113.367983451)

; emit event here to other scene!
(defn tint-card [system entity]
  (let [sprite (-> system (ct/get-sprite-comp entity) :sprite)]
    (ut/tint-sprite! sprite my-tint))
  system)

(defn reset-time-kill-card [system entity]
  (println "KILLING")
  (reset-time-counter-state!)
  (-> system
      (ct/remove-played-comp entity)
      (ct/remove-card entity)))

(defn score-tint? [ct tl ml]
  (and (> ct tl)
       (< ct ml)))

(defn move? [ct ml final]
  (and (> ct ml)
       (< ct final)))

(defn score-card-entity [system entry pos total ct]
  (let [ml (-> total (+ 2) (* duration-freq))
        final (+ ml duration-freq)
        tl (-> entry :order (* duration-freq))
        entity (:entity entry)]
    (cond
      (score-tint? ct tl ml) (tint-card system entity)
      (move? ct ml final) (move-card system entity pos)
      (> ct final) (reset-time-kill-card system entity)
      :else system)))

(defn time-freq? [dt]
  (add-time! dt)
  (> (:time @time-state) time-freq))

(defn score-cards [pos]
  (fn [system delta-time]
    (if (time-freq? delta-time)
      (let [ents (ct/get-all-played-entities system)
            total (count ents)
            ct (:time-counter @time-state)
            f #(score-card-entity %1 %2 pos total ct)]
        (reset-time!)
        (if (> total 0)
          (do
            (add-time-counter! delta-time)
            (->> (for [i (range total)]
                   {:entity (nth ents i)
                    :order (+ i 1)})
                 (reduce f system)))
          system))
      system)))