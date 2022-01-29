(ns clj-launchpad-mk2.demos.colours
  (:gen-class)
  #_{:clj-kondo/ignore [:refer-all]}
  (:require [clj-launchpad-mk2.core :refer :all]
            [clj-launchpad-mk2.midi.core :as midi]))

(defn coord-to-colour [x y] (+ (* 8 y) x))

(defn- show-colours [lpad base]
  (doseq [y (range 8)
          x (range 8)]
    (light-cell lpad x y (coord-to-colour x y))))

(defn- handle-button-press [lpad]
  (fn [msg]
    (let [x (:x msg) y (:y msg)]
      (when (:button-up? msg)
        (scroll-text-once lpad (str (coord-to-colour x y)) WHITE)))))

#_{:clj-kondo/ignore [:unused-binding]}
(defn -main [& args]
  (let [lpad (open)]
    (doto lpad
      (midi/set-button-press-handler (handle-button-press lpad))
      (clear-grid)
      (show-colours 0))))
