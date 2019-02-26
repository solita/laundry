(ns laundry.docx
  (:require [compojure.api.sweet :as sweet :refer :all]
             [ring.util.http-response :as htresp]
             [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
             [ring.swagger.upload :as upload]
             [taoensso.timbre :as timbre :refer [trace debug info warn]]
             [schema.core :as s]
             [clojure.java.shell :as shell]
             [laundry.machines :as machines]
             [pantomime.mime :refer [mime-type-of]]
             [clojure.string :as string]
             [clojure.set :as set]
             [clojure.java.io :as io]
             [clojure.pprint :refer [pprint]])
  (:import (java.io ByteArrayOutputStream ByteArrayInputStream StringWriter File)
           (org.apache.poi.xwpf.usermodel XWPFDocument)
           (fr.opensagres.poi.xwpf.converter.pdf PdfConverter PdfOptions)))


(defn- filename-as-pdf-suffix [filename]
  (string/replace-first filename #"(?i)\.(\w*)" ".pdf"))

(defn docx->pdf [filename-info input-juttu]
  ;; input-juttu = lähde, voi olla input-streamille kelpaava olio eli InputStream, File, URI, URL,Socket, byte array, tai String
  ;; filename-infolla ei tehdä muuta kuin vaihdetaan tiedostopääte
  
  (with-open [in (io/input-stream input-juttu)]
    (let [doc (XWPFDocument. in)
          first-bytesink (ByteArrayOutputStream.)]

      ;; Täytyy olla tyylit, vaikkakin vain tyhjät.  https://stackoverflow.com/questions/51330192/trying-to-make-simple-pdf-document-with-apache-poi      
      (.createStyles doc)
      ;; "document must be written so underlaaying objects will be committed" -> ylimääräisen XWPFDocument:n luonti.
      (.write doc first-bytesink)
      (.close doc)

      ;; Avataan ekan writen tulos taas input streamiksi
      (with-open [in (io/input-stream (.toByteArray first-bytesink))]
        (let [new-doc (XWPFDocument. in)
              final-bytesink (ByteArrayOutputStream.)]
          (-> (PdfConverter/getInstance) (.convert new-doc final-bytesink (PdfOptions/create)))
          (.close new-doc)
          {:name  (filename-as-pdf-suffix filename-info)
           :mime  "application/pdf"
           :bytes (.toByteArray final-bytesink)})))))

(s/defn api-docx2pdf [env, tempfile :- java.io.File]
  (let [res (docx->pdf "file.docx" (io/input-stream tempfile))]
      (.delete tempfile) ;; todo: move to finally block
      (if (:bytes res)
        (htresp/content-type 
         (htresp/ok (io/input-stream (:bytes res)))
         "application/pdf")
         (machines/badness-resp "docx2pdf conversion failed"))))

(machines/add-api-generator! 
   (fn [env] 
     (sweet/context "/docx" []
         (POST "/docx2pdf" []
            :summary "attempt to convert a .docx file to PDF"
            :multipart-params [file :- upload/TempFileUpload]
            :middleware [upload/wrap-multipart-params]
            (let [tempfile (:tempfile file)
                  filename (:filename file)]
               (info "PDF converter received " filename "(" (:size file) "b)")
               (.deleteOnExit tempfile) ;; cleanup if VM is terminated
               (api-docx2pdf env tempfile))))))
