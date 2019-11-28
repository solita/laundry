(ns laundry.test
  (:require
   [clojure.java.io :as io]
   [clojure.java.shell :refer [sh]]
   [compojure.api.sweet :as sweet :refer :all]
   [laundry.machines :as machines]
   [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
   [ring.swagger.upload :as upload]
   [ring.util.http-response :refer [ok status content-type] :as resp]
   [schema.core :as s]
   [taoensso.timbre :as timbre :refer [trace debug info warn]]))

(s/defn api-test [env]
  (let [res (sh (str (:tools env) "/bin/access-test"))]
    (ok (str (:out res)))))

(machines/add-api-generator!
 (fn [env]
   (GET "/access-test" []
     :summary "check sandbox features"
     (info "access-test")
     (api-test env))))
