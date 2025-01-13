(ns docker
  (:require [clostache.parser :as p]))

(def env
  (let [db1-name "jepsen-tutorial-xa-db1"
        db2-name "jepsen-tutorial-xa-db2"]
      {:db-services [{:name db1-name :script-name "alice.sql" :port 55432}
                     {:name db2-name :script-name "bob.sql" :port 55433}]}))

(defn- generate-docker-compose
  [template-path]
  (let [template (slurp template-path)
        db1-name (->> (nth (env :db-services) 0) :name)
        db2-name (->> (nth (env :db-services) 1) :name)]
    (p/render template (merge env {:db1-name db1-name
                                   :db2-name db2-name}))))

(defn -main
  [& args]
  (let [template (generate-docker-compose (nth args 0))]
    (spit (nth args 1) template)))

