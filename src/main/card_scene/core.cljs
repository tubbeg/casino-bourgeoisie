(ns card-scene.core
  (:require ["phaser" :refer (Scene)]
            [goog.object :as gobj]
            [utility.core :as u]
            [utility.core :as ut]))

(defn CardScene
  {:jsdoc ["@constructor"]}
  []
  (this-as this
           ;(.call Scene this "my-args-here") 
           (.call Scene this)
           this))

(defn preld [this]
  (ut/set-base-url! this "./assets")
  (ut/load-image! this "background" "background.png")
  (ut/load-image! this "king" "KINS/Clubs_K.png"))

(defn creat [this]
  (ut/add-image! this 400 300 "background")
  (ut/add-image! this 400 500 "king"))

(defn create-scene []
  (this-as this (creat this)))

(defn preload-scene []
  (this-as this (preld this)))

(defn update-scene []
  )

(gobj/extend
 (.-prototype CardScene)
  (.-prototype  Scene)
  #js {:preload preload-scene}
  #js {:create create-scene}
  #js {:update update-scene})