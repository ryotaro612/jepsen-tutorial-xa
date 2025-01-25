(ns jepsen-xa.remote
  (:require [clojure.spec.alpha :as s]
            [integrant.core :as ig]
            [clojure.java.shell :refer [sh]]
            [taoensso.timbre :as timbre]
            [jepsen.control.core :as control]))

(defrecord DockerRemote [container-name]
  ; https://jepsen-io.github.io/jepsen/jepsen.control.core.html#var-disconnect.21
  jepsen.control.core/Remote
  (connect [this conn-spec]
    (timbre/debug {:record "DockerRemote" :method "connect" :conn-spec conn-spec})
    (map->DockerRemote (assoc this :container-name (:host conn-spec))))
  (disconnect! [this] this)
  (execute! [this context action]
    ; :context {:dir "/", :sudo nil, :sudo-password nil}, :action {:cmd "cd /; hostname"}
    (timbre/debug {:record "DockerRemote" :this this :method "execute!" :context context :action action})
    (let [{:keys [dir sudo sudo-password]} context
          {:keys [cmd]} action]
      (apply sh "docker" "exec" container-name "sh" "-c" cmd
             (if-let [in (:in action)]
               [:in in]
               [])))))

