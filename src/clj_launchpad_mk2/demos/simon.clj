(ns clj-launchpad-mk2.demos.simon
  (:gen-class)
  (:require [clj-launchpad-mk2.core :as lp]
            [clj-launchpad-mk2.midi.core :as midi]))

(defn reset [lpad x y & solving?]
  (if solving?
    (Thread/sleep 100)
    (Thread/sleep 1000))

  (lp/clear-cell lpad x y))

(defn show-1 [lpad & options]
  (lp/light-cell lpad 3 4 lp/GREEN)
  (reset lpad 3 4 (:solving options)))

(defn show-2 [lpad & options]
  (lp/light-cell lpad 4 4 lp/RED)
  (reset lpad 4 4 (:solving options)))

(defn show-3 [lpad & options]
  (lp/light-cell lpad 3 3 lp/YELLOW)
  (reset lpad 3 3 (:solving options)))

(defn show-4 [lpad & options]
  (lp/light-cell lpad 4 3 lp/BLUE)
  (reset lpad 4 3 (:solving options)))

(defn show [lpad id]
  (case id
    0 (show-1 lpad)
    1 (show-2 lpad)
    2 (show-3 lpad)
    3 (show-4 lpad)))

(defn record [lpad id solution]
  (case id
    1 (show-1 lpad {:solving true})
    2 (show-2 lpad {:solving true})
    3 (show-3 lpad {:solving true})
    4 (show-4 lpad {:solving true}))
  (swap! solution conj (dec id)))

(defn show-border [lpad colour]
  (doseq [x (range 2 6)]
    (rgb lpad x 2 colour colour colour)
    (rgb lpad x 5 colour colour colour))

  (doseq [y (range 3 5)]
    (rgb lpad 2 y colour colour colour)
    (rgb lpad 2 y colour colour colour)
    (rgb lpad 5 y colour colour colour)
    (rgb lpad 5 y colour colour colour)))

(defn build-sequence [length] (repeatedly length #(rand-int 4)))

(defn show-win [lpad win-count]
  (doseq [win (range @win-count)]
    (lp/light-cell lpad win 7 lp/GREEN)))

(defn show-loss [lpad loss-count]
  (doseq [win (range @loss-count)]
    (lp/light-cell lpad win 6 lp/RED)))

(defn show-round-counter [lpad round-counter]
  (doseq [step (range round-counter)]
    (lp/light-cell lpad 8 step lp/YELLOW))
  (lp/pulse lpad 8 (dec round-counter) lp/YELLOW))

(defn show-scores [lpad round-counter loss-count win-count]
  (doto lpad
    (show-win win-count)
    (show-loss loss-count)
    (show-round-counter round-counter)))

(defn show-round [lpad sequence win-count loss-count]
  (println sequence)
  (lp/clear-grid lpad)
  (show-scores lpad (count sequence) loss-count win-count)
  (show-border lpad 63)
  (doseq [step (range (count sequence))]
    (show lpad (nth sequence step))))

(defn- handle-button-press [lpad solution finished?]
  (fn [msg]
    (let [x (:x msg) y (:y msg)]
      (when (:button-down? msg)
        (cond
          (and (= x 3) (= y 4)) (record lpad 1 solution)
          (and (= x 4) (= y 4)) (record lpad 2 solution)
          (and (= x 3) (= y 3)) (record lpad 3 solution)
          (and (= x 4) (= y 3)) (record lpad 4 solution)
          (:mixer-button? msg) (reset! finished? true))))))

(defn solve-round [lpad sequence]
  (let [solution (atom [])
        finished? (atom false)]
    (midi/set-button-press-handler lpad (handle-button-press lpad solution false))

    (while (and
            (< (count @solution) (count sequence))
            (not @finished?)
            (not (= sequence @solution)))
      (Thread/sleep 100))
    (midi/remove-button-press-handler lpad)
    (cond
      @finished? :user-quit
      (= sequence @solution) :solution-passed
      (>= (count @solution) (count sequence)) :solution-failed
      :else :unknown)))

#_{:clj-kondo/ignore [:unused-binding]}
(defn run [opts]
  (let [lpad (lp/open)
        win-count (atom 0)
        loss-count (atom 0)
        sequence (build-sequence 8)]
    (midi/remove-button-press-handler lpad)
    (doseq [round (range (count sequence))]
      (show-round lpad (take (inc round) sequence) win-count loss-count)
      (case (solve-round lpad (take (inc round) sequence))
        :user-quit (lp/scroll-text-once lpad "Bye" 54)
        :solution-passed (swap! win-count inc)
        :solution-failed (swap! loss-count inc)))
    (show-scores lpad 8 loss-count win-count)))



