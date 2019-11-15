(ns laundry.digest
  (:require
   [clojure.java.io :as io]
   [clojure.java.shell :refer [sh]]
   [compojure.api.sweet :as sweet :refer :all]
   [laundry.machines :as machines]
   [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
   [ring.swagger.upload :as upload]
   [ring.util.http-response :refer [ok status content-type] :as resp]
   [schema.core :as s]
   [taoensso.timbre :as timbre :refer [trace debug info warn]]))

;(machines/add-command-line-rule!
;    [nil "--checksum-command COMMAND" "compute a checksum"
;       :default "/opt/laundry/bin/checksum"
;       :id :checksum-command])

(s/defschema DigestAlgorithm
  (s/enum "sha256"))

(s/defn api-checksum [env, tempfile :- java.io.File, digest :- DigestAlgorithm]
  (let [res (sh (str (:tools env) "/bin/checksum") (.getAbsolutePath tempfile) digest)]
    (.delete tempfile)
    (if (= (:exit res) 0)
      (ok (:out res))
      (machines/badness-resp "digest computation failed"))))

(machines/add-api-generator!
 (fn [env]
   (sweet/context "/digest" []
     (POST "/sha256" []
       :summary "compute a SHA256 digest for posted data"
       :multipart-params [file :- upload/TempFileUpload]
       :middleware [upload/wrap-multipart-params]
       (let [tempfile (:tempfile file)
             filename (:filename file)]
         (info "SHA256 received " filename "(" (:size file) "b)")
         (.deleteOnExit tempfile)
         (api-checksum env tempfile "sha256"))))))
