(ns accurib.env
  (:require [selmer.parser :as parser]
            [clojure.tools.logging :as log]
            [accurib.dev-middleware :refer [wrap-dev]]))

(def defaults
  {:init
   (fn []
     (parser/cache-off!)
     (log/info "\n-=[accurib started successfully using the development profile]=-"))
   :stop
   (fn []
     (log/info "\n-=[accurib has shut down successfully]=-"))
   :middleware wrap-dev})
