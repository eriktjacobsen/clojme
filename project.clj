(defproject clojme "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [compojure "1.1.6"]
                 [cheshire "5.2.0"]
                 [clj-http "0.7.8"]
                 ]
  :plugins [[lein-ring "0.8.8"]]
  :ring {:handler clojme.handler/app}
  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring-mock "0.1.5"]]}})
