(defproject jepsen-xa "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [com.taoensso/timbre "6.6.1"]
                 [compojure "1.7.1"]
                 [ring/ring-core "1.13.0"]
                 [ring/ring-jetty-adapter "1.13.0"]
                 [ring/ring-json "0.5.1"]
                 [integrant "0.13.1"]
                 [clj-time "0.15.2"]
                 ;; https://mvnrepository.com/artifact/slingshot/slingshot
                 [slingshot/slingshot "0.12.2"]
                 [org.clojure/java.jdbc "0.7.12"]
                 [org.postgresql/postgresql "42.7.4"]]
  ; ここにつけないと警告がコンパイル時にである。a
  :aot :all
  :profiles {:app {:main jepsen-xa.handler
                   
                   #_:uberjar #_{:aot :all}
                   }})
