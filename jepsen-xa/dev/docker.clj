(ns docker
  (:require [clostache.parser :as p]
            [clojure.spec.alpha :as s]
            [config]))

(defn- generate-docker-compose
  [conf template-path]
  (let [template (slurp template-path)
        db-service-names (map :name (-> conf :db-services))]
    (p/render template (merge conf {:db1-name (nth db-service-names 0)
                                             :db2-name (nth db-service-names 1)}))))


(defn -main
  "Generate docker-compses file from a template.
  The number of the arguments is 2.
  The first is the template file. The second is the output file."
  [& args]  
  (let [template-file-path (nth args 0)
        output-path (nth args 1)
        template (generate-docker-compose config/config template-file-path)]
    (spit output-path template)))

