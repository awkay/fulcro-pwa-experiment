(ns app.ui.item-forms
  (:require
    [app.model.item :as item]
    [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
    [com.fulcrologic.rad.form :as form]
    [com.fulcrologic.rad.form-options :as fo]
    [com.fulcrologic.rad.report :as report]
    [com.fulcrologic.rad.report-options :as ro]))


(form/defsc-form ItemForm [this props]
  {fo/id           item/id
   ::form/confirm  (fn [message]
                     #?(:cljs (js/confirm message)))
   fo/attributes   [item/item-name
                    item/description
                    item/in-stock
                    item/price]
   fo/route-prefix "item"
   fo/title        "Edit Item"})

(report/defsc-report InventoryReport [this props]
  {ro/title               "Inventory Report"
   ro/source-attribute    :item/all-items
   ro/row-pk              item/id
   ro/columns             [item/item-name item/price item/in-stock]

   ;; If defined: sort is applied to rows after filtering (client-side)
   ro/initial-sort-params {:sort-by          :item/name
                           :sortable-columns #{:item/name}
                           :ascending?       true}

   ro/form-links          {item/item-name ItemForm}

   ro/run-on-mount?       true
   ro/route               "item-inventory-report"})
