(ns jepsen-xa.log
  (:require [taoensso.timbre :as timbre]
            [integrant.core :as ig]))

(defn load-config
  [app other]
  ;https://taoensso.github.io/timbre/taoensso.timbre.html#var-*config*
  (merge timbre/default-config
         {:min-level [[#{"jepsen-xa.*"} app] [#{"*"} other]]}))


(defmethod ig/init-key ::level [_ {:keys [app other] :as opts}]
  (timbre/set-config! (merge timbre/default-config (load-config app other))))

(defmethod ig/halt-key! ::level [_ _]
  (timbre/debug {:message "Update the timbre config."
                 :config timbre/default-config})
  (timbre/set-config! timbre/default-config))
