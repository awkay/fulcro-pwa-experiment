(ns app.client
  (:require
    [app.application :refer [SPA]]
    [app.ui.root :as root]
    [com.fulcrologic.fulcro.application :as app]
    [com.fulcrologic.fulcro.components :as comp]
    [com.fulcrologic.fulcro.routing.dynamic-routing :as dr]
    [com.fulcrologic.rad.application :as rad-app]
    [com.fulcrologic.rad.rendering.semantic-ui.semantic-ui-controls :as sui]
    [com.fulcrologic.fulcro-pwa.serviceworker :as sworker]
    [com.fulcrologic.rad.routing.history :as history]
    [com.fulcrologic.rad.routing.html5-history :refer [html5-history]]
    [com.fulcrologic.rad.type-support.date-time :as datetime]
    [taoensso.timbre :as log]))

(defn ^:export refresh []
  (log/info "Hot code Remount")
  (rad-app/install-ui-controls! SPA sui/all-controls)
  (comp/refresh-dynamic-queries! SPA)
  (app/mount! SPA root/Root "app"))

(defn ^:export init []
  (log/info "Application starting.")
  (datetime/set-timezone! "America/Los_Angeles")
  (history/install-route-history! SPA (html5-history))
  (app/set-root! SPA root/Root {:initialize-state? true})
  (dr/initialize! SPA)
  (rad-app/install-ui-controls! SPA sui/all-controls)
  (app/mount! SPA root/Root "app" {:initialize-state? false}))
