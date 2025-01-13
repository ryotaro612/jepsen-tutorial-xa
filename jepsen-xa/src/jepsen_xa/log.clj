(ns ^{:doc "The logging configuration for this application."
      :author "Ryotaro Nakamura"}
    jepsen-xa.log
  (:require [clojure.spec.alpha :as s]))


(defprotocol Logger
  "A logger"
  (debug [this message] "Debug level logging"))

(s/def ::logger #(satisfies? Logger %))
