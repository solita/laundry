(ns laundry.antivirus-unhealthy-container-test
  (:require
   [clojure.java.shell :as shell]
   [clojure.test :refer [deftest is use-fixtures]]
   [laundry.test.fixture :as fixture]
   [peridot.multipart]
   [ring.mock.request :as mock]
   [ring.util.request]))

(defn laundry-unhealthy-clamav-fixture! [f]
  ;; start clamav container without clamd to make the scans fail on error
  (shell/sh "docker" "run" "--name" "laundry-clamav" "--env" "CLAMAV_NO_FRESHCLAMD=true" "--env" "CLAMAV_NO_CLAMD=true" "--network=none" "--rm" "-d" "clamav/clamav:latest")
  (f)
  (shell/sh "docker" "stop" "laundry-clamav"))

(use-fixtures :once laundry-unhealthy-clamav-fixture! fixture/batch-peridot-build-with-string-fixture!)

(deftest ^:integration api-clamdscan-unhealthy-container
  (let [app (fixture/get-app)
        request (-> (mock/request :post "/antivirus/scan")
                    (merge (peridot.multipart/build {:file fixture/eicar-antivirus-test-file-contents})))
        response (app request)
        body (ring.util.request/body-string response)]
    (is (= 500 (:status response)))
    (is (= body "Error while performing antivirus scan!"))))