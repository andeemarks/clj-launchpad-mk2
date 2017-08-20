(ns clj-launchpad-mk2
  (:require [midi.core :as midi])
  (:import [javax.sound.midi MidiSystem Receiver]))

(defn- validate-coordinates [x y]
  (if (or (> x 7) (< x 0))
    (throw (javax.sound.midi.InvalidMidiDataException. "x must be in range 0-7 inclusive")))

  (if (or (> y 7) (< y 0))
    (throw (javax.sound.midi.InvalidMidiDataException. "y must be in range 0-7 inclusive"))))

(defn- validate-color [color]
  (if (or (> color 127) (< color 0))
    (throw (javax.sound.midi.InvalidMidiDataException. "color must be in range 0-127 inclusive"))))

(def ^:const CHANNEL_1_NOTE_ON 0x90)
(def ^:const CHANNEL_2_NOTE_ON 0x91)
(def ^:const CHANNEL_3_NOTE_ON 0x92)
(def ^:const CC_NOTE_ON 0xB0)

(def ^:const CC_CURSOR_UP 0x68)
(def ^:const CC_CURSOR_DOWN 0x69)
(def ^:const CC_CURSOR_LEFT 0x6A)
(def ^:const CC_CURSOR_RIGHT 0x6B)
(def ^:const CC_SESSION 0x6C)
(def ^:const CC_USER1 0x6D)
(def ^:const CC_USER2 0x6E)
(def ^:const CC_MIXER 0x6F)

(defn- coordinate-pair-to-index [x y] (+ (+ x 1) (* 10 (+ y 1))))

(defn light-cell
  "set a cell grid on or off.
  x can be 0 to 8 inclusive (8 is for the top buttons)
  y can be 0 to 8 inclusive (8 is for the most right buttons)
  color-description must be 0 to 127 inclusive

  Examples:
  (light-cell lpad 1 2 127)
  (light-cell lpad 1 2 0)
  "
  [lpad x y & color-description]
  (validate-coordinates x y)
  (midi/send-midi lpad CHANNEL_1_NOTE_ON (coordinate-pair-to-index x y) (first color-description)))

(defn scroll-text-once
  [lpad text color-description]
  (let [encoded-text (map #(int (char %)) text)]
    (validate-color color-description)
    (midi/send-midi-sysex-scroll lpad encoded-text 20 color-description 0 )))

(defn light-row
  [lpad y color-description]
  (validate-coordinates 0 y)
  (validate-color color-description)
  (midi/send-midi-sysex lpad 13 y color-description))

(defn light-column
  [lpad x color-description]
  (validate-coordinates x 0)
  (validate-color color-description)
  (midi/send-midi-sysex lpad 12 x color-description))

(defn light-grid
  [lpad color-description]
  (validate-color color-description)
  (midi/send-midi-sysex lpad 14 color-description))

(defn clear-cell
  [lpad x y]
  (validate-coordinates x y)
  (midi/send-midi lpad CHANNEL_1_NOTE_ON (coordinate-pair-to-index x y) 0))

(defn light-cc
  "set a top row control button on or off"
  [lpad cc-ref & color-description]
  (midi/send-midi lpad CC_NOTE_ON cc-ref (first color-description)))

(defn flash
  "flash the specified pad between the current color and specified color"
  [lpad x y & color-description]
  (validate-coordinates x y)
  (midi/send-midi lpad CHANNEL_2_NOTE_ON (coordinate-pair-to-index x y) (first color-description)))

(defn pulse
  "pulse the specified pad with the specified color"
  [lpad x y & color-description]
  (validate-coordinates x y)
  (midi/send-midi lpad CHANNEL_3_NOTE_ON (coordinate-pair-to-index x y) (first color-description)))

(defn clear-grid [lpad]
  "clear the launchpad grid (not the top and right buttons)"
  (midi/send-midi-sysex lpad 14 0))

(defn midi-device-names []
  "returns names of available Midi devices, usable with the open function"
  (map #(.getName %) (MidiSystem/getMidiDeviceInfo)))

(defn open
  ([]
   "find the launchpad name \"MK2 [hw:2,0,0]\" in the available midi devices and return a launchpad object suitable for the calls of this library"
   (open "MK2 [hw:2,0,0]"))
  ([name]
   "find the launchpad by name in the available midi devices and return a launchpad object suitable for the calls of this library"
   (let [[in-device out-device]
         (sort-by #(.getMaxTransmitters % )
                  (map #(MidiSystem/getMidiDevice %)
                       (filter #(= name (.getName %)) (MidiSystem/getMidiDeviceInfo))))
         out (.getReceiver out-device)
         in (.getTransmitter in-device)
         lpad {:in-device in-device
               :out-device out-device
               :in in
               :out out}]
     (do
       (.open out-device)
       (.open in-device)
       (Thread/sleep 100)
       (clear-grid lpad))
     lpad)))

(defn on-grid-pressed
  "Define a callback function when the grid is pressed.
  The function takes 3 parameters: x y pressed?"
  [{:keys [in]} callback]
  (.setReceiver in
                (reify Receiver
                  (send [this msg ts]
                    (let [cmd (.getCommand msg)
                          b (.getData1 msg)
                          top-button? (= 0xb0 cmd)
                          pressed? (= 127 (.getData2 msg))
                          x (if top-button?
                              (- b 0x68)
                              (-> b (mod 16) (mod 9)))
                          y (if top-button?
                              8
                              (quot b 16))]
                      (callback x y pressed?))))))

(defn create-press-register [{:keys [in]}]
  "Creates a registry to register and unregister callbacks for grid presses"
  (let [callbacks (atom [])]
    (.setReceiver in
                (reify Receiver
                  (send [this msg ts]
                    (let [cmd (.getCommand msg)
                          b (.getData1 msg)
                          top-button? (= 0xb0 cmd)
                          pressed? (= 127 (.getData2 msg))
                          x (if top-button?
                              (- b 0x68)
                              (-> b (mod 16) (mod 9)))
                          y (if top-button?
                              8
                              (quot b 16))]
                      (doseq [callback @callbacks]
                        (callback x y pressed?))))))
    {:register (fn [callback] (swap! callbacks (fn [callbacks] (into callbacks [callback]))))
     :unregister (fn [callback] (swap! callbacks (fn [callbacks] (filter #(not (= % callback)) callbacks))))}))

(defn close [lpad]
  "close the launchpad device"
  (dorun (map #(.close %) (vals lpad))))
