(ns ^:figwheel-no-load accurib.app
  (:require [accurib.core :as core]
            [devtools.core :as devtools]))

(enable-console-print!)

(devtools/install!)

(core/init!)
