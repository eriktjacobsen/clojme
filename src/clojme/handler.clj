(ns clojme.handler
  (:use compojure.core)
  (:require [compojure.handler :as handler]
            [clojme.message :as msg]))

(defroutes app-routes
  (POST "/clojme" request (msg/handle (slurp (:body request))))
  (GET "/whosout" [] (msg/display))
  (GET "/" [] "Go Away!"))

(def app
  (handler/site app-routes))

(msg/load-atoms)
