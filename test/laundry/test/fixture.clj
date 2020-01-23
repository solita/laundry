(ns laundry.test.fixture
  (:require
   [laundry.machines :as machines]
   [laundry.server :as server]))

(def test-conf
  {:tools "programs"})

(defn get-app-with [conf]
  (let [api (machines/generate-apis conf)
        app (server/make-handler api conf)]
    app))

(defn get-app []
  (get-app-with test-conf))
