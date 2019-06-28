(ns laundry.util
  (:require
   [clojure.java.shell :as shell]))

(defn shell-out! [binary-path input-path output-path]
  (try
    (info "exception resistant sh invocation attmept!" )
    (shell/sh binary-path input-path output-path)
    (catch java.io.IOException ex ;; sometimes throws instead of returning error, contrary to docs
      {:out "" :err "" :exit 10000 :exception ex})))


