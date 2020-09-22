(ns app.worker
  (:require
    [com.fulcrologic.fulcro-pwa.serviceworker :as sworker]
    [taoensso.timbre :as log]))

(defn serve-files-from-cache [^js evt handler]

  (.then
    (js/caches.match (.-request evt))
    (fn [response]
      (if response
        (do
          (log/info "Found cached value for " (.. evt -request -url))
          response)
        (do
          (log/info "No cached version of " (.. evt -request -url))
          (handler evt))))))

(defn fetch! [^js evt]
  (log/info "Doing real network fetch "
    (.. evt -request -url)
    (.. evt -request -method)
    (.. evt -request -credentials))
  (js/fetch (.-request evt)))

(defn wrap-serve-files-from-cache [handler]
  (fn [evt]
    (serve-files-from-cache evt handler)))

(sworker/setup! {:urls       ["https://cdnjs.cloudflare.com/ajax/libs/fomantic-ui/2.8.7/semantic.min.css"
                              "/js/main/main.CDA121C93C9E4CD42E8A7C49D2C7E51F.js"
                              "/favicon.ico"]
                 :middleware (-> fetch!
                               (wrap-serve-files-from-cache))})

