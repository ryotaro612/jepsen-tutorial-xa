(ns jepsen-xa.boundary.jepsen.client
  (:require [integrant.core :as ig]
            [jepsen-xa.log :as log]
            [taoensso.timbre :as timbre]
            [jepsen-xa.balance :as balance]
            [clojure.spec.alpha :as s]
            [jepsen
             [client :as client]]))

(defrecord ClientGateway
    [nodes lookup db-spec1 db-spec2 transfer]
  client/Client
  (open! [this test node]
    this)
 
  (setup! [_ test])

  (invoke! [_ test op]
    (case (:f op)
      :read-alice (assoc op :type :ok, :value (balance/lookup lookup db-spec1 "alice"))
      :read-bob (assoc op :type :ok, :value (balance/lookup lookup db-spec2 "bob"))
      :transfer (let [{{:keys [sender amount]} :value} op]
                  (timbre/debug "transfer" {:sender sender
                                            :amount amount})
                  (assoc op :type :ok, :value
                         (merge {:sender sender
                                 :amount amount}
                                (balance/transaction transfer sender amount))))))

  (teardown! [this test])

  (close! [_ test]))

(s/fdef map->ClientGateway
  :args (s/cat :nodes any?
               :lookup any?
               :db-spec1 any?
               :db-spec2 any?
               :transfer :jepsen-xa.balance/transfer)
  ;:ret
  )

(defmethod ig/init-key ::client
  [_ {:keys [nodes logger lookup db-spec1 db-spec2 transfer]}]
  (log/debug logger {:message "init a jepsen client"
                     :nodes nodes})
  (map->ClientGateway {:nodes nodes
                       :lookup lookup
                       :db-spec1 db-spec1
                       :db-spec2 db-spec2
                       :transfer transfer}))
