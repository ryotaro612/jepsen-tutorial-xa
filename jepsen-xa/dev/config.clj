(ns config
  (:require [clojure.spec.alpha :as s]
            [clojure.java.jdbc.spec :as jdbc]))
(def config
  {:db-services [{:name "jepsen-tutorial-xa-db1"
                  :script-name "alice.sql"
                  :host "127.0.0.1"
                  :port 55432
                  :dbname "postgres"
                  :password "password"}
                 {:name "jepsen-tutorial-xa-db2"
                  :script-name "bob.sql"
                  :host "127.0.0.1"
                  :port 55433
                  :dbname "postgres"
                  :password "password"}]
   :app {:host-port 3001}})


(s/def ::app any?)
(s/def ::db-services (s/coll-of :clojure.java.jdbc.spec/db-spec-friendly))
(s/def ::config
  (s/keys :req-un [::db-services ::app]))

