(ns jepsen-xa.spec
  (:require [clojure.spec.test.alpha :as stest]
            [taoensso.timbre :as timbre]
            [jepsen-xa.log :as log]
            [integrant.core :as ig]))

(defmethod ig/init-key ::instrument [_ {:keys [enable logger]}]
  (println "doge" logger)
  (log/debug logger {:message "instrument"
                     :enable enable})
  (println "doge2")
  (if enable
    (stest/instrument)
    (stest/unstrument))
  {:enable enable
   :logger logger})

(defmethod ig/halt-key! ::instrument [_ {:keys [logger enable]}]
  (if enable
    (do
      (log/debug logger {:message "instrument"
                         :enable false})
      (stest/unstrument))))

