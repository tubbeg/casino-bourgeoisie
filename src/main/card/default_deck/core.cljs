(ns card.default-deck.core)


(defn def-numbers [s]
  (for [i (range 2 11)]
    {s i}))

(defn default-numbers [suit]
  (->> suit
       (def-numbers)
       (into [])))

(defn default-faceorace [s]
  [{s :jack}
   {s :queen}
   {s :king}
   {s :ace}])

(defn default-cards-suit [s]
  (let [f (default-faceorace s)
        n (default-numbers s)]
    (->> (concat n f)
         (into []))))

(def deck
  (let [c (default-cards-suit :clubs)
        h (default-cards-suit :hearts)
        d (default-cards-suit :diamonds)
        s (default-cards-suit :spades)]
    (->> (concat c h d s)
         (into []))))