(ns app.server-components.parser
  (:require
    [app.model.invoice :as invoice]
    [app.model.item :as item]
    [app.model.model :refer [all-attributes]]
    [app.server-components.auto-resolvers :refer [automatic-resolvers]]
    [app.server-components.config :as config]
    [app.server-components.delete-middleware :as delete]
    [app.server-components.save-middleware :as save]
    [app.server-components.seeded-connection :refer [kv-connections]]
    [com.fulcrologic.rad.attributes :as attr]
    [com.fulcrologic.rad.database-adapters.key-value.pathom :as kv-pathom]
    [com.fulcrologic.rad.form :as form]
    [com.fulcrologic.rad.pathom :as pathom]
    [com.fulcrologic.rad.type-support.date-time :as dt]
    [com.wsscode.pathom.core :as p]
    [mount.core :refer [defstate]]))

(defstate parser
  :start
  (pathom/new-parser config/config
    [(attr/pathom-plugin all-attributes)
     (form/pathom-plugin save/middleware delete/middleware)
     (kv-pathom/pathom-plugin (fn [env] {:production (:main kv-connections)}))
     {::p/wrap-parser
      (fn transform-parser-out-plugin-external [parser]
        (fn transform-parser-out-plugin-internal [env tx]
          (dt/with-timezone "America/Los_Angeles"
            (if (and (map? env) (seq tx))
              (parser env tx)
              {}))))}]
    [automatic-resolvers
     form/resolvers
     invoice/resolvers
     item/resolvers]))
