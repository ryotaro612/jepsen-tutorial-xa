(ns ^{:doc "The logging configuration for this application."
      :author "Ryotaro Nakamura"}
 jepsen-xa.log
  (:require [clojure.spec.alpha :as s]))

(defprotocol Logger
  "A logger"
  (debug [this message] "Debug level logging")
  (info [this message] "Info level logging")
  (error [this message] "Error level logging"))

(s/def ::logger #(satisfies? Logger %))

(defn log-level
  [level default]
  (case level
    "DEBUG" :debug
    "INFO" :info
    "ERROR" :error
    default))
