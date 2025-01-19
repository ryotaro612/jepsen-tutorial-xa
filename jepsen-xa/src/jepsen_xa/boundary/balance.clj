(ns jepsen-xa.boundary.balance
  (:require [jepsen-xa.balance :as b]
            [org.httpkit.client :as hk-client]
            [clojure.data.json :as json]
            [jepsen-xa.log :as log]
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


(defrecord BalnceLookUpPsql [logger]
  b/BalanceLookUp
  (lookup [_ db-spec user-id]
    (first (jdbc/query
     db-spec
     ["select balance from account where user_id = ?" user-id]))))



(defrecord TransferHttpClient [logger url]
  b/Transfer
  (transaction [_ sender amount]
    ; sender 文字列
    (log/debug logger {:message "transfer"
                       :sender sender
                       :amount amount})
    (let[response @(hk-client/post (str url "/transactions")
                                   {:headers {"content-type" "application/json"
                                              "accept" "application/json"}
                                    :body (json/write-str {:sender (sender {:alice "alice" :bob "bob"})
                                                           :amount amount})
                                    })]
      {:success (= (:status response) 201)})))

#_{:opts
 {:headers
  {"content-type" "application/json", "accept" "application/json"},
  :body "{\"sender\":\"alice\",\"amount\":1000}",
  :method :post,
  :url "http://localhost:3000/transactions"},
 :body "{\"message\":\"success\"}",
 :headers
 {:content-type "application/json;charset=utf-8",
  :date "Sun, 19 Jan 2025 11:47:12 GMT",
  :server "Jetty(11.0.24)",
  :transfer-encoding "chunked"},
 :status 201}

(defmethod ig/init-key ::update [_ logger]
  (map->BalanceUpdatePsql {:logger logger}))

(defmethod ig/init-key ::lookup [_ logger]
  (map->BalnceLookUpPsql {:logger logger}))

(defmethod ig/init-key ::transfer [_ {:keys [logger url]}]
  (log/debug logger {:message "init a transfer client"
                     :url url})
  (map->TransferHttpClient {:logger logger :url url}))
