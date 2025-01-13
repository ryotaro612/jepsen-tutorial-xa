(ns jepsen-xa.transaction
  (:require [integrant.core :as ig]
            [clojure.spec.alpha :as s]
            [jepsen-xa.balance :as b]
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
  [{:keys [logger db-spec1 db-spec2 balance-update]} sender amount]
  (l/debug logger {:message "transaction!"
                   :sender sender
                   :amount amount})
  (let [{:keys [alice bob]} (compute-delta sender amount)
        transaction-id (db/generate-transaction-id)]
    (db/with-connection [alice-conn db-spec1]
      (db/begin! alice-conn)      
      (try+
       (balance-update alice-conn "alice" alice)
       (catch Object _
         (db/rollback! alice-conn)
         (throw+)))
      (db/with-connection [bob-conn db-spec2]
        (try+
         (db/begin! bob-conn)
         (catch Object _
           (db/rollback! alice-conn)
           (throw+)))
        (try+
         (balance-update bob-conn "bob" bob)
         (catch Object _
           (try+
            (db/rollback! bob-conn)
            (finally
              (db/rollback-prepared! alice-conn transaction-id)))
           (throw+)))
        (try+
         (db/commit-prepared! alice-conn transaction-id)
         (finally
          (db/commit-prepared! bob-conn transaction-id)))))))

(s/def ::balance-update #(satisfies? b/BalanceUpdate %))
(s/def ::transaction-deps
  (s/keys :req-un [::balance-update]))
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
