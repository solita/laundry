(ns laundry.xlsx
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

(s/defn api-xlsx2pdf [env, tempfile :- java.io.File]
  (let [in-path (.getAbsolutePath tempfile)
        out-path  (str in-path ".pdf")
        res (shell-out! (str (:tools env) "/xlsx2pdf")
                        in-path out-path)]
    (.delete tempfile) ;; todo: move to finally block
    (if (= (:exit res) 0)
      (htresp/content-type
       (htresp/ok (io/input-stream (io/file out-path)))
       "application/pdf")
      (machines/badness-resp "xlsx2pdf conversion failed" res))))

(machines/add-api-generator!
 (fn [env]
   (sweet/context "/xlsx" []
     (POST "/xlsx2pdf" []
       :summary "attempt to convert a .xlsx file to PDF"
       :multipart-params [file :- upload/TempFileUpload]
       :middleware [wrap-multipart-params]
       (let [tempfile (:tempfile file)
             filename (:filename file)]
         (info "xlsx converter received " filename "(" (:size file) "b)")
         (.deleteOnExit tempfile) ;; cleanup if VM is terminated
         (api-xlsx2pdf env tempfile))))))
