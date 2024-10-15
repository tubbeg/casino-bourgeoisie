(ns utility.core
  (:require
   [clojure.core.async :as a]
   [cljs.core.async.interop :refer-macros [<p!]]))


(defn set-base-url! [this url]
  (.. this -load (setBaseURL url)))

(defn load-image! [this id url]
  (.. this -load (image id url)))

(defn add-image! [this x y id]
  (println "Adding image" id)
  (.. this -add (image x y id)))


