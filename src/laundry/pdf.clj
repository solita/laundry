(ns laundry.pdf
   (:require [compojure.api.sweet :as sweet :refer :all]
             [ring.util.http-response :as htresp]
             [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
             [ring.swagger.upload :as upload]
             [taoensso.timbre :as timbre :refer [trace debug info warn]]
             [schema.core :as s]
             [laundry.util :refer [shell-out!]]
             [laundry.machines :as machines]
             [clojure.string :as string]
             [clojure.set :as set]
             [clojure.java.io :as io]
             [clojure.pprint :refer [pprint]]))


(s/defn temp-file-input-stream [path :- s/Str]
   (let [input (io/input-stream (io/file path))]
      (proxy [java.io.FilterInputStream] [input]
         (close []
            (proxy-super close)
            (io/delete-file path)))))


(defn badness-resp [msg]
  (htresp/content-type (htresp/internal-server-error msg)
                       "text/plain"))

;; pdf/a converter
(s/defn api-pdf2pdfa [env, tempfile :- java.io.File]
  (let [in-path (.getAbsolutePath tempfile)
        out-path  (str (.getAbsolutePath tempfile) ".pdf")
        res (shell-out! (str (:tools env) "/bin/pdf2pdfa")
                        in-path out-path)]
      (.delete tempfile)
      (if (= (:exit res) 0)
         (htresp/content-type 
            (htresp/ok (temp-file-input-stream out-path))
             "application/pdf")
         (badness-resp "pdf2pdfa conversion failed"))))

;; pdf → txt conversion
(s/defn api-pdf2txt [env, tempfile :- java.io.File]
   (info "Running, tools are at " (:tools env))
   (let [path (.getAbsolutePath tempfile)
         out  (str path ".txt")
         res (shell-out! (str (:tools env) "/bin/pdf2txt") path out)]
      (.delete tempfile)
      (if (= (:exit res) 0)
         (htresp/content-type 
            (htresp/ok (temp-file-input-stream out))
             "text/plain")
         (badness-resp "pdf2txt conversion failed"))))

;; previewer of first page
(s/defn api-pdf2jpeg [env, tempfile :- java.io.File]
   (let [path (.getAbsolutePath tempfile)
         out  (str (.getAbsolutePath tempfile) ".jpeg")
         res (shell-out! (str (:tools env) "/bin/pdf2jpeg") path out)]
      (.delete tempfile)
      (if (= (:exit res) 0)
         (htresp/content-type 
            (htresp/ok (temp-file-input-stream out))
             "image/jpeg")
         (do
           (warn "pdf preview failed: " res)
           (badness-resp "pdf preview failed")))))

(machines/add-api-generator! 
   (fn [env] 
      (sweet/context "/pdf" []
         
         (POST "/pdf-preview" []
            :summary "attempt to convert first page of a PDF to PNG"
            :multipart-params [file :- upload/TempFileUpload]
            :middleware [upload/wrap-multipart-params]
            (let [tempfile (:tempfile file)
                  filename (:filename file)]
               (info "PDF previewer received " filename "(" (:size file) "b)")
               (.deleteOnExit tempfile) ;; cleanup if VM is terminated
               (api-pdf2jpeg env tempfile)))
            
         (POST "/pdf2txt" []
            :summary "attempt to convert a PDF file to TXT"
            :multipart-params [file :- upload/TempFileUpload]
            :middleware [upload/wrap-multipart-params]
            (let [tempfile (:tempfile file)
                  filename (:filename file)]
               (info "PDF2TXT converter received " filename "(" (:size file) "b)")
               (.deleteOnExit tempfile) ;; cleanup if VM is terminated
               (api-pdf2txt env tempfile)))
            
         (POST "/pdf2pdfa" []
            :summary "attempt to convert a PDF file to PDF/A"
            :multipart-params [file :- upload/TempFileUpload]
            :middleware [upload/wrap-multipart-params]
 -           (let [tempfile (:tempfile file)
                  filename (:filename file)]
               (info "PDF converter received " filename "(" (:size file) "b)")
               (.deleteOnExit tempfile) ;; cleanup if VM is terminated
               (api-pdf2pdfa env tempfile))))))
         
