(ns laundry.image
  (:require
   [clojure.java.io :as io]
   [clojure.pprint :refer [pprint]]
   [clojure.set :as set]
   [clojure.string :as string]
   [compojure.api.sweet :as sweet :refer :all]
   [laundry.machines :as machines :refer [badness-resp]]
   [laundry.util :refer [shell-out!]]
   [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
   [ring.middleware.multipart-params :refer [wrap-multipart-params]]
   [ring.swagger.upload :as upload]
   [ring.util.http-response :as htresp]
   [schema.core :as s]
   [taoensso.timbre :as timbre :refer [trace debug info warn]]))

(s/defn temp-file-input-stream [path :- s/Str]
  (let [input (io/input-stream (io/file path))]
    (proxy [java.io.FilterInputStream] [input]
      (close []
        (proxy-super close)
        (io/delete-file path)))))

(s/defn api-png2png [env, tempfile :- java.io.File]
  (let [in-path (.getAbsolutePath tempfile)
        out-path  (str (.getAbsolutePath tempfile) ".png")
        res (shell-out! (str (:tools env) "/bin/png2png")
                        in-path out-path)]
    (.delete tempfile)
    (if (= (:exit res) 0)
      (htresp/content-type
       (htresp/ok (temp-file-input-stream out-path))
       "image/png")
      (badness-resp "png2png conversion failed" res))))

(s/defn api-jpeg2jpeg [env, tempfile :- java.io.File]
  (let [in-path (.getAbsolutePath tempfile)
        out-path  (str (.getAbsolutePath tempfile) ".png")
        res (shell-out! (str (:tools env) "/bin/jpeg2jpeg")
                        in-path out-path)]
    (.delete tempfile)
    (if (= (:exit res) 0)
      (htresp/content-type
       (htresp/ok (temp-file-input-stream out-path))
       "image/jpeg")
      (badness-resp "jpeg2jpeg conversion failed" res))))

(machines/add-api-generator!
 (fn [env]
   (sweet/context "/image" []
     (POST "/png2png" []
       :summary "attempt to convert a PNG file to PNG"
       :multipart-params [file :- upload/TempFileUpload]
       :middleware [wrap-multipart-params]
       (let [tempfile (:tempfile file)
             filename (:filename file)]
         (info "PNG converter received " filename "(" (:size file) "b)")
         (.deleteOnExit tempfile) ;; cleanup if VM is terminated
         (api-png2png env tempfile)))
     (POST "/jpeg2jpeg" []
       :summary "attempt to convert a JPEG file to JPEG"
       :multipart-params [file :- upload/TempFileUpload]
       :middleware [wrap-multipart-params]
       (let [tempfile (:tempfile file)
             filename (:filename file)]
         (info "PNG converter received " filename "(" (:size file) "b)")
         (.deleteOnExit tempfile) ;; cleanup if VM is terminated
         (api-jpeg2jpeg env tempfile))))))
