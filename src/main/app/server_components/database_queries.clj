(ns app.server-components.database-queries
  (:require
    [com.fulcrologic.rad.database-adapters.key-value :as key-value]
    [com.fulcrologic.rad.database-adapters.key-value.key-store :as kv-key-store]
    [taoensso.encore :as enc]
    [taoensso.timbre :as log]
    [konserve.core :as k]
    [clojure.core.async :refer [<!! <! go]]
    [com.fulcrologic.rad.database-adapters.key-value.pathom :as kv-pathom]))

(defn get-all-items
  [env {:category/keys [id] :as query-params}]
  (when-let [{::kv-key-store/keys [store]} (kv-pathom/env->key-store env)]
    (<!!
      (go
        (if id
          (->> (vals (<! (k/get-in store [:item/id])))
            (filter #(#{id} (-> % :item/category second)))
            (mapv #(select-keys % [:item/id])))
          (->> (keys (<! (k/get-in store [:item/id])))
            (mapv (fn [id] {:item/id id}))))))))


(defn get-all-invoices
  [env query-params]
  (when-let [{::kv-key-store/keys [store]} (kv-pathom/env->key-store env)]
    (<!!
      (go
        (->> (keys (<! (k/get-in store [:invoice/id])))
          (mapv (fn [id] {:invoice/id id})))))))


;; Just created for testing
(defn get-all-line-items
  [env query-params]
  (when-let [{::kv-key-store/keys [store]} (kv-pathom/env->key-store env)]
    (<!!
      (go
        (->> (keys (<! (k/get-in store [:line-item/id])))
          (mapv (fn [id] {:line-item/id id})))))))

(defn d-pull [db pull eid]
  (log/error "datomic pull with id" pull eid))

