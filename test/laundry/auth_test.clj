(ns laundry.auth-test
  (:require
   [clojure.test :refer :all]
   [laundry.test.fixture :as fixture]
   [ring.mock.request :as mock]))

(def api-username "laundry-api")
(def api-password "unittest")

(def test-auth-conf
  (-> fixture/test-conf
      (assoc :basic-auth-password (fn [] api-password))))

(defn base64-encode [s]
  (.encodeToString (java.util.Base64/getEncoder) (.getBytes s)))

(deftest alive-accessible-without-auth
  (let [app (fixture/get-app-with test-auth-conf)
        request (mock/request :get "/alive")
        response (app request)]
    (is (= 200 (:status response)))))

(deftest swagger-accessible-without-auth
  (let [app (fixture/get-app-with test-auth-conf)
        request (mock/request :get "/swagger.json")
        response (app request)]
    (is (= 200 (:status response)))))

(deftest auth-not-accessible-without-basic-auth
  (let [app (fixture/get-app-with test-auth-conf)
        request (mock/request :get "/auth-test")
        response (app request)]
    (is (= 401 (:status response)))))

(deftest auth-accessible-with-basic-auth
  (let [app (fixture/get-app-with test-auth-conf)
        request (-> (mock/request :get "/auth-test")
                    (mock/header "authorization" (str "Basic " (base64-encode (str api-username ":" api-password)))))
        response (app request)]
    (is (= 200 (:status response)))))
