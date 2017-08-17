(ns midi.core
  (:import [javax.sound.midi MidiSystem Receiver ShortMessage]))

(defn send-midi [{:keys [out]} & args]
  (.send out
         (doto (ShortMessage.) (.setMessage (nth args 0) (nth args 1) (nth args 2)))
         -1))
