(ns jepsen-xa.client
  (:require [integrant.core :as ig]
            [jepsen.tests :as tests]
            [jepsen.control.docker :as docker]            
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
  [_]
  (merge tests/noop-test
         {:name "xa"
          :pure-generators true
          :remote docker/docker
                                        ; https://jepsen-io.github.io/jepsen/jepsen.control.docker.html#var-resolve-container-id
          :nodes [
                  "127.0.0.1:55432"                  ,
                  "127.0.0.1:55433",
                  "127.0.0.1:3001"
                  ]          
          }))

(defmethod ig/init-key ::test-fn [_ _]
  make-xa-test)

(defmethod ig/init-key ::runner [_ {:keys [test-fn logger]}]
  (log/debug logger {:test-fn test-fn})
  #(cli/run!
   (cli/single-test-cmd {:test-fn test-fn}) ["test"]))

(defn -main
  [& args]
  (ig/init (load-config)))
