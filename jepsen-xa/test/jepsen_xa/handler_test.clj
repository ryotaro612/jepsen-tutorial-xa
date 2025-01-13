(ns jepsen-xa.handler-test
  (:require [clojure.test :refer :all]
            [integrant.core :as ig]
            [jepsen-xa.handler :refer :all]))
                                        ; cider-test c-c c-t c-p
; run cider-refresh if something is wrong
(deftest show-config-examples
  (testing "This configuration can be used in REPL."
    (is (= {:jepsen-xa.log/level {:app :debug, :other :debug},		  
		    :jepsen-xa.spec/instrument
		    {:enable true, :log (ig/ref :jepsen-xa.log/level)}
            :jepsen-xa.handler/app {:log (ig/ref :jepsen-xa.log/level)}
            :jepsen-xa.handler/server{:port 3000, :join false :app (ig/ref :jepsen-xa.handler/app)}}
           (load-config {:log/level {:app :debug :other :debug}
                         :instrument true
                         :server {:port 3000
                                  :join false}
                         }))
        "Failure")))
