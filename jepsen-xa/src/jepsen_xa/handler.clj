(ns jepsen-xa.handler
  (:require [ring.adapter.jetty :as jetty]
            [integrant.core :as ig]
            [jepsen-xa.boundary.log]
            [clojure.spec.alpha :as s]            
            [jepsen-xa.log :as log]
            [jepsen-xa.transaction]
            [jepsen-xa.boundary.balance]
            [jepsen-xa.spec]
            [jepsen-xa.boundary.db]
            [ring.middleware.json :refer [wrap-json-response wrap-json-params]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.util.response :refer [response]]
            [compojure.core :refer [GET defroutes POST]])
  (:gen-class))

(defn load-config
  "Examples are in handler_test."
  [{:keys [instrument db]
    {:keys [app other]} :log/level
    {:keys [join port]} :server}]
  {:jepsen-xa.boundary.log/level {:app app :other other}
   :jepsen-xa.spec/instrument {:enable instrument
                               :logger (ig/ref :jepsen-xa.boundary.log/level)}
   :jepsen-xa.boundary.db/specs db
   ::app {:log (ig/ref :jepsen-xa.boundary.log/level)
          :transaction (ig/ref :jepsen-xa.transaction/transaction)}
   :jepsen-xa.transaction/transaction {:db-specs (ig/ref :jepsen-xa.boundary.db/specs)
                                       :logger (ig/ref :jepsen-xa.boundary.log/level)
                                       :balance-update (ig/ref :jepsen-xa.boundary.balance/update)}
   :jepsen-xa.boundary.balance/update (ig/ref :jepsen-xa.boundary.log/level)
   ::server {:port port
             :join join
             :app (ig/ref ::app)}})


(defn make-app-routes
  [transaction]
  (defroutes app-routes
    (GET "/v1/health" [] {:status 200 :body {:status "up"}})
    (POST "/transactions" {{:keys [sender amount]} :params}
      (let [result (transaction sender amount)]
        (cond
          (= result {:error :invalid-arguments}) {:status 400 :body {:message "invalid arguments"}}
          (nil? result) {:status 201 :body {:message "success"}}
          :else {:status 500 :body {:message "internal server error"}})))))

(s/def ::transaction
  (s/fspec :args (s/cat :sender :jepsen-xa.transaction/sender
                        :amount :jepsen-xa.transaction/amount)))

(s/fdef make-app-routes
  :args (s/cat :transaction ::transaction))

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

(defn -main []
  (let [port (parse-long (or (System/getenv "PORT") "3000"))
        db1-host (or (System/getenv "DB1_HOST") "127.0.0.1")
        db1-port (parse-long (or (System/getenv "DB1_PORT") "5432"))
        db2-host (or (System/getenv "DB2_HOST") "127.0.0.1")
        db2-port (parse-long (or (System/getenv "DB2_PORT") "5432"))
        app-log-level (log/log-level (System/getenv "APP_LOG_LEVEL") :info)
        other-log-level (log/log-level (System/getenv "OTHER_LOG_LEVEL") :info)]
    (ig/init
     (load-config
      {:log/level {:app app-log-level :other other-log-level}
       :instrument false
       :server {:port port
                :join true}
       :db {:db1 {:host db1-host :port db1-port}
            :db2 {:host db2-host :port db2-port}}}))))
