(ns jepsen-xa.client
  (:require [integrant.core :as ig]
            [jepsen.control.docker :as docker]
            [taoensso.timbre :as timbre]
            [jepsen.os.debian :as debian]
            [jepsen-xa.remote :as remote]
            [jepsen
             [nemesis :as nemesis]             
             [checker :as checker]
             [db :as db]
             [generator :as gen]
             [client :as client]
             [tests :as tests]
             [cli :as cli]]
            [clojure.spec.alpha :as s]
            [jepsen-xa
             [log :as log]
             [invocation :as invocation]
             [model :as model]]
            [jepsen-xa.boundary.jepsen.client]))

(defn load-config
  "Make the integramnt configuration map for the client."
  [{:keys [instrument db-specs app]
    logger :logger}]
  (timbre/debug {:func "load-method" :app app :db-specs db-specs})
  {:jepsen-xa.boundary.log/level logger
   :jepsen-xa.spec/instrument {:enable instrument
                               :logger (ig/ref :jepsen-xa.boundary.log/level)}
   :jepsen-xa.client/nodes {:app (:container-name app)
                            :db1 (-> db-specs :db1 :container-name)
                            :db2 (-> db-specs :db2 :container-name)}
   :jepsen-xa.boundary.jepsen.client/client {:logger (ig/ref :jepsen-xa.boundary.log/level)
                                             :lookup (ig/ref :jepsen-xa.boundary.balance/lookup)
                                             :db-spec1 (:db1 db-specs)
                                             :db-spec2 (:db2 db-specs)
                                             :transfer (ig/ref :jepsen-xa.boundary.balance/transfer)
                                             :nodes (ig/ref :jepsen-xa.client/nodes)}
   :jepsen-xa.model/model {:lookup (ig/ref :jepsen-xa.boundary.balance/lookup)
                           :db-spec1 (:db1 db-specs)
                           :db-spec2 (:db2 db-specs)}
   :jepsen-xa.boundary.balance/lookup {:logger (ig/ref :jepsen-xa.boundary.log/level)} 
   :jepsen-xa.boundary.balance/transfer {:logger (ig/ref :jepsen-xa.boundary.log/level)
                                         :url (str "http://127.0.0.1:" (:host-port app))}
   :jepsen-xa.client/test-fn {:nodes (ig/ref :jepsen-xa.client/nodes)
                              :client (ig/ref :jepsen-xa.boundary.jepsen.client/client)
                              :model (ig/ref :jepsen-xa.model/model)}
   :jepsen-xa.client/runner {:test-fn (ig/ref :jepsen-xa.client/test-fn)
                             :logger (ig/ref :jepsen-xa.boundary.log/level)}})


(s/def ::container-name string?)
(s/def ::container-db-spec (s/keys :req-un [::container-name]))
(s/def ::db1 ::container-db-spec)
(s/def ::db2 ::container-db-spec)
(s/def ::db-specs (s/keys :req-un [::db1 ::db2]))
(s/def ::host-port int?)
(s/def ::container-name string?)
(s/def ::app (s/keys :req-un [::host-port ::container-name]))
(s/def ::config
  (s/keys :req-un [::app ::db-specs]))
(s/fdef load-config
  :args (s/cat :config ::config))


(defn- make-xa-test
  [{:keys [model] :as opts}]
  (merge tests/noop-test
         {:name "xa"
          :pure-generators true
          :os debian/os
          :nemesis (nemesis/partition-random-halves)          
          :checker (checker/linearizable
                    {:model   model
                     :algorithm :linear})
          :remote (remote/map->DockerRemote {:container-name nil})
          ; https://jepsen-io.github.io/jepsen/jepsen.control.docker.html#var-resolve-container-id
          :generator   (->> (gen/mix [invocation/read-alice
                                      invocation/read-bob
                                      invocation/transfer])
                            (gen/stagger 1)
                          (gen/nemesis
                            (cycle [(gen/sleep 5)
                              {:type :info, :f :start}
                              (gen/sleep 5)
                              {:type :info, :f :stop}]))                  
                          ;(gen/nemesis nil)
                          (gen/time-limit 15))
          }
         opts
         ))
(s/def ::nodes (s/coll-of string?))
(s/def ::client #(satisfies? client/Client %))
(s/def ::test map?)
(s/def ::model :jepsen-xa.model/model)
(s/def ::opts
  (s/keys :req-un [::nodes ::client ::model]))
(s/fdef make-xa-test
  :args (s/cat :opts ::opts)
  :ret ::test)

(defmethod ig/init-key ::test-fn [_ opts]
  (timbre/debug {:int-key ::test-fn
                 :opts opts})
  (fn [_] (make-xa-test opts)))

(defmethod ig/init-key ::runner [_ {:keys [test-fn logger]}]
  ;(log/debug logger {:test-fn test-fn})
  #(cli/run!
    (cli/single-test-cmd {:test-fn test-fn}) ["test"]))

(defmethod ig/init-key ::nodes [_ {:keys [app db1 db2] :as opt} ]
  (timbre/debug {:init-key ::nodes :value opt})
  [app db1 db2])

(defn -main
  [& args]
  (ig/init (load-config)))

