(ns app.server-components.seeded-connection
  (:require
    [mount.core :refer [defstate]]
    [app.model.model :refer [all-attributes]]
    [com.fulcrologic.guardrails.core :refer [>defn => ?]]
    [app.server-components.config :as config]
    [com.fulcrologic.rad.database-adapters.key-value.write :as kv-write :refer [ident-of value-of]]
    [com.fulcrologic.rad.database-adapters.key-value :as key-value]
    [com.fulcrologic.rad.database-adapters.key-value.key-store :as kv-key-store]
    [app.model.seed :as seed]
    [com.fulcrologic.rad.ids :refer [new-uuid]]
    [com.fulcrologic.rad.type-support.date-time :as dt]))

(defn all-tables!
  "All the tables that we are going to have entities of. This information is in the RAD registry, we just haven't gone
  that far yet"
  []
  [:account/id :item/id :invoice/id :line-item/id :category/id :address/id])

(defn all-entities!
  "There is no check done on the integrity of this data. But when putting it together the functions ident-of and value-of are
  supposed to help. Just make sure that for every ident-of there is at least one value-of of the same entity"
  []
  (let [date-1          (dt/html-datetime-string->inst "2020-01-01T12:00")
        date-2          (dt/html-datetime-string->inst "2020-01-05T12:00")
        widget          (seed/new-item (new-uuid 200) "Widget" 33.99)
        screwdriver     (seed/new-item (new-uuid 201) "Screwdriver" 4.99)
        wrench          (seed/new-item (new-uuid 202) "Wrench" 14.99)
        hammer          (seed/new-item (new-uuid 203) "Hammer" 14.99)
        doll            (seed/new-item (new-uuid 204) "Doll" 4.99)
        robot           (seed/new-item (new-uuid 205) "Robot" 94.99)
        building-blocks (seed/new-item (new-uuid 206) "Building Blocks" 24.99)]
    [widget screwdriver wrench hammer doll robot building-blocks
     (seed/new-invoice date-1
       [(value-of (seed/new-line-item (ident-of doll) 1 5.0M))
        (value-of (seed/new-line-item (ident-of hammer) 1 14.99M))])
     (seed/new-invoice date-2
       [(value-of (seed/new-line-item (ident-of wrench) 1 12.50M))
        (value-of (seed/new-line-item (ident-of widget) 2 32.0M))])]))

(>defn seed!
  "Get rid of all data in the database then build it again from the data structure at all-entities"
  [{::kv-key-store/keys [instance-name] :as key-store}]
  [::key-value/key-store => any?]
  (dt/set-timezone! "America/Los_Angeles")
  (println "SEEDING data (Starting fresh). For" instance-name)
  (let [tables   (all-tables!)
        entities (all-entities!)]
    (kv-write/import key-store tables entities)))

;;
;; We've got a tiny database so let's seed it every time we refresh
;; Far less confusing not to have this :on-reload thing - change the seed function and it will be run!
;; ^{:on-reload :noop}
;;
(defstate kv-connections
  "The connection to the database that has just been freshly populated"
  :start (let [{:keys [main] :as databases} {:main (key-value/start config/config)}]
           (seed! main)
           databases))
