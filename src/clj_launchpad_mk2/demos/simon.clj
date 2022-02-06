(ns clj-launchpad-mk2.demos.simon
  (:gen-class)
  (:require [clj-launchpad-mk2.core :as lp]
            [clj-launchpad-mk2.midi.core :as midi]))

(defn reset [lpad coords]
  (Thread/sleep 300)
  (doseq [coord coords]
    (lp/clear-cell lpad (first coord) (second coord))))

(defn- light-square [lpad coords colour]
  (doseq [coord coords]
    (lp/light-cell lpad (first coord) (second coord) colour)))

(def hotspots {:0 {:coords '((0 3) (1 3) (0 2) (1 2)) :colour lp/GREEN}
               :1 {:coords '((2 3) (3 3) (2 2) (3 2)) :colour lp/RED}
               :2 {:coords '((0 0) (0 1) (1 1) (1 0)) :colour lp/YELLOW}
               :3 {:coords '((2 1) (3 1) (2 0) (3 0)) :colour lp/BLUE}})

(defn show [lpad id]
  (let [hotspots (get hotspots (keyword (str id)))]
    (light-square lpad (:coords hotspots) (:colour hotspots))
    (reset lpad (:coords hotspots))))

(defn record [lpad id solution]
  (show lpad id)
  (swap! solution conj id))

(defn show-border [lpad]
  (doseq [x (range 0 5)]
    (lp/light-cell lpad x 4 lp/WHITE))

  (doseq [y (range 0 4)]
    (lp/light-cell lpad 4 y lp/WHITE)))

(defn build-sequence [length] (repeatedly length #(rand-int 4)))

(defn show-round-counter [lpad round-counter]
  (doseq [step (range round-counter)]
    (lp/light-cell lpad 8 step lp/YELLOW))
  (lp/pulse lpad 8 (dec round-counter) lp/YELLOW))

(defn show-results [lpad result-tally]
  (doseq [result (range 8)]
    (lp/pulse lpad result 7 lp/YELLOW))

  (doseq [result (range (count @result-tally))]
    (if (= :w (nth @result-tally result))
      (lp/light-cell lpad result 7 lp/GREEN)
      (lp/light-cell lpad result 7 lp/RED))))

(defn show-scores [lpad round-counter result-tally]
  (doto lpad
    (show-results result-tally)
    (show-round-counter round-counter)))

(defn show-round [lpad sequence result-tally]
  (println sequence)
  (doto lpad
    (lp/clear-grid)
    (show-scores (count sequence) result-tally)
    (show-border))

  (doseq [step (range (count sequence))]
    (show lpad (nth sequence step))
    (Thread/sleep 300)))

(defn in-hotspot? [hotspot-id coord]
  (some #(= % coord) (:coords (get hotspots (keyword (str hotspot-id))))))

(defn user-quit? [msg] (:mixer-button? msg))

(defn- handle-button-press [lpad solution finished?]
  (fn [msg]
    (let [coord (conj nil (:y msg) (:x msg))]
      (when (:button-down? msg)
        (cond
          (in-hotspot? 3 coord) (record lpad 3 solution)
          (in-hotspot? 1 coord) (record lpad 1 solution)
          (in-hotspot? 2 coord) (record lpad 2 solution)
          (in-hotspot? 0 coord) (record lpad 0 solution)
          (user-quit? msg) (reset! finished? true))))))

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

(defn- show-incorrect-solution [lpad]
  (doto lpad
    (lp/light-cell 3 0 lp/RED)
    (lp/light-cell 2 1 lp/RED)
    (lp/light-cell 1 2 lp/RED)
    (lp/light-cell 0 3 lp/RED)
    (lp/light-cell 3 3 lp/RED)
    (lp/light-cell 2 2 lp/RED)
    (lp/light-cell 1 1 lp/RED)
    (lp/light-cell 0 0 lp/RED)))

(defn- handle-user-quit [] (throw (IllegalStateException. "user quit")))

(defn- handle-correct-solution [lpad result-tally] (swap! result-tally conj :w))

(defn- handle-incorrect-solution [lpad result-tally]
  (show-incorrect-solution lpad)
  (Thread/sleep 500)

  (swap! result-tally conj :l))

(defn game [lpad sequence]
  (let [result-tally (atom [])]
    (doseq [round (range 1 (inc (count sequence)))]
      (let [fragment (take round sequence)]
        (show-round lpad fragment result-tally)
        (case (solve-round lpad fragment)
          :user-quit (handle-user-quit)
          :solution-passed (handle-correct-solution lpad result-tally)
          :solution-failed (handle-incorrect-solution lpad result-tally)
          (show-scores lpad round result-tally))))))

#_{:clj-kondo/ignore [:unused-binding]}
(defn run [opts]
  (let [lpad (lp/open)
        sequence (build-sequence 8)]
    (midi/remove-button-press-handler lpad)
    (try
      (game lpad sequence)
      (catch IllegalStateException e
        (doto lpad
          (lp/scroll-text-once "Bye" 54)
          (lp/clear-grid)))
      (finally
        (midi/remove-button-press-handler lpad)))))