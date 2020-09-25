(ns app.worker
  (:require
    [com.fulcrologic.fulcro-pwa.serviceworker :as sworker
     :refer-macros [then-as then-if]]
    [com.fulcrologic.fulcro-pwa.caches :as caches]
    [taoensso.timbre :as log]
    [clojure.string :as str]))

(defn serve-files-from-cache [^js evt handler]
  (if (= "GET" (.. evt -request -method))
    (then-as [response (caches/match (.-request evt))]
      (if response
        (do
          (log/info "Found cached value for " (.. evt -request -url))
          response)
        (do
          (log/info "No cached version of " (.. evt -request -url))
          (handler evt))))
    (handler evt)))

(defn fetch! [^js evt]
  (log/info "Doing real network fetch ")
  (js/console.log evt)
  (if (.. evt -request -bodyUsed)
    (log/info "Skipping fetch. Request body has already been used?")
    (js/fetch (.-request evt))))

(defn wrap-ignore-posts [handler]
  (fn [^js evt]
    (if (= "POST" (.. evt -request -method))
      nil
      (handler evt))))

(defn wrap-ignore-browser-extensions [handler]
  (fn [^js evt]
    (if (str/starts-with? (or (.. evt -request -url) "") "chrome-extension")
      nil
      (handler evt))))

(defn respond-with!
  "Respond with a cache entry that matches uri"
  [^js evt uri]
  (.respondWith evt (caches/match uri)))

(defn wrap-alternate
  "Convert the request so that it serves the given `uri` for any request for which the predicate returns true. The
  predicate should be `(fn [url] boolean)`."
  [handler predicate uri]
  (fn [^js evt]
    (let [url      (or (.. evt -request -url) "")
          matches? (predicate url)]
      (if matches?
        (do
          (log/info "Serving " url " as " uri)
          (respond-with! evt uri))
        (handler evt)))))

(defn wrap-serve-files-from-cache [handler]
  (fn [evt]
    (serve-files-from-cache evt handler)))

(defn index?
  "Returns true if the given URL should be treated as if it were /index.html"
  [url]
  (and
    (str/includes? url "localhost:3000")
    (not (str/includes? url "/img/"))
    (not (str/includes? url "/js/"))
    (not (str/includes? url "/css/"))))

(sworker/setup! {:urls       ["/index.html"
                              "https://cdnjs.cloudflare.com/ajax/libs/fomantic-ui/2.8.7/semantic.min.css"
                              "/js/main/main.D18E0208E05ED436A4B7B7FFF6EDB5EA.js"
                              "/favicon.ico"]
                 :middleware (-> fetch!
                               (wrap-serve-files-from-cache)
                               (wrap-alternate index? "/index.html")
                               (wrap-ignore-browser-extensions)
                               (wrap-ignore-posts))})

