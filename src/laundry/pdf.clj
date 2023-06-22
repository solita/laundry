(ns laundry.pdf
  (:require
   [clojure.java.io :as io]
   [compojure.api.sweet :as sweet :refer [POST]]
   [laundry.machines :as machines :refer [badness-resp]]
   [laundry.util :refer [shell-out!]]
   [ring.middleware.multipart-params :refer [wrap-multipart-params]]
   [ring.swagger.upload :as upload]
   [ring.util.http-response :as htresp]
   [schema.core :as s]
   [taoensso.timbre :as timbre :refer [info]]))

(s/defn temp-file-input-stream [path :- s/Str]
  (let [input (io/input-stream (io/file path))]
    (proxy [java.io.FilterInputStream] [input]
      (close []
        (proxy-super close)
        (io/delete-file path)))))

;; pdf/a converter
(s/defn api-pdf2pdfa [env, tempfile :- java.io.File, dpinum :- s/Int]
  (let [in-path (.getAbsolutePath tempfile)
        out-path  (str (.getAbsolutePath tempfile) ".pdf")
        dpi (str dpinum)
        res (shell-out! (str (:tools env) "/pdf2pdfa")
                        in-path out-path dpi)]
    (.delete tempfile)
    (if (= (:exit res) 0)
      (htresp/content-type
       (htresp/ok (temp-file-input-stream out-path))
       "application/pdf")
      (badness-resp "pdf2pdfa conversion failed" res))))

;; pdf → txt conversion
(s/defn api-pdf2txt [env, tempfile :- java.io.File]
  (let [path (.getAbsolutePath tempfile)
        out  (str path ".txt")
        res (shell-out! (str (:tools env) "/pdf2txt") path out)]
    (.delete tempfile)
    (if (= (:exit res) 0)
      (htresp/content-type
       (htresp/ok (temp-file-input-stream out))
       "text/plain")
      (badness-resp "pdf2txt conversion failed" res))))

;; previewer of first page
(s/defn api-pdf2jpeg [env, tempfile :- java.io.File]
  (let [path (.getAbsolutePath tempfile)
        out  (str (.getAbsolutePath tempfile) ".jpeg")
        res (shell-out! (str (:tools env) "/pdf2jpeg") path out)]
    (.delete tempfile)
    (if (= (:exit res) 0)
      (htresp/content-type
       (htresp/ok (temp-file-input-stream out))
       "image/jpeg")
      (badness-resp "pdf preview failed" res))))

(machines/add-api-generator!
 (fn [env]
   (sweet/context "/pdf" []
     (POST "/pdf-preview" []
       :summary "attempt to convert first page of a PDF to JPEG"
       :multipart-params [file :- upload/TempFileUpload]
       :middleware [wrap-multipart-params]
       (let [tempfile (:tempfile file)
             filename (:filename file)]
         (info "PDF previewer received " filename "(" (:size file) "b)")
         (.deleteOnExit tempfile) ;; cleanup if VM is terminated
         (api-pdf2jpeg env tempfile)))
     (POST "/pdf2txt" []
       :summary "attempt to convert a PDF file to TXT"
       :multipart-params [file :- upload/TempFileUpload]
       :middleware [wrap-multipart-params]
       (let [tempfile (:tempfile file)
             filename (:filename file)]
         (info "PDF2TXT converter received " filename "(" (:size file) "b)")
         (.deleteOnExit tempfile) ;; cleanup if VM is terminated
         (api-pdf2txt env tempfile)))
     (POST "/pdf2pdfa" []
       :summary "attempt to convert a PDF file to PDF/A"
       :query-params [{dpi :- s/Int 720}]
       :multipart-params [file :- upload/TempFileUpload]
       :middleware [wrap-multipart-params]
       (let [tempfile (:tempfile file)
             filename (:filename file)]
         (info "PDF converter received " filename "(" (:size file) "b)")
         (.deleteOnExit tempfile) ;; cleanup if VM is terminated
         (api-pdf2pdfa env tempfile dpi))))))
