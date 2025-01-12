(ns jepsen-xa.handler
  (:require [ring.adapter.jetty :as jetty]
            [integrant.core :as ig]
            [ring.middleware.json :refer [wrap-json-response wrap-json-params]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.util.response :refer [response]]
            [compojure.core :refer [GET defroutes POST]])
  (:gen-class))

(defn load-config
  "See configuration examples."
  [{{:keys [app other]} :log/level
    :keys [instrument port]}]
  {:jepsen-xa.log/level {:app app :other other}
   :jepsen-xa.spec/instrument {:enable instrument :log (ig/ref :jepsen-xa.log/level)}})

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
  (ig/init (load-config {})))
                     ; :join ? true
                    ; https://github.com/ring-clojure/ring/wiki/Getting-Started
; #_(jetty/run-jetty app {:port 3000}
