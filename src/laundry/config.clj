(ns laundry.config
  (:require
   [laundry.schemas :refer [LaundryConfig]]
   [schema.core :as s]
   [taoensso.timbre :as timbre :refer [trace debug info warn]]
   [taoensso.timbre.appenders.core :as appenders]))

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
