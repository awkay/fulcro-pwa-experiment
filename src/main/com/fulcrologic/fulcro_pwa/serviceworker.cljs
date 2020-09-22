(ns com.fulcrologic.fulcro-pwa.serviceworker
  (:require
    [cljs.core.async :as async :refer [go]]
    [cljs.core.async.interop]
    [taoensso.timbre :as log]))

(goog-define service-worker-version 1)
(goog-define CACHE_NAME "fulcro-pwa-cache")
(defn cache-name [] (str CACHE_NAME "=" service-worker-version))

(defn v [] (str "(worker version " service-worker-version ")"))

(defn supported?
  "Returns true if service workers are supported on the current browser."
  []
  (boolean (js-in "serviceWorker" js/navigator)))

(defn setup!
  "Set up the service worker and pre-cache the given urls. If `middleware` is defined then it will be used
   to process fetch events."
  [{:keys [urls middleware]}]
  (when (supported?)
    (log/info "Service worker " (v))
    (.addEventListener js/self "install"
      (fn [^js evt]
        (log/info (v) "Performing install steps for service worker.")
        (.waitUntil evt
          (.then
            (fn [cache]
              (log/info (v) "Cache opened. Adding " urls)
              (.addAll cache (clj->js urls)))
            (.open js/caches (cache-name))))))

    (.addEventListener js/self "activate"
      (fn [^js evt]
        (log/info "Activating " (v))
        (.waitUntil evt
          (.then
            (fn [^js cache-names]
              (js/Promise.all
                (.map cache-names (fn [nm]
                                    (when (not= nm (cache-name))
                                      (log/info "Cleaning up old cache " nm)
                                      (js/caches.delete nm))))))
            (js/cache.keys)))))

    (when middleware
      (.addEventListener js/self "fetch" (fn [evt] (middleware evt))))))



