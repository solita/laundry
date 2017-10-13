(ns laundry.config
   (:require 
             [schema.core :as s]
             [taoensso.timbre :as timbre :refer [trace debug info warn]]
             [taoensso.timbre.appenders.core :as appenders]))

(s/defschema Status 
   (s/enum "ok" "error"))

(s/defschema LaundryConfig
   {:port s/Num
    :slow-request-warning s/Num
    :temp-directory s/Str
    :tools s/Str
    :log-level (s/enum :debug :info)})

(defonce config (atom nil))

(s/defn set! [new-configuration :- LaundryConfig]
   (debug "setting config to" new-configuration)
   (reset! config new-configuration))

(defn current [] @config)

(defn read
   ([key]
      (get @config key nil))
   ([key default]
      (get @config key default)))

   

