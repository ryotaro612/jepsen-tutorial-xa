(ns config
  (:require [clojure.spec.alpha :as s]))

(def config
  {:db-services [{:name "jepsen-tutorial-xa-db1" :script-name "alice.sql" :host "127.0.0.1" :port 55432}
                 {:name "jepsen-tutorial-xa-db2" :script-name "bob.sql" :host "127.0.0.1" :port 55433}]
   :app {:host-port 3001}})

; TODO
(s/def ::config
  any?)




