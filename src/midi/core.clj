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
  (let [data1 (.getData1 obj)
  			data2 (.getData2 obj)
  			x (- (rem data1 10) 1)
  			y (- (quot data1 10) 1)]
	  {:channel (.getChannel obj)
	   :command  (.getCommand obj)
	   :note data1
	   :x x
	   :y y
	   :velocity  data2
	   :button-down? (= 127 data2)
	   :button-up? (= 0 data2)
	   :control-button? (<= 104 data1 111)
	   :scene-button? (and (= x 8) (<= 0 y 7))
	   :data1 data1
	   :data2 data2
	   }))
