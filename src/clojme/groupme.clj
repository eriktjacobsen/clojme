(ns clojme.groupme
  (:refer-clojure :exclude [send])
  (:require [cheshire.core   :as json]
            [clj-http.client :as http]))


(def bot-id "")

(defn api-post [url body headers & {:keys [retry-count] :or {retry-count 0}}]
  (if-let [response (http/post url
                              {:throw-exceptions false
                               :headers headers
                               :body body})]
    (letfn [(try-again [] (if (> retry-count 6)
                            (do
                              (println "Ran out of retries: " url body)
                              nil)
                            (do
                              (Thread/sleep (* (* (* retry-count retry-count retry-count) 0.1) 1000))
                              (api-post url body headers :retry-count (inc retry-count)))))]
    ;(println "Url:     " url)
    ;(println "Body" body)
    ;(println "Headers: " headers)
    (cond
      (= (:status response) 200) (:body response)
      (= (:status response) 202) (:body response)
      (= (:status response) 400) (:body response)
      (= (:status response) 500)
          (do (println "Unknown 500: " (:body response)) (try-again))
      :else (do (println (str "Non-200: [" (:status response) "] " response)) (try-again))
    ))
    false))


(defn send [message]
  (when (and (not (nil? message)) (not (empty? message)))
    (do
      (println "Would send: " message)
      (api-post "https://api.groupme.com/v3/bots/post"
                (json/generate-string {:bot_id bot-id :text (str message)})
                  nil))))
