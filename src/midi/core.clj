(ns midi.core
  (:import [javax.sound.midi MidiSystem Receiver ShortMessage SysexMessage]))

(defn send-midi [{:keys [out]} & args]
  (.send out
         (doto (ShortMessage.) (.setMessage (nth args 0) (nth args 1) (nth args 2)))
         -1))

(defn send-midi-sysex [{:keys [out]} & args]
  (let [contents (byte-array (concat [240 0 32 41 2 24] args [247]))
        _ (println contents)]
    (.send out
           (doto (SysexMessage.) (.setMessage contents (count contents)))
           -1)))
