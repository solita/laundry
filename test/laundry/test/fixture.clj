(ns laundry.test.fixture
  (:require
   [laundry.machines :as machines]
   [laundry.server :as server]))

(def test-conf
  {:tools "programs"})

(defn get-app []
  (let [api (machines/generate-apis test-conf)
        app (server/make-handler api test-conf)]
    app))
