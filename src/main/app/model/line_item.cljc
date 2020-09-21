(ns app.model.line-item
  (:require
    [com.fulcrologic.rad.attributes :refer [defattr]]
    [com.fulcrologic.rad.attributes-options :as ao]
    [com.fulcrologic.rad.form-options :as fo]))

(defattr id :line-item/id :uuid
  {ao/identity? true
   ao/schema    :production})

(defattr item :line-item/item :ref
  {ao/target      :item/id
   ao/required?   true
   ao/cardinality :one
   ao/identities  #{:line-item/id}
   ao/schema      :production})

(defattr quantity :line-item/quantity :int
  {ao/required?          true
   ao/identities         #{:line-item/id}
   ao/valid?             (fn [v] (<= 1 v 5))
   fo/validation-message (fn [v] (str v " is invalid. Must be between 1 and 5"))
   ao/schema             :production})

(defattr quoted-price :line-item/quoted-price :decimal
  {ao/identities #{:line-item/id}
   ao/schema     :production})

(defattr subtotal :line-item/subtotal :decimal
  {ao/read-only? true
   ao/identities #{:line-item/id}
   ao/schema     :production})

(def attributes [id item quantity quoted-price subtotal])
