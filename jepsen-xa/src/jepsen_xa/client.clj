(ns jepsen-xa.client
  (:require [integrant.core :as ig]
            [jepsen.control.docker :as docker]
            [jepsen.os.debian :as debian]
            [jepsen
             [db :as db]
             [generator :as gen]
             [client :as client]
             [tests :as tests]
             [cli :as cli]]
            [clojure.spec.alpha :as s]
            [jepsen-xa
             [log :as log]
             [invocation :as invocation]]
            [jepsen-xa.boundary.jepsen.client]))

(defn load-config
  "Make the integramnt configuration map for the client."
  [{:keys [instrument db-specs app]
    logger :logger}]
  {:jepsen-xa.boundary.log/level logger
   :jepsen-xa.spec/instrument {:enable instrument
                               :logger (ig/ref :jepsen-xa.boundary.log/level)}
   :jepsen-xa.client/nodes {:db-specs db-specs :app app}
   :jepsen-xa.boundary.jepsen.client/client {:logger (ig/ref :jepsen-xa.boundary.log/level)
                                             :lookup (ig/ref :jepsen-xa.boundary.balance/lookup)
                                             :db-spec1 (:db1 db-specs)
                                             :db-spec2 (:db2 db-specs)
                                             :nodes (ig/ref :jepsen-xa.client/nodes)}
   :jepsen-xa.boundary.balance/lookup {:logger (ig/ref :jepsen-xa.boundary.log/level)}
   :jepsen-xa.client/test-fn {:nodes (ig/ref :jepsen-xa.client/nodes)
                              :client (ig/ref :jepsen-xa.boundary.jepsen.client/client)}
   :jepsen-xa.client/runner {:test-fn (ig/ref :jepsen-xa.client/test-fn)
                             :logger (ig/ref :jepsen-xa.boundary.log/level)}})



; coreを実行して試す。コマンドラインから実行するケースとreplを用いる。
(defn- make-xa-test
  [opts]
  ; TODO reify dbでログを出力できるようにする
  (merge tests/noop-test
         {:name "xa"
          :pure-generators true
          :os debian/os
          :remote docker/docker
                                        ; https://jepsen-io.github.io/jepsen/jepsen.control.docker.html#var-resolve-container-id
          :generator   (->> invocation/read-alice
                            (gen/stagger 1)
                            (gen/nemesis nil)
                            (gen/time-limit 15))
          }
         opts
         ))

(s/def ::nodes (s/coll-of string?))
(s/def ::client #(satisfies? client/Client %))
(s/def ::opts
  (s/keys :req-un [::nodes ::client]))

(s/fdef make-xa-test
  :args (s/cat :opts ::opts)
  :ret int?)

(defmethod ig/init-key ::test-fn [_ {:keys [nodes client]}]
  (fn [_] (make-xa-test {:nodes nodes :client client})))

(defmethod ig/init-key ::runner [_ {:keys [test-fn logger]}]
  (log/debug logger {:test-fn test-fn})
  #(cli/run!
    (cli/single-test-cmd {:test-fn test-fn}) ["test"]))

(defmethod ig/init-key ::nodes [_ {{:keys [db1 db2]} :db-specs {:keys [host-port]} :app}]
  [(str "127.0.0.1:" (str host-port))
   (str (:host db1) ":" (-> db1 :port str))
   (str (:host db2) ":" (-> db2 :port str))])

(defn -main
  [& args]
  (ig/init (load-config)))

