(ns app.model.invoice
  (:require
    [com.fulcrologic.rad.attributes :refer [defattr]]
    [com.fulcrologic.rad.attributes-options :as ao]
    [com.fulcrologic.rad.form :as form]
    [com.fulcrologic.rad.report-options :as ro]
    [com.fulcrologic.rad.type-support.decimal :as math]
    #?(:clj [app.server-components.database-queries :as queries])))

(defattr id :invoice/id :uuid
  {ao/identity? true
   ao/schema    :production})

(defattr date :invoice/date :instant
  {::form/field-style :date-at-noon
   ao/identities      #{:invoice/id}
   ao/schema          :production})

(defattr line-items :invoice/line-items :ref
  {ao/target      :line-item/id
   ao/cardinality :many
   ao/identities  #{:invoice/id}
   ao/schema      :production})

(defattr total :invoice/total :decimal
  {ao/identities      #{:invoice/id}
   ao/schema          :production
   ro/field-formatter (fn [report v] (math/numeric->currency-str v))
   ao/read-only?      true})

(defattr all-invoices :invoice/all-invoices :ref
  {ao/target     :invoice/id
   ao/pc-output  [{:invoice/all-invoices [:invoice/id]}]
   ao/pc-resolve (fn [{:keys [query-params] :as env} _]
                   #?(:clj
                      {:invoice/all-invoices (queries/get-all-invoices env query-params)}))})


(def attributes [id date line-items all-invoices total])
#?(:clj
   (def resolvers []))
