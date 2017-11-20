(ns laundry.schemas
   (:require [schema.core :as s]))

(s/defschema Status 
   (s/enum "ok" "error"))

(s/defschema LaundryConfig
   {:port s/Num
    :slow-request-warning s/Num
    :temp-directory s/Str
    :tools s/Str
    :log-level (s/enum :debug :info)})
