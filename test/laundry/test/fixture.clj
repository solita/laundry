(ns laundry.test.fixture
  (:require
   [laundry.machines :as machines]
   [laundry.server :as server]
   [peridot.multipart])
  (:import
   (org.apache.http.entity.mime MultipartEntity)
   (org.apache.http.entity.mime.content ByteArrayBody)))

(def test-conf
  {:tools "programs"})

(defn get-app-with [conf]
  (let [api (machines/generate-apis conf)
        app (server/make-handler api conf)]
    app))

(defn get-app []
  (get-app-with test-conf))

;; fixture for invoking peridot.multipart/build with strings converted to files
(defn batch-peridot-build-with-string-fixture! [f]
  (defmethod peridot.multipart/add-part String [^MultipartEntity m k ^String s]
    (.addPart m
              (peridot.multipart/ensure-string k)
              (ByteArrayBody. (.getBytes s) "text/plain" "foobar.txt")))
  (f)
  (remove-method peridot.multipart/add-part String))

(def eicar-antivirus-test-file-contents "X5O!P%@AP[4\\PZX54(P^)7CC)7}$EICAR-STANDARD-ANTIVIRUS-TEST-FILE!$H+H*")