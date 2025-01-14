(ns jepsen-xa.client
  (:require [integrant.core :as ig]
            [jepsen.tests :as tests]
            [jepsen-xa.log :as log]
            [jepsen.cli :as cli]))

(defn load-config
  ""
  [{:keys [instrument db-specs]
    {:keys [app other]} :logger}]
  {:jepsen-xa.boundary.log/level {:app app :other other}
   :jepsen-xa.spec/instrument {:enable instrument
                               :logger (ig/ref :jepsen-xa.boundary.log/level)}
   :jepsen-xa.client/test-fn nil
   :jepsen-xa.client/runner {:test-fn (ig/ref :jepsen-xa.client/test-fn)
                             :logger (ig/ref :jepsen-xa.boundary.log/level)}})

(defn make-xa-test
  [opts]
  (merge tests/noop-test
         {:pure-generators true}
         opts))

(defmethod ig/init-key ::test-fn [_ _]
  make-xa-test)

(defmethod ig/init-key ::runner [_ {:keys [test-fn logger]}]
  (log/debug logger {:test-fn test-fn})
  #(cli/run!
    (cli/single-test-cmd {:test-fn test-fn}) ["test"]))

(defn -main
  [& args]
  (ig/init (load-config)))
