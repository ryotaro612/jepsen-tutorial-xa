(ns jepsen-xa.invocation
    (:require [clojure.spec.alpha :as s])
  )

(defn read-alice [node time-map]
  {:type :invoke :f :read, :value nil})

(s/def ::time int?)
(s/def ::time-map
  (s/keys :req-un [::time]))

(s/fdef read-alice
  :args (s/cat :node any? :time ::time-map))



                                        ;(defn read [a b]
;)
;a:  {:remote #jepsen.control.docker.DockerRemote{:container-id nil}, :concurrency 3, :store {:handle #jepsen.store.format.Handle{:file #object[sun.nio.ch.FileChannelImpl 0x7fcfe8a9 sun.nio.ch.FileChannelImpl@7fcfe8a9], :version #atom[1 0x5eedbc31], :block-index #atom[{:root 2, :blocks {1 18, 2 47, 3 266, 4 :reserved}} 0x178c0f7f], :written? #atom[true 0x5c2372da], :read? #atom[false 0x5c295357]}}, :db #object[jepsen.db$reify__11786 0x15666e13 jepsen.db$reify__11786@15666e13], :name xa, :start-time #clj-time/date-time "2025-01-19T08:41:19.404Z", :net #object[jepsen.net$reify__13238 0x7d931f37 jepsen.net$reify__13238@7d931f37], :client #jepsen_xa.boundary.jepsen.client.ClientGateway{:nodes [127.0.0.1:3001 127.0.0.1:55432 127.0.0.1:55433], :logger #jepsen_xa.boundary.log.TimbreLogger{:config {:min-level [[#{jepsen-xa.*} :debug] [#{*} :info]], :ns-filter #{*}, :middleware [], :timestamp-opts {:pattern :iso8601, :locale :jvm-default, :timezone :utc}, :output-fn #function[taoensso.timbre/default-output-fn], :appenders {:println {:enabled? true, :fn #function[taoensso.timbre.appenders.core/println-appender/fn--5413]}}}}}, :barrier #object[java.util.concurrent.CyclicBarrier 0xd29ec87 java.util.concurrent.CyclicBarrier@d29ec87], :pure-generators true, :checker #object[jepsen.checker$unbridled_optimism$reify__11101 0x209774fb jepsen.checker$unbridled_optimism$reify__11101@209774fb], :nemesis #jepsen.nemesis.Validate{:nemesis #object[jepsen.nemesis$reify__13341 0x4518ad79 jepsen.nemesis$reify__13341@4518ad79]}, :nodes [127.0.0.1:3001 127.0.0.1:55432 127.0.0.1:55433], :sessions {127.0.0.1:3001 #jepsen.control.docker.DockerRemote{:container-id 0e6595fbfdf9}, 127.0.0.1:55432 #jepsen.control.docker.DockerRemote{:container-id 71a09a7c3a78}, 127.0.0.1:55433 #jepsen.control.docker.DockerRemote{:container-id 7dae99383fd4}}, :os #object[jepsen.os.debian.Debian 0xcbb9499 jepsen.os.debian.Debian@cbb9499]}
;b:  {:time 1302804}
