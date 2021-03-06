(ns app.model.seed
  (:require
    [com.fulcrologic.rad.type-support.decimal :as math]
    [com.fulcrologic.rad.ids :refer [new-uuid]]
    [com.fulcrologic.rad.attributes :as attr]))

(defn new-item
  "Seed helper. Uses street at db/id for tempid purposes."
  [id name price & {:as extras}]
  (let [table :item/id
        value (merge
                {:item/id    id
                 :item/name  name
                 :item/price (math/numeric price)}
                extras)]
    [table id value]))

(defn new-line-item [item quantity price & {:as extras}]
  (let [id    (get extras :line-item/id (new-uuid))
        table :line-item/id
        value (merge
                {:line-item/id           id
                 :line-item/item         item
                 :line-item/quantity     quantity
                 :line-item/quoted-price (math/numeric price)
                 :line-item/subtotal     (math/* quantity price)}
                extras)]
    [table id value]))

(defn new-invoice [date line-items & {:as extras}]
  (let [table :invoice/id
        id    (new-uuid)
        value (merge
                {:invoice/id         id
                 :invoice/line-items line-items
                 :invoice/total      (reduce
                                       (fn [total {:line-item/keys [subtotal]}]
                                         (math/+ total subtotal))
                                       (math/zero)
                                       line-items)
                 :invoice/date       date}
                extras)]
    [table id value]))

