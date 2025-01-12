(ns jepsen-xa.docker
  (:require [clostache.parser :as p]))

(defn- generate-docker-compose
  [template-path]
  (let [template (slurp template-path)]
    (p/render template {:db-service [{:name "db1" :script-name "alice.sql" :port 55432}
                                     {:name "db2" :script-name "bob.sql" :port 55433}]})))

(defn -main
  [& args]
  (println args)
  (let [template (generate-docker-compose (nth args 0))]

    (spit (nth args 1) template)))

