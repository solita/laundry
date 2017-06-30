(ns laundry.machines)

(defonce command-line-rules (atom []))

(defn add-command-line-rule! [rule]
   (swap! command-line-rules
      (fn [rules]
         (conj rules rule))))

(defonce api-generators (atom []))

(defn add-api-generator! [generator]
   (swap! api-generators
      (fn [gens]
         (conj gens generator))))

(defn generate-apis [config]
   (map 
      (fn [gen] (gen config))
      (deref api-generators)))
   

