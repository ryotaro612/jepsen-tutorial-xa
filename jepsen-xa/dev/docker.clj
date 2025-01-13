(ns docker
  (:require [clostache.parser :as p]))

(defn- generate-docker-compose
  [template-path]
  (let [template (slurp template-path)
        db1-name "jepsen-tutorial-xa-db1"
        db2-name "jepsen-tutorial-xa-db2"]
    (p/render template {:db-service [{:name db1-name :script-name "alice.sql" :port 55432}
                                     {:name db2-name :script-name "bob.sql" :port 55433}]
                        :db1-name db1-name
                        :db2-name db2-name})))

(defn -main
  [& args]
  (let [template (generate-docker-compose (nth args 0))]
    (spit (nth args 1) template)))

