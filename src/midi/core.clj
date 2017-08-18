(ns midi.core
  (:import [javax.sound.midi MidiSystem Receiver ShortMessage SysexMessage]))

(def ^:const SYSEX_HEADER [240 0 32 41 2 24])
(def ^:const SYSEX_FOOTER [247])

(defn send-midi [{:keys [out]} & args]
  (.send out
         (doto (ShortMessage.) (.setMessage (nth args 0) (nth args 1) (nth args 2)))
         -1))

(defn send-midi-sysex [{:keys [out]} & args]
  (let [contents (byte-array (concat SYSEX_HEADER args SYSEX_FOOTER))]
    (.send out
           (doto (SysexMessage.) (.setMessage contents (count contents)))
           -1)))
