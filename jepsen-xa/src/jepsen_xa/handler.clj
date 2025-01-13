(ns jepsen-xa.handler
  (:require [ring.adapter.jetty :as jetty]
            [integrant.core :as ig]
            [jepsen-xa.log]
            [jepsen-xa.transaction]
            [jepsen-xa.spec]
            [jepsen-xa.db]            
            [ring.middleware.json :refer [wrap-json-response wrap-json-params]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.util.response :refer [response]]
            [compojure.core :refer [GET defroutes POST]])
  (:gen-class))

(defn load-config
  "See configuration examples."
  [{:keys [instrument db]
    {:keys [app other]} :log/level
    {:keys [join port]} :server}]
  {:jepsen-xa.log/level {:app app :other other}
   :jepsen-xa.spec/instrument {:enable instrument
                               :log (ig/ref :jepsen-xa.log/level)}
   :jepsen-xa.db/specs db
   ::app {:log (ig/ref :jepsen-xa.log/level)
          :transaction (ig/ref :jepsen-xa.transaction/transaction)}
   :jepsen-xa.transaction/transaction (ig/ref :jepsen-xa.db/specs)
   ::server {:port port
             :join join
             :app (ig/ref ::app)}})


(defn make-app-routes
  [transaction]
  (defroutes app-routes
    (GET "/v1/health" [] {:status 200 :body {:status "up"}})
    (POST "/transactions" {{:keys [sender amount]} :params}
      (transaction sender amount)
      {:status 201
       :body {:hi "doge"}})))

(defn -main []
  (ig/init (load-config {})))

(defmethod ig/init-key ::app [_ {:keys [transaction]}]
  (-> (make-app-routes transaction)
      wrap-keyword-params
      wrap-json-params
      wrap-json-response))

(defmethod ig/init-key ::server [_ {:keys [app port join]}]
  (let [server (jetty/run-jetty app {:port port :join? join})]
    server))

(defmethod ig/halt-key! ::server [_ server]
  (.stop server))
