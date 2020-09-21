(ns app.ui.root
  (:require
    [com.fulcrologic.fulcro.dom :as dom :refer [div ul li p h3 button b]]
    [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
    [com.fulcrologic.fulcro.routing.dynamic-routing :as dr]
    [com.fulcrologic.fulcro.ui-state-machines :as uism :refer [defstatemachine]]
    [com.fulcrologic.fulcro.mutations :refer [defmutation]]
    [app.ui.invoice-forms :refer [InvoiceForm InvoiceList]]
    [app.ui.item-forms :refer [ItemForm]]
    [com.fulcrologic.rad.routing :as rroute]
    [com.fulcrologic.rad.form :as form]))

(defsc Main [this props]
  {:query         [:main/welcome-message]
   :initial-state {:main/welcome-message "Hi!"}
   :ident         (fn [] [:component/id :main])
   :route-segment ["main"]}
  (div :.ui.container.segment
    (h3 "Welcome!")))

(dr/defrouter TopRouter [this {:keys [current-state route-factory route-props]}]
  {:always-render-body? true
   :router-targets      [Main InvoiceList ItemForm InvoiceForm]}
  ;; Normal Fulcro code to show a loader on slow route change (assuming Semantic UI here, should
  ;; be generalized for RAD so UI-specific code isn't necessary)
  (dom/div
    (dom/div :.ui.loader {:classes [(when-not (= :routed current-state) "active")]})
    (when route-factory
      (route-factory route-props))))

(def ui-top-router (comp/factory TopRouter))

(defsc TopChrome [this {:root/keys [router]}]
  {:query         [{:root/router (comp/get-query TopRouter)}
                   [::uism/asm-id ::TopRouter]]
   :ident         (fn [] [:component/id :top-chrome])
   :initial-state {:root/router {}}}
  (let [current-tab (some-> (dr/current-route this this) first keyword)]
    (div :.ui.container
      (div :.ui.secondary.pointing.menu
        (dom/a :.item {:classes [(when (= :main current-tab) "active")]
                       :onClick (fn [] (rroute/route-to! this Main {}))} "Main")
        (dom/a :.item {:onClick (fn [] (rroute/route-to! this InvoiceList {}))} "View Invoices")
        (dom/a :.item {:onClick (fn [] (form/create! this InvoiceForm))} "New Invoice")
        )
      (div :.ui.grid
        (div :.ui.row
          (ui-top-router router))))))

(def ui-top-chrome (comp/factory TopChrome))

(defsc Root [this {:root/keys [top-chrome]}]
  {:query         [{:root/top-chrome (comp/get-query TopChrome)}]
   :initial-state {:root/top-chrome {}}}
  (ui-top-chrome top-chrome))
