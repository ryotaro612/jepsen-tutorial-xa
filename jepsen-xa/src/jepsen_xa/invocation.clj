(ns jepsen-xa.invocation
  (:require [clojure.spec.alpha :as s]))


(s/def ::time int?)
(s/def ::time-map
  (s/keys :req-un [::time]))

(defn read-alice [node time-map]
  {:type :invoke :f :read-alice :value nil})

(defn read-bob [node time-map]
  {:type :invoke :f :read-bob :value nil})

(defn transfer [node time-map]
  (let [send-i (rand-int 2)
        amount (+ 1 (rand-int 10))]
       {:type :invoke :f :transfer :value {:sender (nth [:alice :bob] send-i)
                                           :amount amount}}))
;; (s/def ::node any?)
;; (s/fdef read-alice
;;   :args (s/cat :node ::node :time-map ::time-map))
