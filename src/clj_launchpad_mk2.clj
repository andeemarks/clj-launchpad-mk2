(ns clj-launchpad-mk2
  (:require [midi.core :as midi])
  (:import [javax.sound.midi MidiSystem Receiver ShortMessage]))

(def #^{:private true} intensities
  { :off 0 :low 1 :medium 2 :high 3 })

(defn draw-grid
  "set a cell grid on or off.
  x can be 0 to 8 inclusive (8 is for the top buttons)
  y can be 0 to 8 inclusive (8 is for the most right buttons)
  color description is a combination of the following possible values:
  :off
  :low :medium :high
  :red :green :orange
  :flashing

  Examples:
  (draw-grid lpad 1 2 :green :medium :flashing)
  (draw-grid lpad 1 2 :off)
  "
  [lpad x y & color-description]
  (let [velocity          (first color-description)
        midi-message      (if (= 8 y) 0xB0 0x90)
        midi-position     (if (= 8 y)
                            (+ x 0x68)
                            (+ (+ x 1) (* 10 (+ y 1))))   ]
    (if (or (> x 7) (< x 0))
      (throw (javax.sound.midi.InvalidMidiDataException. "x must be in range 0-7 inclusive")))

    (if (or (> y 7) (< y 0))
      (throw (javax.sound.midi.InvalidMidiDataException. "y must be in range 0-7 inclusive")))

    #_(println
       "x" x
       "y" y
       "velocity:" velocity)
    (midi/send-midi lpad midi-message midi-position velocity)))

(defn flash
  "flash the specified pad between the current color and specified color"
  [lpad x y & color-description]
  (let [velocity          (first color-description)
        midi-message      0x91
        midi-position     (+ (+ x 1) (* 10 (+ y 1)))   ]
        
    (if (or (> x 7) (< x 0))
      (throw (javax.sound.midi.InvalidMidiDataException. "x must be in range 0-7 inclusive")))

    (if (or (> y 7) (< y 0))
      (throw (javax.sound.midi.InvalidMidiDataException. "y must be in range 0-7 inclusive")))

    (midi/send-midi lpad midi-message midi-position velocity)))

(defn reset
  "reset the launchpad (all lights off)"
  [lpad]
  (midi/send-midi-sysex lpad 14 0))

(defn clear-grid [lpad]
  "clear the launchpad grid (not the top and left buttons)"
  (dorun (for [x (range 8)
               y (range 8)] (draw-grid lpad x y :off))))

(defn test-leds
  "lights all the leds in one command. Intensity can be :low, :medium or :high"
  ([lpad] (test-leds lpad :high))
  ([lpad intensity]
   (midi/send-midi lpad 0xb0 0 (+ (intensity intensities) 0x7c))))

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
       (test-leds lpad)
       (Thread/sleep 100)
       (reset lpad))
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
