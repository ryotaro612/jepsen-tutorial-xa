(ns jepsen-xa.handler
  (:require [ring.adapter.jetty :as jetty]
            [ring.middleware.json :refer [wrap-json-response wrap-json-params]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.util.response :refer [response]]
            [compojure.core :refer [GET defroutes POST]])
  (:gen-class))

(defroutes app-routes
  (GET "/health" [] "OK")
  (POST "/transactions" a
    (println a)
    {:status 201
     :body {:hi "doge"}}))

(def app
  (-> app-routes
      wrap-keyword-params
      wrap-json-params
      wrap-json-response))

(defn -main []
                      ; :join ? true
                    ; https://github.com/ring-clojure/ring/wiki/Getting-Started
  (jetty/run-jetty app {:port 3000}))

