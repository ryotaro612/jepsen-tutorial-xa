(ns jepsen-xa.core
  (:require [clojure.java.jdbc :as jdbc]
            [clj-time.format :as f]
            [clj-time.core :as t]
            [slingshot.slingshot :refer [throw+ try+]]
            [clj-time.local :as l]))

(def db-spec1 {:dbtype "postgresql"
              :dbname "postgres"
              :host "127.0.0.1"   
              :port 55432            
              :user "postgres"       
              :password "password"})

(def db-spec2 {:dbtype "postgresql"
              :dbname "postgres"
              :host "127.0.0.1"   
              :port 55433 
              :user "postgres"       
               :password "password"})

(defn- do-command!
  [conn sql]
  (println conn sqlem)
  (with-open [ps (jdbc/prepare-statement
                  conn sql)]
    (.execute ps)))

(defn- begin!
  [conn]
  (do-command! conn "begin"))

(defn- rollback!
  [conn]
  (do-command! conn "rollback"))

(defn- prepare-transaction!
  [conn transaction-id]
  (do-command! conn (str "prepare transaction '" transaction-id "'")))

(defn- update!
  [conn user-id amount transaction-id]
  (try+
   (begin! conn)   
   (with-open [ps (jdbc/prepare-statement
                   conn
                   "update account set balance = balance + ? where user_id = ?")]
     (.setInt ps 1 amount)
     (.setString ps 2 user-id)
     (.execute ps))
   (prepare-transaction! conn transaction-id)
   (catch Object _
     (rollback! conn)
     #_(log/error (:throwable &throw-context) "unexpected error")
      (throw+))))

(defn- commit-prepared!
  [conn transaction-id]
  (do-command! conn (str "commit prepared '" transaction-id "'")))

(defn- rollback-prepared!
  [conn transaction-id]
  (do-command! conn (str "rollback prepared '" transaction-id "'")))

(defn- generate-transaction-id
  []
  (let [suffix (f/unparse (f/formatter "hhmmssSSS") (t/now))]
    (str "transaction_" suffix)))


(defn transaction!
  [sender amount]
  (let [transaction-id (generate-transaction-id)
        {:keys [alice bob]} (if (= sender "alice")
                              {:alice (- amount) :bob amount}
                              {:alice amount :bob (- amount)})]
    (jdbc/with-db-connection [conn1 db-spec1]
      (jdbc/with-db-connection [conn2 db-spec2]
        (update! conn1 "alice" alice transaction-id)
        (try+
          (update! conn2 "bob" bob transaction-id)
          (catch Object _
            (rollback-prepared! conn1 transaction-id)))
        (commit-prepared! conn1 transaction-id)
        (commit-prepared! conn2 transaction-id)))))



