(ns app.model.model
  (:require
    [com.fulcrologic.rad.attributes :as attr]
    [app.model.item :as item]
    [app.model.line-item :as line-item]
    [app.model.invoice :as invoice]))

(def all-attributes (vec (concat
                           item/attributes
                           invoice/attributes
                           line-item/attributes)))

(def all-attribute-validator (attr/make-attribute-validator all-attributes))
