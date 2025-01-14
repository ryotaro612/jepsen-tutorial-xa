(ns jepsen-xa.handler-test
  (:require [clojure.test :refer :all]
            [integrant.core :as ig]
            [jepsen-xa.handler :refer :all]))
                                        ; cider-test c-c c-t c-p
; run cider-refresh if something is wrong
(deftest show-config-examples
  (testing "This configuration can be used in REPL."
    (is (= {:jepsen-xa.boundary.log/level {:app :debug :other :debug},
            :jepsen-xa.spec/instrument {:enable true
                                        :logger (ig/ref :jepsen-xa.boundary.log/level)}
            :jepsen-xa.handler/app {:log (ig/ref :jepsen-xa.boundary.log/level)
                                    :transaction (ig/ref :jepsen-xa.transaction/transaction)}
            :jepsen-xa.boundary.db/specs {:db1 {:port 55432 :host "127.0.0.1"}
                                          :db2 {:port 55433 :host "127.0.0.1"}}
            :jepsen-xa.transaction/transaction {:db-specs (ig/ref :jepsen-xa.boundary.db/specs)
                                                :logger (ig/ref :jepsen-xa.boundary.log/level)
                                                :balance-update (ig/ref :jepsen-xa.boundary.balance/update)}
            :jepsen-xa.boundary.balance/update (ig/ref :jepsen-xa.boundary.log/level)
            :jepsen-xa.handler/server {:port 3000,
                                       :join false
                                       :app (ig/ref :jepsen-xa.handler/app)}}

           (load-config {:log/level {:app :debug :other :debug}
                         :instrument true
                         :server {:port 3000
                                  :join false}
                         :db {:db1 {:port 55432 :host "127.0.0.1"}
                              :db2 {:port 55433 :host "127.0.0.1"}}}))
        "Failure")))
