(ns jepsen-xa.boundary.balance
  (:require [jepsen-xa.balance :as b]
            [org.httpkit.client :as hk-client]
            [clojure.data.json :as json]
            [jepsen-xa.log :as log]
            [clojure.spec.alpha :as s]
            [clojure.java.jdbc :as jdbc]
            [integrant.core :as ig]))

(defrecord BalanceUpdatePsql []
  b/BalanceUpdate
  (add-balance [_ conn user-id amount]
    (with-open [ps (jdbc/prepare-statement
                    conn
                    "update account set balance = balance + ? where user_id = ?" {:timeout 1000})]
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
                                    ;:timeout 1000
                                    ;:connect-timeout 1000
                                    })]
      {:success (= (:status response) 201)})))


(defmethod ig/init-key ::update [_ _]
  (map->BalanceUpdatePsql {}))

(defmethod ig/init-key ::lookup [_ _]
  (map->BalnceLookUpPsql {}))

(defmethod ig/init-key ::transfer [_ {:keys [logger url]}]
  (log/debug logger {:message "init a transfer client"
                     :url url})
  (map->TransferHttpClient {:logger logger :url url}))
