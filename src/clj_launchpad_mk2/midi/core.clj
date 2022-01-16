(ns clj-launchpad-mk2.midi.core
  "Holds the functions which 'talk MIDI' to the Launchpad via messages in the ```javax.sound.midi``` package."
  (:import [javax.sound.midi MidiSystem Receiver ShortMessage SysexMessage])
  (:require [clojure.string :as string]))

(def ^:const ^:no-doc CHANNEL_1_NOTE_ON 0x90)
(def ^:const ^:no-doc CHANNEL_2_NOTE_ON 0x91)
(def ^:const ^:no-doc CHANNEL_3_NOTE_ON 0x92)
(def ^:const ^:no-doc CC_NOTE_ON 0xB0)

(def ^:const SYSEX_RGB_STATUS "The status byte for a RGB Sysex message" 11)
(def ^:const SYSEX_SCROLL_STATUS "The status byte for a scroll text Sysex message" 20)
(def ^:const SYSEX_LIGHT_ROW_STATUS "The status byte for a light row Sysex message" 13)
(def ^:const SYSEX_LIGHT_COLUMN_STATUS "The status byte for a light column Sysex message" 12)
(def ^:const SYSEX_LIGHT_GRID_STATUS "The status byte for a light grid Sysex message" 14)

(def ^:const SYSEX_HEADER "The common set of header bytes sent with each Sysex message" [240 0 32 41 2 24])
(def ^:const SYSEX_FOOTER "The common set of footer bytes sent with each Sysex message" [247])

(def ^:const CC_CURSOR_UP "Identifies the cursor up control button in messages sent to/from the Launchpad." 0x68)
(def ^:const CC_CURSOR_DOWN "Identifies the cursor down control button in messages sent to/from the Launchpad." 0x69)
(def ^:const CC_CURSOR_LEFT "Identifies the cursor left control button in messages sent to/from the Launchpad." 0x6A)
(def ^:const CC_CURSOR_RIGHT "Identifies the cursor right control button in messages sent to/from the Launchpad." 0x6B)
(def ^:const CC_SESSION "Identifies the \"Session\" control button in messages sent to/from the Launchpad." 0x6C)
(def ^:const CC_USER1 "Identifies the \"User 1\" control button in messages sent to/from the Launchpad." 0x6D)
(def ^:const CC_USER2 "Identifies the \"User 2\" control button in messages sent to/from the Launchpad." 0x6E)
(def ^:const CC_MIXER "Identifies the \"Mixer\" control button in messages sent to/from the Launchpad." 0x6F)

