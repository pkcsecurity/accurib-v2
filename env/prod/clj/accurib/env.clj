(ns accurib.env
  (:require [clojure.tools.logging :as log]))

(def defaults
  {:init
   (fn []
     (log/info "\n-=[accurib started successfully]=-"))
   :stop
   (fn []
     (log/info "\n-=[accurib has shut down successfully]=-"))
   :middleware identity})
