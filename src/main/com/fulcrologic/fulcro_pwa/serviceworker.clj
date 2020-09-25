(ns com.fulcrologic.fulcro-pwa.serviceworker
  (:require [clojure.walk :as walk]))

(defmacro then-as
  "Execute `body` as the `.then` clause of the given js/Promise `promise`.

  ```
  (then-as [value (.resolve js/Promise 42)]
    (js/console.log value))
  ```

  Uses the notation of `let`, but is equivalent to:

  ```
  (.then
    (.resolve js/Promise 42)
    (fn [value] (js/console.log value)))
  ```
  "
  [[varname promise] & body]
  `(.then ~promise (fn [~varname] ~@body)))

(defmacro then-if
  "An if-let style construct to make js promises more readable.
  The binding var is available in the good and bad branch. In the good
  branch it is bound to the result, and the error branch it is bound to
  the error.

  These are equivalent:

  ```
  (.then
    (js/navigator.serviceWorker.register js-file)
    (fn [result] (js/console.log \"OK\" result))
    (fn [result] (js/console.error \"Error\" result)))
  ```

  ```
  (then-if [result (js/navigator.serviceWorker.register js-file)]
    (js/console.log \"OK\" result)
    (js/console.log \"Error\" result))
  ```

  You can elide the else, but syntactically if you need more than one statement you must use a `do`.
  "
  [[varname promise] & [then else :as body]]
  (when-not (<= 1 (count body) 2)
    (throw (IllegalArgumentException. "then-if must have one or two forms as a body.")))
  (if else
    `(.then ~promise (fn [~varname] ~then) (fn [~varname] ~else))
    `(.then ~promise (fn [~varname] ~then))))

(defmacro all-promises
  "Walk the collection running `body` on each item as `varname`, where the body returns a js/Promise, then return
   a composite js/Promise.all on that result.

   ```
   (all-promises [nm cache-names]
     (when (not= nm (cache-name))
       (js/caches.delete nm)))
   ```

   is exactly equivalent to:

   ```
   (js/Promise.all
     (.map cache-names (fn [nm]
                         (when (not= nm (cache-name))
                           (js/caches.delete nm)))))
   ```
   "
  [[varname collection] & body]
  `(js/Promise.all
     (.map ~collection (fn [~varname]
                         ~@body))))

(comment
  (walk/macroexpand-all '(then-as [browser (.launch pupeteer)]
                           (as-> (.newPage browser) <>
                             (then-as [page <>]
                               (then-if [result (.goto page "http://www.google.com")]
                                 (.screenshot page #js {:path "screenshot.png"})
                                 (js/console.error result)))
                             (.then <> #(.close browser)))))

  (walk/macroexpand-all '(as-> (open-cache (cache-name)) <>
                           (then-as [cache <>]
                             (log/info (v) "Cache opened. Adding " urls)
                             (add-all! cache urls))
                           (then-if [result <>]
                             (log/info "all done" result)
                             (log/info "failed!" result)))))
