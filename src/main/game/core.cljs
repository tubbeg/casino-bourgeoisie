(ns game.core
  (:require ["phaser" :refer (Scene AUTO Game)]
            [clojure.core.async :as a]
            [goog.object :as gobj]
            [cljs.core.async.interop :refer-macros [<p!]]
            [utility.core :as ut]
            [macro.core :as m]
            [card-scene.core :as scene]))

(println "hello hello hello")


(def y #js{:y 200})
(def arcade #js{:gravity y})
(def physics #js{:default "arcade"
                 :arcade arcade})

(def config #js{:type AUTO
                :width 800
                :height 600,
                :scene scene/CardScene
                :physics physics})

(def app (new Game config))