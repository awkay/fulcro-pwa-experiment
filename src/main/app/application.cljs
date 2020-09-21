(ns app.application
  (:require
    [com.fulcrologic.fulcro.networking.http-remote :as net]
    [com.fulcrologic.fulcro.application :as app]
    [com.fulcrologic.fulcro.algorithms.tx-processing.synchronous-tx-processing :as stx]
    [com.fulcrologic.rad.routing.html5-history :as hist5]
    [com.fulcrologic.fulcro.components :as comp]))

(def secured-request-middleware
  ;; The CSRF token is embedded via server_components/html.clj
  (->
    (net/wrap-csrf-token (or js/fulcro_network_csrf_token "TOKEN-NOT-IN-HTML!"))
    (net/wrap-fulcro-request)))

(defonce SPA
  (stx/with-synchronous-transactions
    (app/fulcro-app
      {;; This ensures your client can talk to a CSRF-protected server.
       ;; See middleware.clj to see how the token is embedded into the HTML
       :client-did-mount (fn [app]
                           (let [Main (comp/registry-key->class :app.ui.root/Main)]
                             (hist5/restore-route! app Main {})))
       :remotes          {:remote (net/fulcro-http-remote
                                    {:url                "/api"
                                     :request-middleware secured-request-middleware})}})))
