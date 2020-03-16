(ns laundry.server
  (:require
   [clojure.java.io :as io]
   [clojure.string :as string]
   [compojure.api.sweet :as sweet :refer [api undocumented GET context routes]]
   [compojure.route]

   ;; Require the machine defining namespaces
   [laundry.machines :as machines]
   laundry.docx
   laundry.image
   laundry.pdf

   [laundry.schemas :refer [LaundryConfig]]

   [ring.adapter.jetty :as jetty]
   [ring.middleware.basic-authentication :refer [wrap-basic-authentication]]
   [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
   [ring.util.http-response :refer [ok] :as resp]
   [schema.core :as s]
   [taoensso.timbre :as timbre :refer [info warn]]
   [taoensso.timbre.appenders.core :as appenders]))

(defn timestamp []
  (.getTime (java.util.Date.)))

(s/defn temp-file-input-stream [path :- s/Str]
  (let [input (io/input-stream (io/file path))]
    (proxy [java.io.FilterInputStream] [input]
      (close []
        (proxy-super close)
        (io/delete-file path)))))

(defn create-auth-middleware [env]
  (let [configured-password ((or (get env :basic-auth-password) (constantly nil)))]
    (if (nil? configured-password)
      identity
      (fn [handler]
        (wrap-basic-authentication handler (fn auth-ok? [user-name provided-password]
                                             (and (= "laundry-api" user-name)
                                                  (= provided-password configured-password))))))))

;;; Handler

(s/defn make-api-handler [api-calls, env :- LaundryConfig]
  (api {:swagger
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

       (context "/" []
         :middleware [(create-auth-middleware env)]
         (GET "/auth-test" []
           (ok))
         (apply routes api-calls))

       (undocumented
         (compojure.route/not-found (resp/not-found)))))

(defn laundry-path-stripper [handler]
  ;; accommodate getting served requests with /laundry prefix
  ;; for when we are behind a non-rewriting load balancer serving also other paths.
  ;; (AWS case)
  (fn [req]
    (let [new-req (update req :uri #(clojure.string/replace % #"^/laundry" ""))]
      (handler new-req))))

(defn request-time-logger [handler conf]
  (fn [req]
    (let [start (timestamp)
          res (handler req)
          elapsed (- (timestamp) start)]
      (when (> elapsed (get conf :slow-request-warning 10000))
        (warn (str "slow request: " (:uri req)
                   " took " elapsed "ms")))
      res)))

(s/defn make-handler [api env :- LaundryConfig]
  (let [handler (-> (make-api-handler api env)
                    (request-time-logger env)
                    laundry-path-stripper
                    (wrap-defaults (-> (assoc-in site-defaults [:security :anti-forgery] false)
                                       (assoc-in [:params :multipart] false))))]
    handler))

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
    (info "Laundry is running at port" (get conf :port))))

(s/defn ^:always-validate start-server [conf :- LaundryConfig]
  (let [api (machines/generate-apis conf)]
    ;; configure logging accordingly
    (timbre/merge-config!
     {:appenders
      {:spit
       (appenders/spit-appender
        {:fname (get conf :logfile "laundry.log")})}})
    (timbre/set-level!
     (get conf :log-level :info))
    (info "start-server, config " conf)
    ;; configure logging
    (start-laundry (make-handler api conf)
                   {:port (get conf :port 8080)
                    :join? false})))

;;; Dev mode entry

(defn reset []
  (when server
    (println "Stopping laundry")
    (stop-server))
  (require 'laundry.server :reload)
  ;; todo: default config should come from command line setup
  (start-server
   {:slow-request-warning 500
    :port 9001
    :tools "programs"
    :log-level :info
    :basic-auth-password nil}))

(defn go []
  (info "Go!")
  (reset))
