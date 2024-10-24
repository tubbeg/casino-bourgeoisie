(ns game.core
  (:require ["phaser" :refer (Scene AUTO Game Sprite)]
            [clojure.core.async :as a]
            [goog.object :as gobj]
            [cljs.core.async.interop :refer-macros [<p!]]
            [utility.core :as ut]
            [card.scene :as c]
            [ui.scene :as ui]))

(println "hello hello hello")

(def root (. js/document (createElement "div")))
(.. js/document -body (appendChild root))

(def y #js{:y 200})
(def arcade #js{:gravity y})
(def physics #js{:default "arcade"
                 :arcade arcade})

(def scenes
   (array   ui/ui-scene 
            c/card-scene))

(println scenes)

(def target #js{:target root})
(def input #js{:mouse target
               :touch target})
(def dom #js{:createContainer true})
(def config #js{:type AUTO
                :width 800
                :height 600,
                :scene scenes
                :physics physics
                :dom dom
                :parent root
                :input input})

(def app (new Game config))