(ns clojme.message
  (:require [cheshire.core :as json]
            [clj-time.core :as joda]
            [clj-time.coerce :as jout]
            [clojme.groupme :as groupme]))

(def places   (atom {}))
(def people   (atom {}))
(def keywords (atom {}))

(defn fileswap! [name & args]
  (spit (str "/tmp/" name ".cloj") (apply swap! args)))

(defn swap-places [places place person]
  (let [checkin {:name person :time (jout/to-long (joda/now))}
        removed (into {} (for [[k v] places] [k (remove #(= person (:name %1)) v)]))]
    (assoc removed place (conj (get removed place) checkin))))

(defn load-atoms []
  (try
    (do
      (reset! places (read-string (slurp "/tmp/places.cloj")))
      (reset! people (read-string (slurp "/tmp/people.cloj")))
      (reset! keywords (read-string (slurp "/tmp/keywords.cloj"))))
    (catch Exception e (str "caught exception: " (.getMessage e)))))

(defn trim [line]
  (if (string? line)
    (clojure.string/trim line)
    line))

(defn lc [line]
  (if (string? line)
    (clojure.string/lower-case line)
    line))

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
          (fileswap! "keywords" keywords assoc macro content)
          ;(groupme/send (str "Storing [" macro "] as value: [" content"]"))
          )
      ))))

(defn store-locations [text user]
  (let [matches (re-find #"\|(\w*)" text)
        place   (lc (trim (second matches)))]
    (when (not (nil? matches))
      (fileswap! "places" places swap-places place user)
      (fileswap! "people" people assoc user place)
      )))

(defn since [datetime]
  (let [min  (joda/in-minutes (joda/interval (jout/from-long datetime) (joda/now)))
        hour (joda/in-hours   (joda/interval (jout/from-long datetime) (joda/now)))]
    (if (> min 60)
      (str hour "h")
      (str min "m"))
   ))

(defn format-place [coll]
  (let [name   (first coll)
        people (last coll)
        old-removed (remove #(joda/before? (jout/from-long (:time %1)) (joda/minus (joda/now) (joda/hours 2))) people)]
    (when (> (count old-removed) 0)
      (str name ": "
         (clojure.string/join ", " (map #(str (shorten-name (:name %1)) "(" (since (:time %1)) ")") old-removed))
         "\n"))))


(defn check-locations [text user]
    (if (re-find #"^whosout" text)
      (dorun (map #(groupme/send (format-place %)) @places))
      ;(dorun (map #(groupme/send (str (shorten-name (first %)) " is at: " (last %))) @people))
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
      "Leave me alone!"
      ))))

(defn display []
  (map format-place @places))
