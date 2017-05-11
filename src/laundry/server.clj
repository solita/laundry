(ns laundry.server
   (:require [compojure.api.sweet :refer :all]
             [ring.util.http-response :refer [ok status content-type] :as resp]
             [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
             [ring.adapter.jetty :as jetty]
             [ring.swagger.upload :as upload]
             [schema.core :as s]
             [taoensso.timbre :as timbre :refer [trace debug info warn]]
             [taoensso.timbre.appenders.core :as appenders]
             [clojure.java.shell :refer [sh]]
             [pantomime.mime :refer [mime-type-of]]
             [clojure.string :as string]
             [clojure.set :as set]
             [clojure.java.io :as io]
             [clojure.pprint :refer [pprint]]
             [cheshire.core :as json]
             [ring.util.codec :as codec]))

(s/defschema Status 
   (s/enum "ok" "error"))

(s/defschema LaundryConfig
   {:port s/Num
    :slow-request-warning s/Num
    :log-level (s/enum :debug :info)})

(defonce config (atom nil))

(defn timestamp []
   (.getTime (java.util.Date.)))
   
(s/defn set-config! [new-configuration :- LaundryConfig]
   (debug "setting config to" new-configuration)
   (reset! config new-configuration))

(defn get-config
   ([key]
      (get-config key nil))
   ([key default]
      (get @config key default)))


(defn not-ok [res]
   (status (ok res) 500))

(defn not-there [res]
   (status (ok res) 404))


;;; Handler

(def api-handler
   (api
      {:swagger
         {:ui "/api-docs"
          :spec "/swagger.json"
          :data {:info {:title "Laundry API"
                          :description ""}}}}

      (undocumented
        (GET "/" []
          (resp/temporary-redirect "/index.html")))

      (context "/api" []

         (GET "/alive" []
            :summary "check whether server is running"
            (ok "yes"))

         (GET "/config" []
            :summary "get current laundry configuration"
            :return LaundryConfig
            (ok @config))
         
         (POST "/pdf2pdfa" []
            :summary "attempt to convert a PDF file to PDF/A"
            :multipart-params [file :- upload/TempFileUpload]
            :middleware [upload/wrap-multipart-params]
            (let [tempfile (:tempfile file)
                  filename (:filename file)]
               (info "PDF converter received " filename "(" (:size file) "b)")
               (.deleteOnExit tempfile) ;; cleanup if VM is terminated
               (ok "not converting yet"))))))

(defn request-time-logger [handler]
   (fn [req]
      (let [start (timestamp)
            res (handler req)
            elapsed (- (timestamp) start)]
         (if (> elapsed (get-config :slow-request-warning 10000))
            (warn (str "slow request: " (:uri req) 
                  " took " elapsed "ms")))
         res)))

(def handler
  (-> api-handler
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
     (info "Laundry is running at port" (get-config :port))))

(s/defn ^:always-validate start-server [conf :- LaundryConfig]
   ;; update config
   (set-config! conf)
   ;; configure logging accordingly
   (timbre/merge-config!
      {:appenders
         {:spit
            (appenders/spit-appender
               {:fname (get-config :logfile "laundry.log")})}})
   (timbre/set-level!
      (get-config :log-level :info))
   (info "start-server, config " @config)
   ;; configure logging
   (start-laundry handler
      {:port (get-config :port 8080)
              :join? false}))


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
       :log-level :info}))

(defn go []
   (info "Go!")
   (reset))

