(ns app.model.line-item
  (:require
    [com.fulcrologic.rad.attributes :refer [defattr]]
    [com.fulcrologic.rad.attributes-options :as ao]))

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
  {ao/required?  true
   ao/identities #{:line-item/id}
   ao/schema     :production})

(defattr quoted-price :line-item/quoted-price :decimal
  {ao/identities #{:line-item/id}
   ao/schema     :production})

(defattr subtotal :line-item/subtotal :decimal
  {ao/read-only? true
   ao/identities #{:line-item/id}
   ao/schema     :production})

(def attributes [id item quantity quoted-price subtotal])
