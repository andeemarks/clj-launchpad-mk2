(ns ^:no-doc clj-launchpad-mk2.time
  (:gen-class)
  #_{:clj-kondo/ignore [:refer-all]}
  (:require [clojure.java.shell :refer [sh]]
            [clojure.string :refer [trim]]
            [clj-launchpad-mk2.core :refer :all]))

(defn show-time [lpad]
  (let [time (trim (:out (sh "date" "+%I:%M")))]
    (doto lpad
      (clear-grid)
      (scroll-text-once time 54))))

(defn -main [& args]
  (show-time (open)))
