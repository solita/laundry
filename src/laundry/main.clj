(ns laundry.main
   (:require
      [laundry.server :as server]
      [clojure.tools.cli :refer [parse-opts]])
   (:gen-class))

(def command-line-rules
  [["-p" "--port PORT" "Port number"
      :default 8080
      :parse-fn #(Integer/parseInt %)
      :validate [#(< 0 % 0x10000) "Port must be 1-65535"]]
   ["-t" "--slow-request MS" "slow request warning threshold in ms"
      :default 10000
      :parse-fn #(Integer/parseInt %)]
    ["-h" "--help"
      :id :help])

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
            (server/start-server 
               (select-keys (:options conf)
                  [:port]))
            0)))
