(ns laundry.machines
  (:require
   [ring.util.http-response :as htresp]
   [taoensso.timbre :as timbre :refer [warn]]))

(defonce command-line-rules (atom []))

(defn add-command-line-rule! [rule]
  (swap! command-line-rules
         (fn [rules]
           (conj rules rule))))

(defonce api-generators (atom []))

(defn add-api-generator! [generator]
  (swap! api-generators
         (fn [gens]
           (conj gens generator))))

(defn generate-apis [config]
  (map
   (fn [gen] (gen config))
   (deref api-generators)))

;; response helpers commonly used by machines

(defn badness-resp [msg log]
  (warn (str msg ": " log))
  (htresp/content-type (htresp/internal-server-error (str msg))
                       "text/plain"))