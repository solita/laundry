(ns laundry.notfound-test
  (:require
   [clojure.test :refer :all]
   [laundry.test.fixture :as fixture]
   [ring.mock.request :as mock]))

(deftest not-found
  (let [app (fixture/get-app)
        request (mock/request :get "/not-found")
        response (app request)]
    (is (= 404 (:status response)))))
