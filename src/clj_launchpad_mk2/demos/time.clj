(ns ^:no-doc clj-launchpad-mk2.demos.time
  (:gen-class)
  (:require [clojure.java.shell :refer [sh]]
            [clojure.string :refer [trim]]
            [clj-launchpad-mk2.core :refer [open clear-grid scroll-text-once]]))

(defn show-time [lpad]
  (let [time (trim (:out (sh "date" "+%I:%M")))]
    (doto lpad
      (clear-grid)
      (scroll-text-once time 54))))

(defn run [opts]
  (show-time (open)))
