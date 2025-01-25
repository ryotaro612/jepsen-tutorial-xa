(ns config
  (:require [clojure.spec.alpha :as s]))

(defn- make-db-config
  [partial]
  (merge {:dbtype "postgresql"
          :host "127.0.0.1"
          :dbname "postgres"
          :user "postgres"
          :password "password"}
         partial))

(def config
  "The configuration for the server, databases and client."
  {:db-services (map #(make-db-config %) [{:container-name "jepsen-tutorial-xa-db1"
                                           :script-name "alice.sql"                                           
                                           :port 55432}
                                          {:container-name "jepsen-tutorial-xa-db2"
                                           :script-name "bob.sql"
                                           :port 55433}])
   :app {:host-port 3001 :container-name "jepsen-tutorial-xa-app"}})


(s/def ::app any?)
;(s/def ::db-services (s/coll-of :clojure.java.jdbc.spec/db-spec-friendly))
(s/def ::db-services (s/coll-of any?))
(s/def ::config
  (s/keys :req-un [::db-services ::app]))

