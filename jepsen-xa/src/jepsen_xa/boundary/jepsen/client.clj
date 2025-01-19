(ns jepsen-xa.boundary.jepsen.client
  (:require [integrant.core :as ig]
            [jepsen-xa.log :as log]
            [jepsen-xa.balance :as balance]
            [clojure.spec.alpha :as s]
            [jepsen
             [client :as client]]))

(defrecord ClientGateway
    [nodes logger lookup db-spec1 db-spec2 transfer]
  client/Client
  (open! [this test node]
    (log/debug logger {:message ""
                       :func "open!"
                       :node node})
    this)
 
  (setup! [_ test])

  (invoke! [_ test op]
    (log/debug logger {:message ""
                       :func "invoke!"
                       :op op})
    (case (:f op)
      :read-alice (assoc op :type :ok, :value (balance/lookup lookup db-spec1 "alice"))
      :read-bob (assoc op :type :ok, :value (balance/lookup lookup db-spec2 "bob"))
      :transfer (let [{{:keys [sender amount]} :value} op]
                  (assoc op :type :ok, :value (balance/transfer transfer sender amount)))))

  (teardown! [this test])

  (close! [_ test]))

(s/fdef ClientGateway
  :args (s/cat :nodes any?
               :logger any?
               :lookup any?
               :db-spec1 any?
               :db-spec2 any?)
  ;:ret
  )

(defmethod ig/init-key ::client
  [_ {:keys [nodes logger lookup db-spec1 db-spec2 transfer]}]
  (log/debug logger {:message "init a jepsen client"
                     :nodes nodes})
  (map->ClientGateway {:nodes nodes
                       :logger logger
                       :lookup lookup
                       :db-spec1 db-spec1
                       :db-spec2 db-spec2}))
