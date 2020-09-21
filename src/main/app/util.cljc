(ns app.util
  #?(:cljs (:refer-clojure :exclude [uuid]))
  (:require [com.fulcrologic.guardrails.core :refer [>defn =>]]
            [clojure.spec.alpha :as s])
  #?(:clj
     (:import (java.util UUID))))

(>defn uuid
  "Generate a UUID the same way via clj/cljs.  Without args gives random UUID. With args, builds UUID based on input (which
  is useful in tests)."
  #?(:clj ([] [=> uuid?] (UUID/randomUUID)))
  #?(:clj ([int-or-str]
           [(s/or :i int? :s string?) => uuid?]
           (if (int? int-or-str)
             (UUID/fromString
               (format "ffffffff-ffff-ffff-ffff-%012d" int-or-str))
             (UUID/fromString int-or-str))))
  #?(:cljs ([] [=> uuid?] (random-uuid)))
  #?(:cljs ([& args]
            [(s/* any?) => uuid?]
            (cljs.core/uuid (apply str args)))))
