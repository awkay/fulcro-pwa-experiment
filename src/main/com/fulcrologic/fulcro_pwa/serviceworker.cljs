(ns com.fulcrologic.fulcro-pwa.serviceworker
  (:require-macros [com.fulcrologic.fulcro-pwa.serviceworker :refer [then-as all-promises then-if]])
  (:require
    [com.fulcrologic.fulcro-pwa.caches :as caches]
    [cljs.core.async :as async :refer [go]]
    [cljs.core.async.interop :refer-macros [<p!]]
    [taoensso.timbre :as log]))

(defn wait-until
  "A wrapper for service worker event.waitUntil. Returns a core async channel on which the result will appear."
  [^js evt promise]
  (.waitUntil evt promise))

(defn add-listener!
  "Add a listener to the service worker. MUST be called from WITHIN the service worker code."
  [event f] (.addEventListener js/self (name event) f))
(defn register! [js-file] (js/navigator.serviceWorker.register js-file))

(goog-define service-worker-version 6)
(goog-define CACHE_NAME "fulcro-pwa-cache")
(defn cache-name [] (str CACHE_NAME "-" service-worker-version))

(defn v [] (str "(worker version " service-worker-version ")"))

(defn install!
  "Set up the given js-file as the service worker for this page. Must be called from the main application."
  [js-file]
  (.addEventListener js/window "load"
    (fn []
      (then-if [result (register! js-file)]
        (js/console.log "Registered" result)
        (js/console.error "Failed to register" result)))))

(defn setup!
  "Set up the service worker and pre-cache the given urls. If `middleware` is defined then it will be used
   to process fetch events."
  [{:keys [urls middleware]}]
  (log/info "Service worker " (v))
  (add-listener! :install
    (fn [^js evt]
      (log/info (v) "Performing install steps for service worker.")
      (wait-until evt
        (then-as [cache (caches/open-cache (cache-name))]
          (log/info (v) "Cache opened. Adding " urls)
          (then-if [finished (caches/add-all! cache urls)]
            (log/info "Cached")
            (log/error "failed" finished))))))


  (add-listener! :activate
    (fn [^js evt]
      (log/info "Activating " (v))
      #_(wait-until evt
        (then-as [cache-names (caches/cache-names)]
          (all-promises [nm cache-names]
            (when (not= nm (cache-name))
              (log/info "Cleaning up old cache " nm)
              (caches/delete! nm)))))))

  (when middleware
    (add-listener! :fetch (fn [evt] (some->> (middleware evt) (.respondWith evt))))))
