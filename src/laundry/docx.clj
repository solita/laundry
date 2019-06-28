(ns laundry.docx
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
             [clojure.pprint :refer [pprint]])
  (:import (java.io ByteArrayOutputStream ByteArrayInputStream StringWriter File)))



(s/defn api-docx2pdf [env, tempfile :- java.io.File]
  (let [res (shell-out! (str (:tools env) "/bin/docx2pdf") path out)]
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
               (info "docx converter received " filename "(" (:size file) "b)")
               (.deleteOnExit tempfile) ;; cleanup if VM is terminated
               (api-docx2pdf env tempfile))))))
