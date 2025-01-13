(ns jepsen-xa.balance)


(defprotocol BalanceUpdate
  "An account"
  (add-balance [this conn user-id amount] "Increase or decrease the balance of an account"))


(defprotocol BalanceLookUp
  "Return the balance of a user."
  (lookup [this user-id amount] "Return the balance of a user"))
