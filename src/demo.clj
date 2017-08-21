(ns demo
  (:require [clj-launchpad-mk2 :refer :all]))

(defn wipe-left-to-right [lpad]
  (doseq [x (range 0 8)]
  (light-column lpad x 66)
  (Thread/sleep 200)
  (light-column lpad x 0)))

(defn wipe-top-to-bottom [lpad]
  (doseq [y (range 0 8)]
  (light-row lpad y 66)
  (Thread/sleep 200)))

(defn flash-border [lpad]
  (doseq [y (range 0 8)
          x [0 7]]
    (flash lpad x y 60))
  (doseq [x (range 0 8)
          y [0 7]]
    (flash lpad x y 60)))

(defn pulse-middle [lpad]
  (doseq [x (range 1 7)
          y (range 1 7)]
    (pulse lpad x y 60)))

(defn brightness-quadrants [lpad]
  (doseq [x (range 0 4)
          y (range 0 4)]
    (rgb lpad x y (rand-int 64) 0 0))
  (doseq [x (range 0 4)
          y (range 4 8)]
    (rgb lpad x y  0 (rand-int 64) 0))
  (doseq [x (range 4 8)
          y (range 4 8)]
    (rgb lpad x y  0 0 (rand-int 64)))
  (doseq [x (range 4 8)
          y (range 0 4)
          :let [brightness (rand-int 64)]]
    (rgb lpad x y  brightness brightness brightness)))

(defn light-pressed-button [lpad]
  (scroll-text-once lpad "Touch me!" 54)

  (set-button-press-handler     
    lpad    
    (fn 
      [msg timestamp]
      (light-cell 
        lpad 
        (:x msg) 
        (:y msg) 
        (- 127 (+ (:x msg) (:y msg))))))  )

(defn -main [& args]
  (let [lpad (open)]
    (light-grid lpad 23)      
    (clear-grid lpad)       
    (wipe-left-to-right lpad)
    (wipe-top-to-bottom lpad)
    (flash-border lpad)
    (pulse-middle lpad)
    (Thread/sleep 1000)
    (clear-grid lpad)       
    (brightness-quadrants lpad)
    (Thread/sleep 2000)
    (clear-grid lpad)       
    (light-pressed-button lpad)
    (close lpad)
  ))
