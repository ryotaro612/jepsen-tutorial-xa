(ns jepsen-xa.boundary.db
  (:require [integrant.core :as ig]
            [clojure.spec.alpha :as s]))

(s/def ::host string?)
(s/def ::port int?)

(s/def ::spec-arg
  (s/keys :req-un [::host ::port]))

(defn- make-spec
  [partial]
  (merge   {:dbtype "postgresql"
   :dbname "postgres"
   :user "postgres"
            :password "password"}
           partial))

(s/fdef make-spec
  :args (s/cat :partial ::spec-arg)
  :ret map?)

(defmethod ig/init-key ::specs [_ {:keys [db1 db2]}]
  {:db-spec1 (make-spec db1)
   :db-spec2 (make-spec db2)})
