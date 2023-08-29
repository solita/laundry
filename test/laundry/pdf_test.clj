(ns laundry.pdf-test
  (:require
   [clojure.java.io :as io]
   [clojure.test :refer :all]
   [laundry.test.fixture :as fixture]
   [peridot.multipart]
   [ring.mock.request :as mock]
   [ring.util.request])
  (:import
   (javax.imageio ImageIO)))

(deftest ^:integration api-pdf-pdf-preview
  (let [app (fixture/get-app)
        file (io/file (io/resource "hypno.pdf"))
        _ (assert file)
        request (-> (mock/request :post "/pdf/pdf-preview")
                    (merge (peridot.multipart/build {:file file})))
        response (app request)
        content-type (get-in response [:headers "Content-Type"])]
    (is (= 200 (:status response)))
    (is (= "image/jpeg" content-type))
    (when (= "image/jpeg" content-type)
      (let [img (ImageIO/read (:body response))]
        (is (some? img)
            "Response is an image")
        (when img
          (is (= 612 (.getWidth img)))
          (is (= 792 (.getHeight img))))))))

(deftest ^:integration api-pdf-pdf2pdfa
  (let [app (fixture/get-app)
        file (io/file (io/resource "hypno.pdf"))
        _ (assert file)
        request (-> (mock/request :post "/pdf/pdf2pdfa")
                    (merge (peridot.multipart/build {:file file})))
        response (app request)
        body (ring.util.request/body-string response)]
    (is (= 200 (:status response)))
    (is (= "application/pdf" (get-in response [:headers "Content-Type"])))
    (is (clojure.string/starts-with? body "%PDF-"))))

(deftest ^:integration api-pdf-pdf2pdfa-with-opt-args
  (let [app (fixture/get-app)
        file (io/file (io/resource "hypno.pdf"))
        _ (assert file)
        request (-> (mock/request :post "/pdf/pdf2pdfa")
                    (assoc-in [:query-params :dpi] 720)
                    (assoc-in [:query-params :maxbitmap] 0)
                    (assoc-in [:query-params :pdfsettings] "/default")
                    (assoc-in [:query-params :pdfaconformance] 2)
                    (merge (peridot.multipart/build {:file file})))
        response (app request)
        body (ring.util.request/body-string response)]
    (is (= 200 (:status response)))
    (is (= "application/pdf" (get-in response [:headers "Content-Type"])))
    (is (clojure.string/starts-with? body "%PDF-"))))

(deftest ^:integration api-pdf-pdf2txt
  (let [app (fixture/get-app)
        file (io/file (io/resource "hypno.pdf"))
        _ (assert file)
        request (-> (mock/request :post "/pdf/pdf2txt")
                    (merge (peridot.multipart/build {:file file})))
        response (app request)
        body (ring.util.request/body-string response)]
    (is (= 200 (:status response)))
    (is (= "text/plain; charset=utf-8" (get-in response [:headers "Content-Type"])))
    (is (= "All glory to the hypnotoad." (clojure.string/trim body)))))
