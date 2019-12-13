(ns laundry.alive-test
  (:require
   [clojure.test :refer :all]
   [laundry.test.fixture :as fixture]
   [ring.mock.request :as mock]))

(deftest alive
  (let [app (fixture/get-app)
        request (mock/request :get "/alive")
        response (app request)]
    (is (= 200 (:status response)))))
