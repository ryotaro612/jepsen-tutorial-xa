(ns jepsen-xa.invocation
  (:require [clojure.spec.alpha :as s]))


(s/def ::time int?)
(s/def ::time-map
  (s/keys :req-un [::time]))

(defn read-alice [node time-map]
  {:type :invoke :f :read-alice :value nil})

(defn read-bob [node time-map]
  {:type :invoke :f :read-bob :value nil})


;; (s/def ::node any?)
;; (s/fdef read-alice
;;   :args (s/cat :node ::node :time-map ::time-map))
