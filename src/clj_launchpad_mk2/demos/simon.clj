(ns clj-launchpad-mk2.demos.simon
  (:gen-class)
  (:require [clj-launchpad-mk2.core :as lp]
            [clj-launchpad-mk2.midi.core :as midi]))

(defn reset [lpad coords & solving?]
  (Thread/sleep 300)

  (doseq [coord coords]
    (lp/clear-cell lpad (first coord) (second coord))))

(defn- light-square [lpad coords colour]
  (doseq [coord coords]
    (lp/light-cell lpad (first coord) (second coord) colour) coords))

(def hotspots {:1 {:coords '((0 3) (1 3) (0 2) (1 2)) :colour lp/GREEN}
               :2 {:coords '((2 3) (3 3) (2 2) (3 2)) :colour lp/RED}
               :3 {:coords '((0 0) (0 1) (1 1) (1 0)) :colour lp/YELLOW}
               :4 {:coords '((2 1) (3 1) (2 0) (3 0)) :colour lp/BLUE}})

(defn show-1 [lpad & options]
  (light-square lpad (:coords (:1 hotspots)) (:colour (:1 hotspots)))
  (reset lpad (:coords (:1 hotspots)) options))

(defn show-2 [lpad & options]
  (light-square lpad (:coords (:2 hotspots)) (:colour (:2 hotspots)))
  (reset lpad (:coords (:2 hotspots)) options))

(defn show-3 [lpad & options]
  (light-square lpad (:coords (:3 hotspots)) (:colour (:3 hotspots)))
  (reset lpad (:coords (:3 hotspots)) options))

(defn show-4 [lpad & options]
  (light-square lpad (:coords (:4 hotspots)) (:colour (:4 hotspots)))
  (reset lpad (:coords (:4 hotspots)) options))

(defn show [lpad id]
  (case id
    0 (show-1 lpad)
    1 (show-2 lpad)
    2 (show-3 lpad)
    3 (show-4 lpad)))

(defn record [lpad id solution]
  (case id
    0 (show-1 lpad {:solving true})
    1 (show-2 lpad {:solving true})
    2 (show-3 lpad {:solving true})
    3 (show-4 lpad {:solving true}))
  (swap! solution conj id))

(defn show-border [lpad]
  (doseq [x (range 0 5)]
    (lp/light-cell lpad x 4 lp/WHITE))

  (doseq [y (range 0 4)]
    (lp/light-cell lpad 4 y lp/WHITE)))

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
  (show-border lpad)
  (doseq [step (range (count sequence))]
    (show lpad (nth sequence step))))

(defn- handle-button-press [lpad solution finished?]
  (fn [msg]
    (let [x (:x msg) y (:y msg)
          coord (conj nil x y)]
      (when (:button-down? msg)
        (cond
          (some #(= % coord) (:coords (:4 hotspots))) (record lpad 0 solution)
          (some #(= % coord) (:coords (:2 hotspots))) (record lpad 1 solution)
          (some #(= % coord) (:coords (:3 hotspots))) (record lpad 2 solution)
          (some #(= % coord) (:coords (:1 hotspots))) (record lpad 3 solution)
          (:mixer-button? msg) (reset! finished? true))))))

(defn- still-solving? [sequence solution finished?]
  (and
   (< (count @solution) (count sequence))
   (not @finished?)
   (not (= sequence @solution))))

(defn solve-round [lpad sequence]
  (let [solution (atom [])
        finished? (atom false)]
    (midi/set-button-press-handler lpad (handle-button-press lpad solution finished?))

    (while (still-solving? sequence solution finished?)
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
    (try
      (doseq [round (range 1 (inc (count sequence)))]
        (show-round lpad (take round sequence) win-count loss-count)
        (case (solve-round lpad (take round sequence))
          :user-quit (lp/scroll-text-once lpad "Bye" 54)
          :solution-passed (swap! win-count inc)
          :solution-failed (swap! loss-count inc))
        (show-scores lpad round loss-count win-count))
      (finally
        (midi/remove-button-press-handler lpad)))))