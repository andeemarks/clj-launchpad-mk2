(ns demo
  (:gen-class)
  (:require [clj-launchpad-mk2 :refer :all]))

(defn- wipe-left-to-right [lpad]
  (doseq [x (range 0 8)]
  (light-column lpad x 66)
  (Thread/sleep 200)
  (light-column lpad x 0)))

(defn- wipe-top-to-bottom [lpad]
  (doseq [y (range 0 8)]
  (light-row lpad y 66)
  (Thread/sleep 200)))

(defn- flash-border [lpad]
  (doseq [y (range 0 8)
          x [0 7]]
    (flash lpad x y 60))
  (doseq [x (range 0 8)
          y [0 7]]
    (flash lpad x y 60)))

(defn- pulse-middle [lpad]
  (doseq [x (range 1 7)
          y (range 1 7)]
    (pulse lpad x y 60))
  (Thread/sleep 1000))

(defn- brightness-quadrants [lpad]
  (doseq [x (range 0 4)
          y (range 0 4)
          :let [brightness (* 4 (+ x (* 4 y)))]]
    (rgb lpad x y brightness 0 0) ; bottom-left => red
    (rgb lpad (+ x 4) y  0 brightness 0) ; bottom-right => green
    (rgb lpad x (+ y 4)  0 0 brightness) ; top-left => blue
    (rgb lpad (+ x 4) (+ y 4) brightness brightness brightness) ; top-right => white/gray
    )
  (Thread/sleep 2000))

(defn- handle-button-press [lpad]
  (fn [msg timestamp]
    ; (println msg)
    (if (:mixer-button? msg)
      (doto lpad
        (remove-button-press-handler)
        (clear-grid)
        (close))
      (let [x (:x msg)
            y (:y msg)
            color (- 127 (+ x y))]
        (light-cell lpad x y color)))))

(defn- light-pressed-button [lpad]
  (scroll-text-once lpad "Touch me!" 54)

  (set-button-press-handler lpad (handle-button-press lpad)))

(defn -main [& args]
  (let [lpad (open)]
    (doto lpad
      (clear-grid)       
      (wipe-left-to-right)
      (wipe-top-to-bottom)
      (flash-border)
      (pulse-middle)
      (clear-grid)       
      (brightness-quadrants)
      (clear-grid)       
      (light-pressed-button))))
