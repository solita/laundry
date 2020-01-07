(ns laundry.schemas
  (:require
   [schema.core :as s]))

(s/defschema Status
  (s/enum "ok" "error"))

(s/defschema LaundryConfig
  {:port s/Num
   :slow-request-warning s/Num
   :tools s/Str
   :basic-auth-password (s/maybe (s/pred fn?))
   :log-level (s/enum :debug :info)})

