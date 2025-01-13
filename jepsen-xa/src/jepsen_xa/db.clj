(ns jepsen-xa.db
  (:require [clojure.java.jdbc :as jdbc]
            [clj-time.format :as f]
            [clj-time.core :as t]))

(defn- do-command
  [conn sql]
  (with-open [ps (jdbc/prepare-statement
                  conn sql)]
    (.execute ps)))

(defn begin!
  [conn]
  (do-command conn "begin"))

(defn rollback!
  [conn]
  (do-command conn "rollback"))

(defn prepare-transaction!
  [conn transaction-id]
  (do-command conn (str "prepare transaction '" transaction-id "'")))

(defn commit-prepared!
  [conn transaction-id]
  (do-command conn (str "commit prepared '" transaction-id "'")))

(defn rollback-prepared!
  [conn transaction-id]
  (do-command conn (str "rollback prepared '" transaction-id "'")))

(defn generate-transaction-id
  []
  (let [suffix (f/unparse (f/formatter "hhmmssSSS") (t/now))]
    (str "transaction_" suffix)))


(defmacro with-connection [[conn db-spec] & body]
  `(jdbc/with-db-connection [{~conn :connection} ~db-spec]
     ~@body))