(defn open
  "find the launchpad by name in the available midi devices and return a launchpad object suitable for the calls of this library

  * `name` should be the string returned from [MidiSystem/getMidiDeviceInfo](https://docs.oracle.com/javase/7/docs/api/javax/sound/midi/MidiSystem.html)

  Examples:
  ```
  (open)
  ```
  "
  []
  (let [[in-device out-device]
        (sort-by #(.getMaxTransmitters %)
                 (map #(MidiSystem/getMidiDevice %)
                      (filter #(string/includes? (.getDescription %) "Launchpad") (MidiSystem/getMidiDeviceInfo))))
        out (.getReceiver out-device)
        in (.getTransmitter in-device)
        lpad {:in-device in-device
              :out-device out-device
              :in in
              :out out}]
    (do
      (.open out-device)
      (.open in-device)
      (Thread/sleep 100))
    lpad))

(defn close
  "close the launchpad device.

  Examples:
  ```
  (close lpad)
  ```
  "
  [lpad]
  (dorun (map #(.close %) (vals lpad))))

(defn send-midi
  "Sends a [javax.sound.midi.ShortMessage](https://docs.oracle.com/javase/7/docs/api/javax/sound/midi/ShortMessage.html) to the specified device.

	* the first argument should be a map containing an `out` key which returns a [javax.sound.midi.Receiver](https://docs.oracle.com/javase/7/docs/api/javax/sound/midi/Receiver.html).
	* `args` should be a three element sequence containing the `status`, `data1` and `data2` components of the message respectively.

	Examples:
	```
	(send-midi lpad 0x90 11 43)
	```
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
  "Sends a [javax.sound.midi.SysexMessage](https://docs.oracle.com/javase/7/docs/api/javax/sound/midi/SysexMessage.html) to the specified device.

	* the first argument should be a map containing an `out` key which returns a [javax.sound.midi.Receiver](https://docs.oracle.com/javase/7/docs/api/javax/sound/midi/Receiver.html).
	* `args` should be an arbitary length sequence which will be wrapped by [[SYSEX_HEADER]] and [[SYSEX_FOOTER]] information and converted to a byte array.

	Examples:
	```
	(send-midi-sysex lpad 13 4 43)
	```
	"
  [{:keys [out]} & args]
  (let [contents (byte-array (concat SYSEX_HEADER args SYSEX_FOOTER))]
    (send-midi-sysex-common out contents)))

(defn send-midi-sysex-scroll
  "Sends a [javax.sound.midi.SysexMessage](https://docs.oracle.com/javase/7/docs/api/javax/sound/midi/SysexMessage.html) to the specified device to produce scrolling text.
	
	* the first argument should be a map containing an `out` key which returns a [javax.sound.midi.Receiver](https://docs.oracle.com/javase/7/docs/api/javax/sound/midi/Receiver.html).
	* `text` should be a sequence of integers representing the letters of the text to scroll (e.g., `(map #(int (char %)) \"Hello\")`.
	* `args` should be an arbitary length sequence which will be wrapped by [[SYSEX_HEADER]], text and [[SYSEX_FOOTER]] information and converted to a byte array.

	Examples:

	```
	(send-midi-sysex-scroll lpad (72 101 108 108 111) 20 45 0)
	```
	"
  [{:keys [out]} text & args]
  (let [contents (byte-array (concat SYSEX_HEADER args text SYSEX_FOOTER))]
    (send-midi-sysex-common out contents)))

(defn decode-message
  "Decompose a [com.sun.media.sound.FastShortMessage](http://www.docjar.com/docs/api/com/sun/media/sound/FastShortMessage.html) into a Launchpad-specific map.

  Examples:

  ```
  (def lp-msg (decode-message raw-msg))
  (println (:x lp-msg) (:y lp-msg))
  (println (:button-up? lp-msg))
  (println (:velocity lp-msg))
  (= (:note lp-msg) (:getData1 raw-msg))
  ```
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
     :data2 data2}))

(defn set-button-press-handler
  "Specify a single handler that will receive all midi events from the input device.
  
  * the first argument should be a map containing an `in` key which returns a [javax.sound.midi.Transmitter](https://docs.oracle.com/javase/7/docs/api/javax/sound/midi/Transmitter.html).
  * handler should be a function that accepts a single parameter - a decoded version of the event.

  Examples:
  ```
  (set-button-press-handler
    lpad    
    (fn [msg]
      (light-cell 
        lpad 
        (:x msg) 
        (:y msg) 
        (- 127 (+ (:x msg) (:y msg))))))
  ```
  "
  [{:keys [in]} handler-fn]
  (let [receiver  (proxy [Receiver] []
                    (close [] nil)
                    (send [msg timestamp]
                      (if (= (type msg) com.sun.media.sound.FastShortMessage)
                        (handler-fn (decode-message msg)))))]
    (.setReceiver in receiver)))

(defn remove-button-press-handler
  "Remove any event handlers.

  * the first argument should be a map containing an `in` key which returns a [javax.sound.midi.Transmitter](https://docs.oracle.com/javase/7/docs/api/javax/sound/midi/Transmitter.html).

  Examples:
  ```
  (remove-button-press-handler lpad)
  ```
  "
  [{:keys [in]}]
  (let [receiver  (proxy [Receiver] []
                    (close [] nil)
                    (send [msg timestamp]))]
    (.setReceiver in receiver)))
