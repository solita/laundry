(ns laundry.cache
   (:require 
      [compojure.api.sweet :as sweet :refer :all]
      [schema.core :as s]
      [clojure.java.shell :refer [sh]]
      [laundry.machines :as machines]
      [laundry.config :as config]
      [laundry.schemas :refer :all]
      [clojure.java.io :as io])
   (:import 
      [java.io File]))

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
         (machines/badness-resp "cache cleanup failed"))))

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

