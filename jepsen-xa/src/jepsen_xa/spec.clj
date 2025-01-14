(ns jepsen-xa.spec
  (:require [clojure.spec.test.alpha :as stest]
            [taoensso.timbre :as timbre]
            [jepsen-xa.log :as log]
            [integrant.core :as ig]))

(defmethod ig/init-key ::instrument [_ {:keys [enable logger]}]
  (log/debug logger {:message "instrument"
                     :enable enable})
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

