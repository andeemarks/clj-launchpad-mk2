(ns midi.core
  (:import [javax.sound.midi MidiSystem Receiver ShortMessage SysexMessage]))

(def ^:const ^:private SYSEX_HEADER [240 0 32 41 2 24])
(def ^:const ^:private SYSEX_FOOTER [247])

(def ^:const CC_CURSOR_UP "Identifies the cursor up control button in messages sent to/from the Launchpad." 0x68)
(def ^:const CC_CURSOR_DOWN "Identifies the cursor down control button in messages sent to/from the Launchpad."0x69)
(def ^:const CC_CURSOR_LEFT "Identifies the cursor left control button in messages sent to/from the Launchpad." 0x6A)
(def ^:const CC_CURSOR_RIGHT "Identifies the cursor right control button in messages sent to/from the Launchpad."0x6B)
(def ^:const CC_SESSION "Identifies the \"Session\" control button in messages sent to/from the Launchpad." 0x6C)
(def ^:const CC_USER1 "Identifies the \"User 1\" control button in messages sent to/from the Launchpad." 0x6D)
(def ^:const CC_USER2 "Identifies the \"User 2\" control button in messages sent to/from the Launchpad." 0x6E)
(def ^:const CC_MIXER "Identifies the \"Mixer\" control button in messages sent to/from the Launchpad." 0x6F)

(defn send-midi
	"Sends a javax.sound.midi.ShortMessage to the specified device.
	out should be the Launchpad receiving the message.
	args should be a three element sequence containing the status, data1 and data2 components of the message respectively.
	See https://docs.oracle.com/javase/7/docs/api/javax/sound/midi/ShortMessage.html#ShortMessage(int,%20int,%20int)

	Examples:
	(send-midi lpad 0x90 11 43)
	"
	[{:keys [out]} & args]
  (.send out
         (doto (ShortMessage.) (.setMessage (nth args 0) (nth args 1) (nth args 2)))
         -1))

(defn- send-midi-sysex-common [out contents]
  (.send out
         (doto (SysexMessage.) (.setMessage contents (count contents)))
         -1))

(defn send-midi-sysex
	"Sends a javax.sound.midi.SysexMessage to the specified device.
	out should be the Launchpad receiving the message.
	args should be an arbitary length sequence which will be wrapped by Launchpad Sysex header and footer information and converted to a byte array.
	See https://docs.oracle.com/javase/7/docs/api/javax/sound/midi/SysexMessage.html#setMessage(byte[],%20int)

	Examples:
	(send-midi-sysex lpad 13 4 43)
	"
	[{:keys [out]} & args]
  (let [contents (byte-array (concat SYSEX_HEADER args SYSEX_FOOTER))]
  	(send-midi-sysex-common out contents)))

(defn send-midi-sysex-scroll
 	"Sends a javax.sound.midi.SysexMessage to the specified device to produce scrolling text.
	out should be the Launchpad receiving the message.
	text should be the text to scroll.
	args should be an arbitary length sequence which will be wrapped by Launchpad Sysex header, text and footer information and converted to a byte array.
	See https://docs.oracle.com/javase/7/docs/api/javax/sound/midi/SysexMessage.html#setMessage(byte[],%20int)

	Examples:
	(send-midi-sysex lpad (72 101 108 108 111) 20 45 0)
	"
	[{:keys [out]} text & args]
  (let [contents (byte-array (concat SYSEX_HEADER args text SYSEX_FOOTER))]
  	(send-midi-sysex-common out contents)))

(defn decode-message 
  "Decompose a com.sun.media.sound.FastShortMessage into a Launchpad-specific map.

  See http://www.docjar.com/docs/api/com/sun/media/sound/FastShortMessage.html.
  "
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
