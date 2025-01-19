(ns jepsen-xa.boundary.jepsen.client
  (:require [integrant.core :as ig]
            [jepsen-xa.log :as log]
            [clojure.spec.alpha :as s]
            
            [jepsen
             [client :as client]]))

(defrecord ClientGateway [nodes logger lookup]
  client/Client
  (open! [this test node]
    (log/debug logger {:message ""
                       :func "open!"
                       :node node})
    this)
 
  (setup! [_ test])

  (invoke! [_ test op]

    )

  (teardown! [this test])

  (close! [_ test]))


(defmethod ig/init-key ::client [_ {:keys [nodes logger lookup]}]
  (log/debug logger {:message "init client"
                     :nodes nodes})
  (map->ClientGateway {:nodes nodes :logger logger :lookup lookup}))

