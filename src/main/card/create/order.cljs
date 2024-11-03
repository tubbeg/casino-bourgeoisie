(ns card.create.order
   (:require [utility.core :as ut]
             [brute.entity :as e]
             [card.types :as t]
             [card.create.utility :as ct]))


 (defn log-result [result message]
   (println message result)
   result)

 (defn selected? [system entity]
   (-> (ct/get-sel-comp system entity)
       (nil?)
       (not)))

 (defn overlap-left? [world entity]
   (let [slot (ct/get-slot-comp world entity)
         sprite (-> world (ct/get-sprite-comp entity) (:sprite))
         [origin-x _] (:pos slot)
         padding 5
         w (-> sprite (.-width) (/ 2))
         x (.-x sprite)]
     (< x (- origin-x w))))

 (defn overlap-right?  [world entity]
   (let [slot (ct/get-slot-comp world entity)
         sprite (-> world (ct/get-sprite-comp entity) (:sprite))
         [origin-x _] (:pos slot)
         padding 5
         w (-> sprite (.-width) (/ 2))
         x (.-x sprite)]
     (> x (+ origin-x w))))

 (defn entity-has-matching-slot-nr [world entity nr]
   (->
    world
    (ct/get-slot-comp entity)
    (:order)
    (= nr)))

 (defn next-entity [nr world]
   (let [e (e/get-all-entities-with-component
            world t/SlotComponent)
         f (filter #(entity-has-matching-slot-nr world % nr) e)]
     (first f)))

 (defn clip-one [n]
   (if (< n 1) 1 n))

 (defn clip-max [n max]
   (if (> n max) max n))

 (defn next-slot-right [world entity]
   (let [slot (ct/get-slot-comp world entity)
         max (:max slot)]
     (-> slot (:order) (+ 1) (clip-max max))))

 (defn next-slot-left [world entity]
   (let [slot (ct/get-slot-comp world entity)]
     (-> slot (:order) (- 1) (clip-one))))

 (defn not-dragging? [system entity]
   (-> system
       (ct/get-drag-comp entity)
       (nil?)))

 (defn get-tweens [world]
   (let [ents (e/get-all-entities-with-component
               world t/TweensComponent)]
     (-> world
         (e/get-component (first ents) t/TweensComponent)
         (:tweens))))

 (defn add-card-tween! [world entity [x y] duration]
   (let [sprite (-> world
                    (ct/get-sprite-comp entity)
                    (:sprite))
         tweens (get-tweens world)]
     (when (ut/not-nil? tweens)
       (ut/add-sprite-tween! tweens sprite x y duration))))

 (defn in-range? [n comp margin]
   (and (> n (- comp margin))
        (< n (+ comp margin))))

 (defn almost-identical-position? [pos1 pos2]
   (let [base-margin 4
         [x1 y1] pos1
         [x2 y2] pos2]
     (or
      (and (in-range? x1 x2 base-margin)
           (in-range? y1 y2 base-margin))
      (and (= x1 x2)
           (= y1 y2)))))

 (defn entity-at-wrong-position? [world entity pos]
   (let [s (-> world
               (ct/get-sprite-comp entity)
               (:sprite))
         sx (.-x s)
         sy (.-y s)
         slot (ct/get-slot-comp world entity)
         rank (-> world (ct/get-rank-comp entity) :rank)
         suit (-> world (ct/get-suit-comp entity) :suit)
         msg "Wrong position?"
         result (-> (almost-identical-position? [sx sy] pos)
                    (not))]
    ;(when result
    ;  (println "Rank: " rank "Suit: " suit "Target pos: " pos)
      ;(check-log-state rank suit [sx sy] pos)
     ; )
     result))

 (defn sprite-has-no-tween? [tween-manager sprite]
   (-> tween-manager (ut/sprite-has-tween? sprite) not))

 (defn no-tweens-active? [tweens-manager]
   (-> tweens-manager (ut/get-all-tweens-tm) (count) (= 0)))

 (defn move? [world entity pos]
   (let [tweens (get-tweens world)
         s (-> world (ct/get-sprite-comp entity) :sprite)]
     (and (not-dragging? world entity)
          (entity-at-wrong-position? world entity pos)
        ;(no-tweens-active? tweens)
          (sprite-has-no-tween? tweens s))))

 (defn randomize-x-slightly [x]
   (let [low (- x 0.5)
         high (+ x 0.5)
         lower (- x 1)
         higher (+ x 1)
         coll [low high x lower higher]]
     (-> (rand-nth coll))))

 (defn calc-pos [world entity]
   (let [slot (ct/get-slot-comp world entity)
         [x y] (:pos slot)
         sw (-> world
                (ct/get-sprite-comp entity)
                (:sprite)
                (.-height)
                (/ 3))
         adj-y (if (selected? world entity) (- y sw) y)]
     [(randomize-x-slightly x) adj-y]))

 (defn move-card! [world entity]
   (let [pos (calc-pos world entity)
        ;[x y] pos
        ;tweens (get-tweens world)
        ;s (ct/get-sprite-comp world entity)
         ]
     (when (move? world entity pos)
      ;(ut/set-xy-object! (:sprite s) x y)
       (add-card-tween! world entity pos 150))))


 (defn swap-left [system entity]
   (-> system
       (next-slot-left entity)
       (next-entity system)
       (ct/swap-slots system entity)))

 (defn swap-right [system entity]
   (-> system
       (next-slot-right entity)
       (next-entity system)
       (ct/swap-slots system entity)))

 (def has-sorted-state (atom {:sorted false}))

 (defn swap-if-overlap [system entity]
   (cond 
     (:sorted @has-sorted-state) system
     (overlap-right? system entity) (swap-right system entity)
     (overlap-left? system entity) (swap-left system entity)
     :else system))

 (def time-state (atom {:time 0}))

 (defn reset-sorted-state! []
   (swap! has-sorted-state #(assoc % :sorted false)))

 (defn add-time! [add]
   (let [nxt-time (-> @time-state :time (+ add))]
     (swap! time-state #(assoc % :time nxt-time))))

 (defn run? [limit]
   (> (:time @time-state) limit))

 (defn order-deck [system delta-time]
   (loop [s  system
          ents (ct/get-all-slot-entities system)]
     (if (ut/zero-coll? ents)
       s
       (let [f (first ents)
             rem (next ents)]
         (move-card! s f)
         (-> (swap-if-overlap s f)
             (recur rem))))))

(defn order-cards [system dt]
  (add-time! dt)
  (if (run? 300)
    (let [s (order-deck system dt)]
      (when (:sorted @has-sorted-state)
        (reset-sorted-state!))
      s)
    system))
