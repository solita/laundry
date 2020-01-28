(ns laundry.main
  (:require
   [clojure.java.io :as io]
   [clojure.tools.cli :refer [parse-opts]]
   [laundry.machines :as machines]
   [laundry.server :as server])
  (:gen-class))

(machines/add-command-line-rule!
 ["-h" "--help" :id :help])

(machines/add-command-line-rule!
 ["-p" "--port PORT" "Port number"
  :default 8080
  :parse-fn #(Integer/parseInt %)
  :validate [#(< 0 % 0x10000) "Port must be 1-65535"]
  :id :port])

(machines/add-command-line-rule!
 ["-k" "--api-key-file FILE" "File name"
  :default nil
  :parse-fn (fn [x] ;; obfuscation against printing/logging the config
              (let [pw (when x
                         (clojure.string/trim-newline (slurp x)))]
                (fn []
                  pw)))
  :id :basic-auth-password])

(machines/add-command-line-rule!
 ["-t" "--tools DIR" "External tools directory"
  :default "/opt/laundry/bin"
  :validate [(fn [x] (.isDirectory (io/file x))) "Argument must be a directory"]])

(machines/add-command-line-rule!
 ["-S" "--slow-request MS" "slow request warning threshold in ms"
  :default 10000
  :parse-fn #(Integer/parseInt %)
  :id :slow-request-warning])

(machines/add-command-line-rule!
 ["-L" "--log-level" "choose log level"
  :default :info
  :parse-fn keyword
  :id :log-level])

(defn -main [& args]
  (let [conf (parse-opts args (deref machines/command-line-rules))
        opts (:options conf)]
    (cond
      (-> conf :options :help)
      (do
        (println "Usage: java -jar laundry.jar ...")
        (println (:summary conf))
        0)
      (not (= 0 (count (:arguments conf))))
      (do
        (println "Unexpected arguments: " (:arguments conf))
        1)
      (:errors conf)
      (do
        (println "No:")
        (println (:errors conf))
        1)
      :else
      (do
        (server/start-server opts)
        0))))
