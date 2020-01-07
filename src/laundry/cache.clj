(ns laundry.cache
  (:require
   [clojure.java.io :as io]
   [clojure.java.shell :refer [sh]]
   [compojure.api.sweet :as sweet :refer :all]
   [laundry.config :as config]
   [laundry.machines :as machines]
   [laundry.schemas :refer :all]
   [schema.core :as s])
  (:import
   (java.io File)))

(s/defn temp-file-input-stream [path :- s/Str]
  (let [input (io/input-stream (io/file path))]
    (proxy [java.io.FilterInputStream] [input]
      (close []
        (proxy-super close)
        (io/delete-file path)))))

(defn clean-caches []
  (let [res (sh (str (:tools (config/current)) "/bin/clean-caches"))]
    (if (= 0 (:exit res))
      (machines/ok (:out res))
      ;; not outputting error to avoid exposing unintentional data
      (machines/badness-resp "cache cleanup failed" res))))

(machines/add-command-line-rule!
 ["-T" "--temporary-directory" "directory for holding temporary data"
  :default "/tmp"
  :id :temp-directory])

(machines/add-api-generator!
 (fn [env]
   (sweet/context "/pdf" []
     (GET "/recycle" []
       :summary "clean temporary file caches"
       (clean-caches)))))
