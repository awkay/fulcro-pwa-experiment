(ns com.fulcrologic.fulcro-pwa.caches
  "A thin wrapper around the js Service Worker caches API.")

(defn open-cache
  "Open the given cache by name, returns a js/Promise that will provide the cache"
  [name]
  (js/caches.open name))

(defn add-all!
  "Add all of the given URLs (a vector of strings) to the given js cache. Returns a js/Promise"
  [cache urls]
  (.addAll cache (clj->js urls)))

(defn delete!
  "Delete the cache with the given name."
  [cache-name]
  (js/caches.delete cache-name))

(defn cache-names
  "Returns a js/Promise that resolves to all of the cache names."
  []
  (js/caches.keys))

