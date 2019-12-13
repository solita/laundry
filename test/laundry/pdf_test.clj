(ns laundry.pdf-test
  (:require
   [clojure.java.io :as io]
   [clojure.test :refer :all]
   [laundry.test.fixture :as fixture]
   [peridot.multipart]
   [ring.mock.request :as mock]
   [ring.util.request]))

(deftest ^:integration api-pdf-pdf2pdfa
  (let [app (fixture/get-app)
        file (io/file (io/resource "testcases/hypno.pdf"))
        _ (assert file)
        request (-> (mock/request :post "/pdf/pdf2pdfa")
                    (merge (peridot.multipart/build {:file file})))
        response (app request)
        body (ring.util.request/body-string response)]
    (is (= 200 (:status response)))
    (is (= "application/pdf" (get-in response [:headers "Content-Type"])))
    (is (clojure.string/starts-with? body "%PDF-1.4"))))
