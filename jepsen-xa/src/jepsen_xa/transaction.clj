(ns jepsen-xa.transaction
  (:require [integrant.core :as ig]
            [clojure.spec.alpha :as s]
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
  [sender amount logger db-spec1 db-spec2]
  (l/debug logger {:message "transaction!"
                   :sender sender
                   :amount amount})
  (let [{:keys [alice bob]} (compute-delta sender amount)]
    ))

(s/def ::transaction-ret #(or (= {:error :invalid-arguments} %) (nil? %)))

(s/fdef transaction!
  :args (s/cat :sender ::sender
               :amount ::amount
               :logger any?
               :db-spec1 any?
               :db-spec2 any?)
  :ret ::transaction-ret)


(defmethod ig/init-key ::transaction [_ {:keys [logger] {:keys [db-spec1 db-spec2]} :db-specs}]
  #(if (or (s/explain-data ::sender %1) (s/explain-data ::amount %2))
     (do
       (l/debug logger {:message "transaction!"
                        :sender %1
                        :amount %2
                        :error "invalid input"})
       {:error :invalid-arguments})
     (transaction! %1 %2 logger db-spec1 db-spec2)))
