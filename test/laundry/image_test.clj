(ns laundry.image-test
  (:require
   [clojure.java.io :as io]
   [clojure.test :refer :all]
   [laundry.test.fixture :as fixture]
   [peridot.multipart]
   [ring.mock.request :as mock])
  (:import
   (javax.imageio ImageIO)))

(deftest ^:integration api-image-jpeg2jpeg
  (let [app (fixture/get-app)
        file (io/file (io/resource "test.jpg"))
        _ (assert file)
        orig-img (ImageIO/read file)
        request (-> (mock/request :post "/image/jpeg2jpeg")
                    (merge (peridot.multipart/build {:file file})))
        response (app request)
        content-type (get-in response [:headers "Content-Type"])]
    (is (= 200 (:status response)))
    (is (= "image/jpeg" content-type))
    (when (= "image/jpeg" content-type)
      (let [img (ImageIO/read (:body response))]
        (is (some? img)
            "Response is an image")
        (is (= (.getWidth orig-img) (.getWidth img)))
        (is (= (.getHeight orig-img) (.getHeight img)))))))

(deftest ^:integration api-image-png2png
  (let [app (fixture/get-app)
        file (io/file (io/resource "test.png"))
        _ (assert file)
        orig-img (ImageIO/read file)
        request (-> (mock/request :post "/image/png2png")
                    (merge (peridot.multipart/build {:file file})))
        response (app request)
        content-type (get-in response [:headers "Content-Type"])]
    (is (= 200 (:status response)))
    (is (= "image/png" content-type))
    (when (= "image/png" content-type)
      (let [img (ImageIO/read (:body response))]
        (is (some? img)
            "Response is an image")
        (is (= (.getWidth orig-img) (.getWidth img)))
        (is (= (.getHeight orig-img) (.getHeight img)))))))
