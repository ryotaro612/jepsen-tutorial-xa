(ns ^{:doc "The logging configuration for this application."
      :author "Ryotaro Nakamura"}
    jepsen-xa.log)


(defprotocol Logger
  "A logger"
  (debug [this message] "Debug level logging"))
