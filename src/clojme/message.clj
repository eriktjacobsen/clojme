(ns clojme.message
  (:require [cheshire.core :as json]
            [clj-time.core :as joda]
            [clojme.groupme :as groupme]))

(def places   (atom {"Vertigo" {"Rob" 1 "Ryan" 5} "McTegues" {"August" 5 "Jackie" 10}}))
(def people   (atom {"Curveball" "vertigo"}))
(def keywords (atom {}))

(defn trim [line]
  (if (string? line)
    (clojure.string/trim line)
    line))

(defn lc [line]
  (if (string? line)
    (clojure.string/lower-case line)
    line))

(defn swap-places [places place person]
  (assoc places place (assoc (get places place) person (joda/now))))

(defn shorten-name [name]
  (subs name 0 (min 15 (count name))))

(defn handle-keywords [text]
  (let [matches (re-find #"^~(\w*) ?(.*)" text)
        macro   (lc (trim (second matches)))
        content (trim (get matches 2))]
    (when (not (nil? matches))
      (if (= content "")
        (groupme/send (get @keywords macro))
        (do
          (swap! keywords assoc macro content)
          (groupme/send (str "Storing [" macro "] as value: [" content"]")))
      ))))

(defn store-locations [text user]
  (let [matches (re-find #"\|(\w*)" text)
        place   (lc (trim (second matches)))]
    (when (not (nil? matches))
      (swap! places swap-places place user)
      (swap! people assoc user place)
      )))

(defn check-locations [text user]
    (if (re-find #"^whosout" text)
      (dorun (map #(groupme/send (str (shorten-name (first %)) " is at: " (last %))) @people))
      (store-locations text user)))

(defn handle [message]
  (let [parsed (json/parse-string message)
        user   (get parsed "name")
        text   (get parsed "text")]
  (when (not (= user "curvebot"))
    (do
      (println "keywords: " @keywords)
      (println "people:   " @people)
      (println "places:   " @places)

      (handle-keywords text)
      (check-locations text user)

      (println "keywords: " @keywords)
      (println "people:   " @people)
      (println "places:   " @places)
      ))))


