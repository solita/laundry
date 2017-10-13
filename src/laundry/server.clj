(ns laundry.server
   (:require [compojure.api.sweet :as sweet :refer :all]
             [ring.util.http-response :refer [ok status content-type] :as resp]
             [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
             [ring.adapter.jetty :as jetty]
             [ring.swagger.upload :as upload]
             [schema.core :as s]
             [taoensso.timbre :as timbre :refer [trace debug info warn]]
             [taoensso.timbre.appenders.core :as appenders]
             [clojure.java.shell :refer [sh]]
             [pantomime.mime :refer [mime-type-of]]
             [laundry.pdf :as pdf]
             [laundry.digest :as digest]
             [laundry.test :as test]
             [laundry.machines :as machines]
             [laundry.config :as config]
             [laundry.schemas :refer :all]
             [laundry.cache :as cache]
             [clojure.string :as string]
             [clojure.set :as set]
             [clojure.java.io :as io]
             [clojure.pprint :refer [pprint]]
             [cheshire.core :as json]
             [ring.util.codec :as codec])
   (:import 
      [java.io File]))

(defn timestamp []
   (.getTime (java.util.Date.)))
   
(s/defn temp-file-input-stream [path :- s/Str]
   (let [input (io/input-stream (io/file path))]
      (proxy [java.io.FilterInputStream] [input]
         (close []
            (proxy-super close)
            (io/delete-file path)))))

;;; Handler
    
(s/defn make-api-handler [api-calls, env :- LaundryConfig]
   (apply api
      (concat
         [ {:swagger
               {:ui "/api-docs"
                :spec "/swagger.json"
                :data {:info {:title "Laundry API"
                                :description ""}}}}
   
            (undocumented
              (GET "/" []
                (resp/temporary-redirect "/index.html")))
         
            (GET "/alive" []
                  :summary "check whether server is running"
                  (ok "yes"))
            
            (GET "/config" []
               :summary "get current laundry configuration"
               (ok (config/current)))
            ]
        api-calls)))

(defn request-time-logger [handler]
   (fn [req]
      (let [start (timestamp)
            res (handler req)
            elapsed (- (timestamp) start)]
         (if (> elapsed (config/read :slow-request-warning 10000))
            (warn (str "slow request: " (:uri req) 
                  " took " elapsed "ms")))
         res)))

(s/defn make-handler [api env :- LaundryConfig]
  (-> (make-api-handler api env)
      request-time-logger
      (wrap-defaults (-> (assoc-in site-defaults [:security :anti-forgery] false)
                         (assoc-in [:params :multipart] false)))))

;;; Startup and shutdown

(defonce server (atom nil))

(defn stop-server []
   (when-let [s @server]
      (info "stopping server")
      (.stop s)
      (reset! server nil)))

(defn start-laundry [handler conf]
   (when server
      (stop-server))
   (stop-server)
   (reset! server (jetty/run-jetty handler conf))
   (when server
     (info "Laundry is running at port" (config/read :port))))

(s/defn ^:always-validate start-server [conf :- LaundryConfig] 
   ;; update config
   (config/set! conf)
   (let [api (machines/generate-apis conf)]
      ;; configure logging accordingly
      (timbre/merge-config!
         {:appenders
            {:spit
               (appenders/spit-appender
                  {:fname (config/read :logfile "laundry.log")})}})
      (timbre/set-level!
         (config/read :log-level :info))
      (info "start-server, config " (config/current))
      ;; configure logging
      (start-laundry (make-handler api conf)
         {:port (config/read :port 8080)
          :join? false})))


;;; Dev mode entry

(defn reset [] 
   (if server
      (do
         (println "Stopping laundry")
         (stop-server)))
   (require 'laundry.server :reload)
   ;; todo: default config should come from command line setup
   (start-server
      {:slow-request-warning 500
       :port 9001
       :temp-directory "/tmp"
       :tools "/opt/laundry"
       :log-level :info}))

(defn go []
   (info "Go!")
   (reset))

