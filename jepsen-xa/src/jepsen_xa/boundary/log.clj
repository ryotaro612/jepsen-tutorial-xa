(ns ^{:doc "The logging configuration for this application."
      :author "Ryotaro Nakamura"}
 jepsen-xa.boundary.log
  (:require [taoensso.timbre :as timbre]
            [jepsen-xa.log]
            [integrant.core :as ig]))

(defrecord TimbreLogger [config]
  jepsen-xa.log/Logger
  (debug [this message]
    (timbre/with-config config
      (timbre/debug message)))
  (info [this message]
    (timbre/with-config config
      (timbre/info message)))
  (error [this message]
    (timbre/with-config config
      (timbre/error message))))

(defn load-config
  [app other]
  ;https://taoensso.github.io/timbre/taoensso.timbre.html#var-*config*
  (merge timbre/default-config
         {:min-level [[#{"jepsen-xa.*"} app] [#{"*"} other]]}))

(defmethod ig/init-key ::level [_ {:keys [app other] :as opts}]
  (let [config (merge timbre/default-config (load-config app other))]
    ; handle jetty's logging message.
    (timbre/set-config! config)
    (map->TimbreLogger {:config config})))

(defmethod ig/halt-key! ::level [_ _]
  (timbre/debug {:message "Update the timbre config."
                 :config timbre/default-config})
  (timbre/set-config! timbre/default-config))
