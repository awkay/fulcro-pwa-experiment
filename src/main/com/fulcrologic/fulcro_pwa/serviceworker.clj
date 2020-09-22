(ns com.fulcrologic.fulcro-pwa.serviceworker
  (:require [clojure.string :as str]
            [taoensso.timbre :as log]))

(defn installation-script
  "Creates a vanilla js script that registers the given js file as the page's service worker on the page's load event."
  [js-file]
  (when (str/includes? (subs js-file 1) "/")
    (log/warn "Your js worker file in not being served from URI root. This will scope the serviceworker so that it can only service items in that path."))
  (str
    "
if('serviceWorker' in navigator)
{
   window.addEventListener('load',
     function() {
       navigator.serviceWorker.register('" js-file "')
       .then(function(reg) { console.log(reg); },
             function(err) {console.log(err);});
     });
}"))


