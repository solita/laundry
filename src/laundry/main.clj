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
   ["-t" "--slow-request MS" "slow request warning threshold in ms"
      :default 10000
      :parse-fn #(Integer/parseInt %)
      
      :id :slow-request-warning]
    ["-L" "--log-level" 
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
                     [:port :slow-request-warning :log-level]))
               0))))
