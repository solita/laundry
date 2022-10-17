(ns laundry.antivirus-missing-container-test
  (:require
   [clojure.test :refer [deftest is use-fixtures]]
   [laundry.test.fixture :as fixture]
   [peridot.multipart]
   [ring.mock.request :as mock]
   [ring.util.request]))

(use-fixtures :once fixture/batch-peridot-build-with-string-fixture!)

(deftest ^:integration api-clamdscan-without-container-running
  (let [app (fixture/get-app)
        request (-> (mock/request :post "/antivirus/scan")
                    (merge (peridot.multipart/build {:file fixture/eicar-antivirus-test-file-contents})))
        response (app request)
        body (ring.util.request/body-string response)]
    (is (= 500 (:status response)))
    (is (= body "Error while performing antivirus scan!"))))

