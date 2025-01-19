(ns config
  (:require [clojure.spec.alpha :as s]
            ; the spec of jdbc make db/with-connection fail
            ;[clojure.java.jdbc.spec :as jdbc]
            ))

(defn- make-db-config
  [partial]
  (merge {:dbtype "postgresql"
          :host "127.0.0.1"
          :dbname "postgres"
          :user "postgres"
          :password "password"}
         partial))

(def config
  {:db-services (map #(make-db-config %) [{:name "jepsen-tutorial-xa-db1"
                                           :script-name "alice.sql"                                           
                                           :port 55432}
                                          {:name "jepsen-tutorial-xa-db2"
                                           :script-name "bob.sql"
                                           :port 55433}
                                          ])
   :app {:host-port 3001}})


(s/def ::app any?)
;(s/def ::db-services (s/coll-of :clojure.java.jdbc.spec/db-spec-friendly))
(s/def ::db-services (s/coll-of any?))
(s/def ::config
  (s/keys :req-un [::db-services ::app]))

