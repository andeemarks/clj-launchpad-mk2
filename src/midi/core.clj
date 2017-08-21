(ns midi.core
  (:import [javax.sound.midi MidiSystem Receiver ShortMessage SysexMessage]))

(def ^:const SYSEX_HEADER [240 0 32 41 2 24])
(def ^:const SYSEX_FOOTER [247])

(defn send-midi [{:keys [out]} & args]
  (.send out
         (doto (ShortMessage.) (.setMessage (nth args 0) (nth args 1) (nth args 2)))
         -1))

(defn- send-midi-sysex-common [out contents]
  (.send out
         (doto (SysexMessage.) (.setMessage contents (count contents)))
         -1))

(defn send-midi-sysex [{:keys [out]} & args]
  (let [contents (byte-array (concat SYSEX_HEADER args SYSEX_FOOTER))]
  	(send-midi-sysex-common out contents)))

(defn send-midi-sysex-scroll [{:keys [out]} text & args]
  (let [contents (byte-array (concat SYSEX_HEADER args text SYSEX_FOOTER))]
  	(send-midi-sysex-common out contents)))

(defn decode-message 
  "Make a clojure map out of a midi object."
  [obj]
  {:channel (.getChannel obj)
   :command  (.getCommand obj)
   :note (.getData1 obj)
   :x (- (rem (.getData1 obj) 10) 1)
   :y (- (quot (.getData1 obj) 10) 1)
   :velocity  (.getData2 obj)
   :button-down? (= 127 (.getData2 obj))
   :button-up? (= 0 (.getData2 obj))
   :data1 (.getData1 obj)
   :data2 (.getData2 obj)
   })
