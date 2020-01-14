(ns laundry.swagger-test
  (:require
   [clojure.test :refer :all]
   [laundry.test.fixture :as fixture]
   [ring.mock.request :as mock]))

(deftest swagger-json
  (let [app (fixture/get-app)
        request (mock/request :get "/swagger.json")
        response (app request)]
    (is (= 200 (:status response)))
    (is (= "application/json; charset=utf-8" (get-in response [:headers "Content-Type"])))))

(deftest swagger-ui
  (let [app (fixture/get-app)
        request (mock/request :get "/api-docs/index.html")
        response (app request)]
    (is (= 200 (:status response)))))
