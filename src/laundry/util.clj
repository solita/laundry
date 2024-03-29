(ns laundry.util
  (:require
   [clojure.java.shell :as shell]
   [taoensso.timbre :as timbre :refer [warn]]))

(defn shell-out! [& args]
  (try
    (apply shell/sh args)
    (catch java.io.IOException ex ;; sometimes throws instead of returning error, contrary to docs
      (warn ex "java.shell/sh threw exception, translating as exit value 10000")
      {:out "" :err "" :exit 10000 :exception ex})))
