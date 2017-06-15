(ns laundry.main
   (:require
      [laundry.server :as server]
      [clojure.tools.cli :refer [parse-opts]])
   (:gen-class))

(def command-line-rules
  [["-p" "--port PORT" "Port number"
      :default 8080
      :parse-fn #(Integer/parseInt %)
      :validate [#(< 0 % 0x10000) "Port must be 1-65535"]
      :id :port]
   ["-t" "--temporary-directory" "directory for holding temporary data"
      :default "/tmp"
      :id :temp-directory]
   ["-S" "--slow-request MS" "slow request warning threshold in ms"
      :default 10000
      :parse-fn #(Integer/parseInt %)
      :id :slow-request-warning]
    ["-C" "--checksum-command COMMAND" "compute a checksum"
       :default "/opt/laundry/bin/checksum"
       :id :checksum-command]
    ["-P" "--pdf2pdfa-command COMMAND" "command for PDF/A conversion"
       :default "/opt/laundry/bin/pdf2pdfa"
       :id :pdf2pdfa-command]
    ["-X" "--pdf2png-command COMMAND" "command for PDF preview"
       :default "/opt/laundry/bin/pdf2png"
       :id :pdf2png-command]
    ["-T" "--pdf2txt-command COMMAND" "command for PDF to text conversion"
       :default "/opt/laundry/bin/pdf2txt"
       :id :pdf2txt-command]
    ["-L" "--log-level" "choose log level"
       :default :info 
       :parse-fn keyword 
       :id :log-level]
    ["-h" "--help"
      :id :help]])

(defn -main [& args]
   (let [conf (parse-opts args command-line-rules)]
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
               (println (str " opts " (:options conf)))
               (server/start-server 
                  (select-keys (:options conf)
                     [:port :slow-request-warning :log-level :temp-directory 
                      :checksum-command :pdf2pdfa-command :pdf2png-command 
                      :pdf2txt-command]))
               0))))
