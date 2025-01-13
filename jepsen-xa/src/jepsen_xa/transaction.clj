(ns jepsen-xa.transaction
  (:require [integrant.core :as ig]
            [clojure.spec.alpha :as s]
            [jepsen-xa.balance :as b]
            [jepsen-xa.log]
            [slingshot.slingshot :refer [throw+ try+]]
            [jepsen-xa.db :as db]
            [jepsen-xa.log :as l]))

(s/def ::sender #{"alice" "bob"})
(s/def ::amount #(and (int? %) (>= % 0)))
(s/def ::alice int?)
(s/def ::bob int?)
(s/def ::compute-delta-ret (s/keys :req-un [::alice ::bob]))

(defn- compute-delta
  [sender amount]
  (if (= sender "alice")
    {:alice (- amount) :bob amount}
    {:alice amount :bob (- amount)}))

(s/fdef compute-delta
  :args (s/cat :sender ::sender
               :amount ::amount)
  :ret ::compute-delta-ret)

(defn- transaction!
  [{:keys [logger db-spec1 db-spec2 balance-update] :as deps} sender amount]
  (l/debug logger {:message "transaction!"
                   :sender sender
                   :amount amount})
  (let [{:keys [alice bob]} (compute-delta sender amount)
        transaction-id (db/generate-transaction-id)]
    (db/with-connection [alice-conn db-spec1]
      (db/begin! alice-conn)
      (l/debug logger {:transaction-id transaction-id
                       :message "alice begin"})
      (try+
       (b/add-balance balance-update alice-conn "alice" alice)
       (l/debug logger {:transaction-id transaction-id
                        :message "alice add balance"})
       (db/prepare-transaction! alice-conn transaction-id)
       (l/debug logger {:transaction-id transaction-id
                        :message "alice prepare-transaction"})
       (catch Object _
         (db/rollback! alice-conn)
         (throw+)))
      (db/with-connection [bob-conn db-spec2]
        (try+
         (db/begin! bob-conn)
         (l/debug logger {:transaction-id transaction-id
                          :message "bob begin"})
         (catch Object e
           (l/error logger {:transaction-id transaction-id
                            :message "bob begin"
                            :error e})
           (db/rollback! alice-conn)
           (throw+)))
        (try+
         (b/add-balance balance-update bob-conn "bob" bob)
         (db/prepare-transaction! bob-conn transaction-id)
         (l/debug logger {:transaction-id transaction-id
                          :user "bob"
                          :message "prepare-transaction"})
         (catch Object _
           (try+
            (db/rollback! bob-conn)
            (l/debug logger {:transaction-id transaction-id
                             :user "bob"
                             :message "rollback"})
            (finally
              (db/rollback-prepared! alice-conn transaction-id)))
           (throw+)))
        (try+
         (db/commit-prepared! alice-conn transaction-id)
         (l/debug logger {:transaction-id transaction-id
                          :user "alice"
                          :message "commit prepared"})
         (finally
           (db/commit-prepared! bob-conn transaction-id)
           (l/debug logger {:transaction-id transaction-id
                            :user "bob"
                            :message "commit prepared"})))))))

(s/def ::db-spec1 #(map? %))
(s/def ::db-spec2 #(map? %))
(s/def ::transaction-deps
  (s/keys :req-un [:jepsen-xa.log/logger :jepsen-xa.balance/balance-update ::db-spec1 ::db-spec2]))
(s/def ::transaction-ret #(or (= {:error :invalid-arguments} %) (nil? %)))
(s/fdef transaction!
  :args (s/cat :deps ::transaction-deps
               :sender ::sender
               :amount ::amount)
  :ret ::transaction-ret)

(defmethod ig/init-key ::transaction [_ {:keys [logger balance-update]
                                         {:keys [db-spec1 db-spec2]} :db-specs}]
  #(if (or (s/explain-data ::sender %1) (s/explain-data ::amount %2))
     (do
       (l/debug logger {:message "transaction!"
                        :sender %1
                        :amount %2
                        :error "invalid input"})
       {:error :invalid-arguments})
     (transaction! {:logger logger
                    :balance-update balance-update
                    :db-spec1 db-spec1
                    :db-spec2 db-spec2}
                   %1
                   %2)))
