(ns laundry.xlsx-test
  (:require
   [clojure.java.io :as io]
   [clojure.test :refer :all]
   [laundry.test.fixture :as fixture]
   [peridot.multipart]
   [ring.mock.request :as mock]
   [ring.util.request]))

(deftest ^:integration api-xlsx
  (let [app (fixture/get-app)
        file (io/file (io/resource "test.xls"))
        _ (assert file)
        request (-> (mock/request :post "/xlsx/xlsx2pdf")
                    (merge (peridot.multipart/build {:file file})))
        response (app request)
        body (ring.util.request/body-string response)]
    (is (= 200 (:status response)))
    (is (clojure.string/starts-with? body "%PDF-1.4"))))
