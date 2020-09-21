(ns com.fulcrologic.fulcro-pwa.serviceworker
  (:require
    ["workbox-routing" :refer [registerRoute]]
    ["workbox-expiration" :refer [ExpirationPlugin]]
    ["workbox-strategies" :refer [CacheFirst StaleWhileRevalidate]]))

(defn supported? [] (boolean (js-in "serviceWorker" js/navigator)))

(defn install! [js-file]
  (when (supported?)
    (js/console.log "Installing worker on load")
    (.addEventListener js/window "load"
      (fn [] (.. js/navigator
               -serviceWorker
               (register js-file)
               (then
                 (fn [reg] (js/console.log "registered" reg))
                 (fn [err] (js/console.error "failed" err))))))))

(defn cache-css! []
  (when (supported?)
    (js/console.log "Caching styles")
    (registerRoute
      (fn [^js event]
        (js/console.log event)
        (let [destination (.. event -request -destination)]
          (= "style" destination)))
      (StaleWhileRevalidate. #js {:cacheName "css-cache"}))))

(defn cache-images! []
  (when (supported?)
    (js/console.log "Caching images")
    (registerRoute
      (fn [^js event]
        (let [destination (.. event -request -destination)]
          (= "image" destination)))
      (CacheFirst.
        #js {:cacheName "css-cache"
             :plugins   #js [(ExpirationPlugin.
                               #js {:maxEntries    20
                                    :maxAgeSeconds (* 7 86400)})]}))))
