(ns jepsen-xa.transaction
  (:require [integrant.core :as ig]))


(defn- transaction!
  [sender amount db-spec1 db-spec2])

(defmethod ig/init-key ::transaction [_ {:keys [db-spec1 db-spec2]}]
  #(transaction! %1 %2 db-spec1 db-spec2))
