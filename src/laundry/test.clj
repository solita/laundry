(ns laundry.test
   (:require [compojure.api.sweet :as sweet :refer :all]
             [ring.util.http-response :refer [ok status content-type] :as resp]
             [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
             [ring.swagger.upload :as upload]
             [taoensso.timbre :as timbre :refer [trace debug info warn]]
             [schema.core :as s]
             [clojure.java.shell :refer [sh]]
             [laundry.machines :as machines]
             [clojure.java.io :as io]))

(s/defn api-test [env]
   (let [res (sh (str (:tools env) "/bin/access-test"))]
      (ok (str (:out res)))))

(machines/add-api-generator! 
   (fn [env] 
      (GET "/access-test" []
         :summary "check sandbox features"
         (info "access-test")
         (api-test env))))
