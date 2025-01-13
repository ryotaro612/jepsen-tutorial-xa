(ns jepsen-xa.client-test
  (:require [clojure.test :refer :all]
            [integrant.core :as ig]
            [jepsen-xa.client :refer :all]))


(deftest test-load-config
  (testing "This configuration can be used in REPL."
    (is (=   {:jepsen-xa.boundary.log/level {:app :debug :other :info}
              :jepsen-xa.spec/instrument true}
             
           (load-config {:logger {:app :debug :other :info}
                         :instrument true
                         :db-specs {:db1 {:port 55432 :host "127.0.0.1"}
                                    :db2 {:port 55433 :host "127.0.0.1"}}}))
        "Failure"
        )))
