(ns jepsen-xa.model  
  (:require [knossos.model :as model]
            [integrant.core :as ig]
            [taoensso.timbre :as timbre]
            [jepsen-xa.balance :as b]
            [clojure.spec.alpha :as s]))


(defrecord ModelTransfer [alice bob]
  knossos.model.Model
  (step [this op]
    (timbre/debug {:message "model transfer"
                   :func "step"
                   :this this
                   :op op})
    (case (:f op)
      :read-bob (let [{{:keys [balance]} :value} op]
                  (if (= balance bob)
                    this
                    (model/inconsistent (str "bob balance is " balance " but expected " bob))))
      :read-alice (let [{{:keys [balance]} :value} op]
                  (if (= balance alice)
                    this
                    (model/inconsistent (str "alice balance is " balance " but expected " alice))))
      :transfer (let [{{:keys [sender amount success]} :value} op]
                  (if success
                    (case sender
                      :alice (map->ModelTransfer {:alice (- alice amount) :bob (+ bob amount)})
                      :bob (map->ModelTransfer {:alice (+ alice amount) :bob (- bob amount)}))
                    this)))
    this))

(s/def ::bob int?)
(s/def ::alice int?)
(s/def ::model (s/keys :req-un [::alice ::bob]))

(defmethod ig/init-key ::model [_ {:keys [lookup db-spec1 db-spec2]}]
  (let [initial (map->ModelTransfer {:alice (:balance (b/lookup lookup db-spec1 "alice"))
                                     :bob (:balance (b/lookup lookup db-spec2 "bob"))})]
    (timbre/debug {:message "initial model"
                   :value initial})
    initial))
