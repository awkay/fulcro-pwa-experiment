(ns app.ui.line-item-forms
  (:require
    [com.fulcrologic.fulcro.components :as comp]
    [com.fulcrologic.fulcro.mutations :as m]
    [com.fulcrologic.rad.picker-options :as picker-options]
    [app.model.model :as model]
    [app.model.line-item :as line-item]
    [app.ui.item-forms :as item-forms]
    [com.fulcrologic.rad.type-support.decimal :as math]
    [com.fulcrologic.rad.form :as form]
    [com.fulcrologic.rad.form-options :as fo]
    [com.fulcrologic.fulcro.ui-state-machines :as uism]
    [com.fulcrologic.fulcro.algorithms.normalized-state :as fns]))

(defn add-subtotal* [{:line-item/keys [quantity quoted-price] :as item}]
  (assoc item :line-item/subtotal (math/* quantity quoted-price)))

(form/defsc-form LineItemForm [this props]
  {fo/id             line-item/id
   ::form/confirm    (fn [message]
                       #?(:cljs (js/confirm message)))
   fo/attributes     [line-item/item line-item/quantity line-item/quoted-price line-item/subtotal]
   fo/validator      model/all-attribute-validator
   fo/route-prefix   "line-item"
   fo/title          "Line Items"
   fo/default-values {:line-item/quantity 20}
   fo/layout         [[:line-item/item :line-item/quantity :line-item/quoted-price :line-item/subtotal]]
   fo/triggers       {:derive-fields (fn [new-form-tree] (add-subtotal* new-form-tree))
                      :on-change     (fn [{::uism/keys [state-map fulcro-app] :as uism-env} form-ident k old-value new-value]
                                       (case k
                                         :line-item/item
                                         (let [item-price  (get-in state-map (conj new-value :item/price))
                                               target-path (conj form-ident :line-item/quoted-price)]
                                           (uism/apply-action uism-env assoc-in target-path item-price))
                                         uism-env))}
   fo/field-styles   {:line-item/item :pick-one}
   fo/field-options  {:line-item/item {::picker-options/query-key       :item/all-items
                                       ::picker-options/query-component item-forms/ItemForm
                                       ::picker-options/options-xform   (fn [_ options]
                                                                          (mapv
                                                                            (fn [{:item/keys [id name price]}]
                                                                              {:text (str name " - " (math/numeric->currency-str price)) :value [:item/id id]})
                                                                            (sort-by :item/name options)))
                                       ::picker-options/cache-time-ms   60000}}})
