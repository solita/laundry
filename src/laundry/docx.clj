(ns laundry.docx
  (:require
   [clojure.java.io :as io]
   [compojure.api.sweet :as sweet :refer [POST]]
   [laundry.machines :as machines]
   [laundry.util :refer [shell-out!]]
   [ring.middleware.multipart-params :refer [wrap-multipart-params]]
   [ring.swagger.upload :as upload]
   [ring.util.http-response :as htresp]
   [schema.core :as s]
   [taoensso.timbre :as timbre :refer [info]]))

(s/defschema PdfVersion
  "Check for allowed PDF versions as specified in LibreOffice documentation here: https://help.libreoffice.org/latest/en-US/text/shared/guide/pdf_params.html
   Listing allowed choices as of 11.12.2024, LibreOffice version 24.8
   0: PDF 1.7 (default choice).
   1: PDF/A-1b
   2: PDF/A-2b
   3: PDF/A-3b
   15: PDF 1.5
   16: PDF 1.6
   17: PDF 1.7"
  (s/constrained s/Int (fn [version-number]
                       (contains? #{0 1 2 3 15 16 17} version-number))))

(s/defn api-docx2pdf [env, tempfile :- java.io.File pdf-version :- PdfVersion]
  (let [in-path (.getAbsolutePath tempfile)
        out-path  (str in-path ".pdf")
        res (shell-out! (str (:tools env) "/docx2pdf")
                        in-path out-path (str pdf-version))]
    (.delete tempfile) ;; todo: move to finally block
    (if (= (:exit res) 0)
      (htresp/content-type
       (htresp/ok (io/input-stream (io/file out-path)))
       "application/pdf")
      (machines/badness-resp "docx2pdf conversion failed" res))))

(machines/add-api-generator!
 (fn [env]
   (sweet/context "/docx" []
     (POST "/docx2pdf" []
       :summary "attempt to convert a .docx file to PDF"
       :query-params #_{:clj-kondo/ignore [:unresolved-symbol]} [{pdf-version :- PdfVersion 0}]
       :multipart-params [file :- upload/TempFileUpload]
       :middleware [wrap-multipart-params]
       (let [tempfile (:tempfile file)
             filename (:filename file)]
         (info "docx converter received " filename "(" (:size file) "b), requested pdf-version: " pdf-version)
         (.deleteOnExit tempfile) ;; cleanup if VM is terminated
         (api-docx2pdf env tempfile pdf-version))))))
