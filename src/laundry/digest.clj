(ns laundry.digest
  (:require
   [clojure.java.io :as io]
   [clojure.java.shell :refer [sh]]
   [compojure.api.sweet :as sweet :refer :all]
   [digest :as clj-digest]
   [laundry.machines :as machines]
   [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
   [ring.middleware.multipart-params :refer [wrap-multipart-params]]
   [ring.swagger.upload :as upload]
   [ring.util.http-response :refer [ok status content-type] :as resp]
   [schema.core :as s]
   [taoensso.timbre :as timbre :refer [trace debug info warn]]))

(s/defschema DigestAlgorithm
  (s/enum "sha256"))

(s/defn api-checksum [tempfile :- java.io.File, algorithm :- DigestAlgorithm]
  (ok (str (clj-digest/digest algorithm tempfile) "\n")))

(machines/add-api-generator!
 (fn [env]
   (sweet/context "/digest" []
     (POST "/sha256" []
       :summary "compute a SHA256 digest for posted data"
       :multipart-params [file :- upload/TempFileUpload]
       :middleware [wrap-multipart-params]
       (let [tempfile (:tempfile file)
             filename (:filename file)]
         (info "SHA256 received " filename "(" (:size file) "b)")
         (.deleteOnExit tempfile)
         (api-checksum tempfile "SHA-256"))))))
