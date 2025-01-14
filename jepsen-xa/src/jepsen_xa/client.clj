(ns jepsen-xa.client
  (:require [integrant.core :as ig]
            [jepsen.tests :as tests]
            [jepsen.cli :as cli]))

(defn load-config
  ""
  [{:keys [instrument db-specs]
    {:keys [app other]} :logger}]
  {:jepsen-xa.boundary.log/level {:app app :other other}
   :jepsen-xa.spec/instrument {:enable instrument
                               :logger (ig/ref :jepsen-xa.boundary.log/level)}})

(defn make-xa-test
  []
  (merge tests/noop-test
         {:pure-generators true}))


(defn -main
  [& args]
  (ig/init (load-config)))
