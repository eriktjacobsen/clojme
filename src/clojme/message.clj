(ns clojme.message
  (:require [cheshire.core :as json]
            [clojme.groupme :as groupme]))

(def places (atom {}))

(defn handle [message]
  (let [parsed (json/parse-string message)]
    (println parsed)))
