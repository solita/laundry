(ns laundry.antivirus-test
  (:require
   [clojure.java.io :as io]
   [clojure.java.shell :as shell]
   [clojure.string  :as string]
   [clojure.test :refer [deftest is use-fixtures]]
   [laundry.test.fixture :as fixture]
   [peridot.multipart]
   [ring.mock.request :as mock]
   [ring.util.request]))

(defn query-clamav-health! []
  (let [ret (shell/sh
             "docker" "inspect" "-f" "{{.State.Running}}" "laundry-clamav")
        health (-> ret
                   :out
                   string/trim-newline)]
    (println "health status:" health)
    health))

(defn laundry-clamav-fixture! [f]
  (shell/sh "docker" "run" "--name" "laundry-clamav" "--env" "CLAMAV_NO_FRESHCLAMD=true" "--network=none" "--rm" "-d" "clamav/clamav:latest")
  ;; clamd bootup is slow, thus need to wait for container healthy stat
  (while (not= "healthy" (query-clamav-health!))
    (println "waiting for clamav container healthy status")
    (Thread/sleep 5000))
  (f)
  (shell/sh "docker" "stop" "laundry-clamav"))

(use-fixtures :once laundry-clamav-fixture! fixture/batch-peridot-build-with-string-fixture!)

(deftest ^:integration api-clamdscan-no-virus
  (let [app (fixture/get-app)
        file (io/file (io/resource "hypno.pdf"))
        request (-> (mock/request :post "/antivirus/scan")
                    (merge (peridot.multipart/build {:file file})))
        response (app request)]
    (is (= 200 (:status response)))))

(deftest ^:integration api-clamdscan-virus-detected
  (let [app (fixture/get-app)
        request (-> (mock/request :post "/antivirus/scan")
                    (merge (peridot.multipart/build {:file fixture/eicar-antivirus-test-file-contents})))
        response (app request)
        body (ring.util.request/body-string response)]
    (is (= 400 (:status response)))
    (is (string/starts-with? body "Viruses found!"))))
