(ns accurib.doo-runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [accurib.core-test]))

(doo-tests 'accurib.core-test)

