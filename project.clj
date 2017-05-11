(defproject laundry "0.0.0-SNAPSHOT"
   :dependencies 
      [[org.clojure/clojure "1.8.0"]
       [prismatic/schema "1.1.3"]
       [prismatic/plumbing "0.5.3"]
       [metosin/potpuri "0.4.0"]
       [ring/ring "1.5.0"]
       [ring/ring-defaults "0.2.1"]
       [metosin/compojure-api "1.2.0-alpha2"]
       [metosin/ring-http-response "0.8.0"]
       [metosin/ring-swagger "0.22.14"]
       [metosin/ring-swagger-ui "2.2.5-0"]
       [clj-http "3.4.1"]
       [hiccup "1.0.5"]
       [enlive "1.1.6"]
       [org.clojure/core.async "0.2.395"]
       [org.clojure/clojurescript "1.9.293"]
       [prismatic/dommy "1.1.0"]
       [com.novemberain/pantomime "2.8.0"]
       [org.clojure/tools.cli "0.3.1"] 
       [com.taoensso/timbre "4.8.0"]
       [com.fzakaria/slf4j-timbre "0.3.2"]]
   
   :source-paths ["src"]

   :profiles 
      {:dev 
         {:dependencies [[ring-mock "0.1.5"]]
          :resource-paths ["target/generated"]
          :plugins [[lein-cljsbuild "1.1.5"]]}
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
