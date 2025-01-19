(ns jepsen-xa.client-test
  (:require [clojure.test :refer :all]
            [integrant.core :as ig]
            [jepsen-xa.client :refer :all]))

(deftest test-load-config
  (testing "This configuration can be used in REPL."
    (let []
      (is (= {:jepsen-xa.boundary.log/level {:app :debug :other :info}
              :jepsen-xa.spec/instrument {:enable true
                                          :logger (ig/ref :jepsen-xa.boundary.log/level)}
              :jepsen-xa.client/nodes {:db-specs
                                       {:db1 {:port 55432 :host "127.0.0.1" }
                                        :db2 {:port 55433 :host "127.0.0.1"}}
                                       :app {:host-port 3001}}
              :jepsen-xa.boundary.jepsen.client/client {:logger (ig/ref :jepsen-xa.boundary.log/level)
                                                        :lookup (ig/ref :jepsen-xa.boundary.balance/lookup)
                                                        :db-spec1 {:port 55432 :host "127.0.0.1"}
                                                        :db-spec2 {:port 55433 :host "127.0.0.1"}
                                                        :transfer (ig/ref :jepsen-xa.boundary.balance/transfer)
                                                        :nodes (ig/ref :jepsen-xa.client/nodes)}
              :jepsen-xa.boundary.balance/lookup {:logger (ig/ref :jepsen-xa.boundary.log/level)}
              :jepsen-xa.boundary.balance/transfer {:logger (ig/ref :jepsen-xa.boundary.log/level)
                                                    :url "http://127.0.0.1:3001"}
              :jepsen-xa.client/test-fn {:nodes (ig/ref :jepsen-xa.client/nodes)
                                         :client (ig/ref :jepsen-xa.boundary.jepsen.client/client)}
              :jepsen-xa.client/runner {:test-fn (ig/ref :jepsen-xa.client/test-fn)
                                        :logger (ig/ref :jepsen-xa.boundary.log/level)}}
             ;actual
             (load-config {:logger {:app :debug :other :info}
                           :instrument true
                           :db-specs {:db1 {:port 55432 :host "127.0.0.1"}
                                      :db2 {:port 55433 :host "127.0.0.1"}}
                           :app {:host-port 3001}}))

          "Failure"))))

