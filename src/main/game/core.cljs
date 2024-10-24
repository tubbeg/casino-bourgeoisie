(ns game.core
  (:require ["phaser" :refer (Scene AUTO Game Sprite)]
            [clojure.core.async :as a]
            [goog.object :as gobj]
            [cljs.core.async.interop :refer-macros [<p!]]
            [utility.core :as ut]
            [card.scene :as c]
            [ui.scene :as ui]))


(println "hello hello hello")


(def wx (.. js/window -screen -width))
(def wy (.. js/window -screen -height))
(def awx (.. js/window -screen -availWidth))
(def awy (.. js/window -screen -availHeight))


(def root (. js/document (createElement "div")))
(.. js/document -body (appendChild root))

(def class-name "divClass")
(def properties "display: flex;
                  justify-content: center;
                  align-items: center;")
(defn css-div [n]
  (str "." n "{" properties "}"))

(defn create-css-class! [inner-html]
  (let [el (. js/document (createElement "style"))]
    (set! (.-type el) "text/css")
    (set! (.-innerHTML el) inner-html)
    (.. js/document -head (appendChild el))))

(defn add-class-to-element! [element class]
  (.. element -classList (add class)))
(create-css-class! (css-div class-name))
(add-class-to-element! root class-name)


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
                :width (/ awx 1.1)
                :height (/ awy 1.1)
                :scene scenes
                :physics physics
                :dom dom
                :parent root
                :input input})

(def app (new Game config))