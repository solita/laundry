(ns laundry.antivirus
  (:require
   [clojure.string :as string]
   [compojure.api.sweet :as sweet :refer [POST]]
   [laundry.machines :as machines]
   [laundry.util :refer [shell-out!]]
   [ring.middleware.multipart-params :refer [wrap-multipart-params]]
   [ring.swagger.upload :as upload]
   [ring.util.http-response :as htresp]
   [schema.core :as s]
   [taoensso.timbre :as timbre :refer [info warn]]))

(s/defn api-clamdscan [env, tempfile :- java.io.File]
  (let [path (.getAbsolutePath tempfile)
        res (shell-out! (str (:tools env) "/antivirus") path)]
    (.delete tempfile)
    ;; The following cases are pretty complex as we need to prepare
    ;; for errors from docker in addition to clamdscan exit codes
    (cond
      (and (string/blank? (:err res)) (= (:exit res) 0) (string/includes? (:out res) "Infected files: 0"))
      (htresp/content-type (htresp/ok) "text/plain") ;; no viruses
      (and (string/blank? (:err res)) (= (:exit res) 1) (string/includes? (:out res) "Infected files: 1"))
      (do
        (warn (str "Viruses found! " (:out res)))
        (htresp/content-type (htresp/bad-request (str "Viruses found! " (:out res))) "text/plain"))
      :else (machines/badness-resp "Error while performing antivirus scan!" res))))

(machines/add-api-generator!
 (fn [env]
   (sweet/context "/antivirus" []
     (POST "/scan" []
       :summary "scan attached file for viruses"
       :multipart-params [file :- upload/TempFileUpload]
       :middleware [wrap-multipart-params]
       (let [tempfile (:tempfile file)
             filename (:filename file)]
         (info "Antivirus scanner received " filename "(" (:size file) "b)")
         (.deleteOnExit tempfile) ;; cleanup if VM is terminated
         (api-clamdscan env tempfile))))))
