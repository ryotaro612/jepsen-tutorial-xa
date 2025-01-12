(ns jepsen-xa.handler
  (:require [ring.adapter.jetty :as jetty]
            [compojure.core :refer [GET, defroutes]])
  (:gen-class))


(defn hello
  [x]
  (println x))
(defroutes app-routes
  (GET "/health" [] "OK"))


(defn -main []
                      ; :join ? true
                    ; https://github.com/ring-clojure/ring/wiki/Getting-Started
  (jetty/run-jetty app-routes {:port 3000}))

