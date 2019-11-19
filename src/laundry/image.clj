(ns laundry.image
   (:require [compojure.api.sweet :as sweet :refer :all]
             [ring.util.http-response :as htresp]
             [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
             [ring.swagger.upload :as upload]
             [taoensso.timbre :as timbre :refer [trace debug info warn]]
             [schema.core :as s]
             [laundry.util :refer [shell-out!]]
             [laundry.machines :as machines :refer [badness-resp]]
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
         (badness-resp "png2png conversion failed"))))


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
         (badness-resp "jpeg2jpeg conversion failed"))))

(machines/add-api-generator! 
   (fn [env] 
      (sweet/context "/image" []
         
         (POST "/png2png" []
            :summary "attempt to convert a PNG file to PNG"
            :multipart-params [file :- upload/TempFileUpload]
            :middleware [upload/wrap-multipart-params]
            (let [tempfile (:tempfile file)
                  filename (:filename file)]
               (info "PNG converter received " filename "(" (:size file) "b)")
               (.deleteOnExit tempfile) ;; cleanup if VM is terminated
               (api-png2png env tempfile)))
         (POST "/jpeg2jpeg" []
            :summary "attempt to convert a JPEG file to JPEG"
            :multipart-params [file :- upload/TempFileUpload]
            :middleware [upload/wrap-multipart-params]
            (let [tempfile (:tempfile file)
                  filename (:filename file)]
               (info "PNG converter received " filename "(" (:size file) "b)")
               (.deleteOnExit tempfile) ;; cleanup if VM is terminated
               (api-jpeg2jpeg env tempfile))))))
         
