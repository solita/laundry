(defproject laundry "0.0.0-SNAPSHOT"
   :dependencies
      [[clj-http "3.10.0"]
       [com.fzakaria/slf4j-timbre "0.3.14"]
       [com.taoensso/timbre "4.10.0"]
       [enlive "1.1.6"]
       [hiccup "1.0.5"]
       ; ring-swagger-ui upgraded manually due to compojure-api 
       ; having issue https://github.com/swagger-api/swagger-ui/issues/2547
       ; which breaks image uploads from swagger-ui
       [metosin/compojure-api "1.1.13" :exclusion [metosin/ring-swagger-ui]]
       [metosin/ring-swagger-ui "4.5.0"]
       [org.clojure/clojure "1.10.1"]
       [org.clojure/tools.cli "0.4.2"]
       [prismatic/schema "1.1.12"]
       [ring-basic-authentication "1.0.5"]
       [ring/ring "1.8.0"]
       [ring/ring-defaults "0.3.2"]]
   :source-paths ["src"]
   :target-path "target/default+uberjar/"
   :profiles
      {:dev
       {:dependencies [[peridot "0.5.2"]
                       [ring-mock "0.1.5"]]
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
       :port 43210 ; Vagrantfile opens this port for host access
       :host "0.0.0.0" ; allow connections from outside the vm
       }
   :main laundry.main
   :test-selectors {:default (complement :integration)
                    :integration :integration}
)
