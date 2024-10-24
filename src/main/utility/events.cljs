(ns utility.events 
  (:require ["phaser$Events" :refer (EventEmitter)]))

(def eventEmitter (new EventEmitter))

;eventsCenter.emit('update-count', this.count)
(defn emit-event [event-emitter message data]
  (. event-emitter (emit message data)))

(defn add-on-event [event-emitter message function]
  (. event-emitter (on message function)))