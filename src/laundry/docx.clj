(ns laundry.docx
  (:require
   [clojure.java.io :as io]
   [clojure.pprint :refer [pprint]]
   [clojure.set :as set]
   [clojure.string :as string]
   [compojure.api.sweet :as sweet :refer :all]
   [laundry.machines :as machines]
   [laundry.util :refer [shell-out!]]
   [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
   [ring.middleware.multipart-params :refer [wrap-multipart-params]]
   [ring.swagger.upload :as upload]
   [ring.util.http-response :as htresp]
   [schema.core :as s]
   [taoensso.timbre :as timbre :refer [trace debug info warn]])
  (:import
   (java.io ByteArrayOutputStream ByteArrayInputStream StringWriter File)))

(s/defn api-docx2pdf [env, tempfile :- java.io.File]
  (let [in-path (.getAbsolutePath tempfile)
        out-path  (str in-path ".pdf")
        res (shell-out! (str (:tools env) "/bin/docx2pdf")
                        in-path out-path)]
    (.delete tempfile) ;; todo: move to finally block
    (if (= (:exit res) 0)
      (htresp/content-type
       (htresp/ok (io/input-stream (io/file out-path)))
       "application/pdf")
      (machines/badness-resp "docx2pdf conversion failed"))))

(machines/add-api-generator!
 (fn [env]
   (sweet/context "/docx" []
     (POST "/docx2pdf" []
       :summary "attempt to convert a .docx file to PDF"
       :multipart-params [file :- upload/TempFileUpload]
       :middleware [wrap-multipart-params]
       (let [tempfile (:tempfile file)
             filename (:filename file)]
         (info "docx converter received " filename "(" (:size file) "b)")
         (.deleteOnExit tempfile) ;; cleanup if VM is terminated
         (api-docx2pdf env tempfile))))))
