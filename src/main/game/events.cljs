(ns game.events 
  (:require ["phaser$Events" :refer (EventEmitter)]))

(def eventEmitter (new EventEmitter))