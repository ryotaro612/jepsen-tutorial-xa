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
    :keys [instrument]
    {:keys [join port]} :server}]
  {:jepsen-xa.log/level {:app app :other other}
   :jepsen-xa.spec/instrument {:enable instrument
                               :log (ig/ref :jepsen-xa.log/level)}
   ::app {:log (ig/ref :jepsen-xa.log/level)}
   ::server {:port port
             :join join
             :app (ig/ref ::app)}})

(defroutes app-routes
  (GET "/v1/health" [] {:status 200 :body {:status "up"}})
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

(defmethod ig/init-key ::app [_ _]
  app)

(defmethod ig/init-key ::server [_ {:keys [app port join]}]
  (let [server (jetty/run-jetty app {:port port :join? join})]
    server))

(defmethod ig/halt-key! ::server [_ server]
  (.stop server))
