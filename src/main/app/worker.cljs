(ns app.worker
  (:require
    [com.fulcrologic.fulcro-pwa.serviceworker :as sworker]))

(js/console.log "Installing worker")
(sworker/cache-css!)
(sworker/cache-images!)
