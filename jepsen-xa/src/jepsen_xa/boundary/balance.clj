(ns jepsen-xa.boundary.balance
  (:require [jepsen-xa.balance :as b]
            [clojure.spec.alpha :as s]
            [clojure.java.jdbc :as jdbc]
            [integrant.core :as ig]))


(defrecord BalanceUpdatePsql [logger]
  b/BalanceUpdate
  (add-balance [_ conn user-id amount]
    (with-open [ps (jdbc/prepare-statement
                    conn
                    "update account set balance = balance + ? where user_id = ?")]
      (.setInt ps 1 amount)
      (.setString ps 2 user-id)
      (.execute ps))))

(defmethod ig/init-key ::update [_ logger]
  (map->BalanceUpdatePsql {:logger logger}))
