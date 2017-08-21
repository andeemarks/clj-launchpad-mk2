(ns midi.core
  (:import [javax.sound.midi MidiSystem Receiver ShortMessage SysexMessage]))

(def ^:const SYSEX_HEADER [240 0 32 41 2 24])
(def ^:const SYSEX_FOOTER [247])

(def ^:const CC_CURSOR_UP 0x68)
(def ^:const CC_CURSOR_DOWN 0x69)
(def ^:const CC_CURSOR_LEFT 0x6A)
(def ^:const CC_CURSOR_RIGHT 0x6B)
(def ^:const CC_SESSION 0x6C)
(def ^:const CC_USER1 0x6D)
(def ^:const CC_USER2 0x6E)
(def ^:const CC_MIXER 0x6F)

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
	   :cursor-up-button? (= data1 CC_CURSOR_UP)
	   :cursor-down-button? (= data1 CC_CURSOR_DOWN)
	   :cursor-left-button? (= data1 CC_CURSOR_LEFT)
	   :cursor-right-button? (= data1 CC_CURSOR_RIGHT)
	   :session-button? (= data1 CC_SESSION)
	   :user-1-button? (= data1 CC_USER1)
	   :user-2-button? (= data1 CC_USER2)
	   :mixer-button? (= data1 CC_MIXER)
	   :data1 data1
	   :data2 data2
	   }))
