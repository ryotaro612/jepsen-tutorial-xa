(ns jepsen-xa.balance
  (:require [clojure.spec.alpha :as s]))

(defprotocol BalanceUpdate
  "An account"
  (add-balance [this conn user-id amount] "Increase or decrease the balance of an account"))

(s/def ::balance-update #(satisfies? BalanceUpdate %))

(defprotocol BalanceLookUp
  "Return the balance of a user."
  (lookup [this user-id amount] "Return the balance of a user"))
