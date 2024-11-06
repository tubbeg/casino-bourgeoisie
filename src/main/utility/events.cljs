(ns utility.events 
  (:require ["phaser$Events" :refer (EventEmitter)]))

(def eventEmitter (new EventEmitter))

(def ui-message "input-state")
(def card-message "reset-input")
(def remaining-cards-in-deck "deck-rem")

;eventsCenter.emit('update-count', this.count)
(defn emit-event [event-emitter message data]
  (. event-emitter (emit message data)))

(defn add-on-event [event-emitter message function]
  (. event-emitter (on message function)))

(defn add-event-listener! [msg function] 
  (add-on-event eventEmitter msg function))

(defn emit-event! [message data]
  (emit-event eventEmitter message data))