(defproject laundry "0.0.0-SNAPSHOT"
   :dependencies 
      [[org.clojure/clojure "1.9.0"]
       [prismatic/schema "1.1.9"]
       [ring/ring "1.7.0"]
       [ring/ring-defaults "0.3.2"]
       [ring-basic-authentication "1.0.5"]
       [metosin/compojure-api "1.1.11"]
       [clj-http "3.9.1"]
       [hiccup "1.0.5"]
       [enlive "1.1.6"]
       [org.clojure/tools.cli "0.4.1"]
       [com.fzakaria/slf4j-timbre "0.3.12"]
       [com.taoensso/timbre "4.10.0"]]
   
   :source-paths ["src"]
   :target-path "target/%s/"
   :profiles 
      {:dev 
       {:dependencies [[ring-mock "0.1.5"]
                       [org.clojure/clojurescript "1.9.293"]]
          :resource-paths ["target/generated"]
          :plugins [[lein-cljsbuild "1.1.7"]]}
       :uberjar 
          {:main  laundry.main
           :aot   [laundry.main]
           :uberjar-name "laundry.jar"}}
           
   :repl-options 
      {:init-ns laundry.server
       :init (println "Now (go)")
       :timeout 900000 ; 90s, needed for slow machines
       }
       
   :main laundry.main
)
