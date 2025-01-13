(ns jepsen-xa.spec
  (:require [clojure.spec.test.alpha :as stest]
            [taoensso.timbre :as timbre]
            [integrant.core :as ig]))

(defmethod ig/init-key ::instrument [_ {:keys [enable]}]
  (timbre/debug {:message "instrument"
                 :enable enable})  
  (if enable
ra      (stest/instrument)
      (stest/unstrument))
  enable)

(defmethod ig/halt-key! ::instrument [_ enable]
  (if enable
    (do
        (timbre/debug {:message "instrument"
                       :enable false})
        (stest/unstrument))))

