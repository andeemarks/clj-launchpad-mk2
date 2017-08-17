(ns clj-launchpad-mk2.test.core
  (:use midje.sweet) 
  (:require [midi.core :as midi])
  (:require [clj-launchpad-mk2 :refer :all]))

(def lpad (atom (open)))

(def ^:const CHANNEL_1_NOTE_ON 144)

(with-state-changes [(after :contents (close @lpad))]
	(facts "about #draw-grid"
		(fact "valid x/y coordinates and color are mapped onto the MIDI message when being dispatched to midi/send-midi"
			(draw-grid lpad 2 3 127) => nil
			(provided (midi/send-midi lpad CHANNEL_1_NOTE_ON 43 127) => nil)
			(draw-grid lpad 0 0 13) => nil
			(provided (midi/send-midi lpad CHANNEL_1_NOTE_ON 11 13) => nil) )
		(fact "x coordinate must be within the range 0-7 inclusive"
			(draw-grid lpad -1 0 127) => (throws javax.sound.midi.InvalidMidiDataException)
			(draw-grid lpad 8 0 127) => (throws javax.sound.midi.InvalidMidiDataException))
		(fact "y coordinate must be within the range 0-7 inclusive"
			(draw-grid lpad 0 -1 127) => (throws javax.sound.midi.InvalidMidiDataException)
			(draw-grid lpad 0 8 127) => (throws javax.sound.midi.InvalidMidiDataException))
		(fact "color must be within the range 0-127 inclusive"
			(draw-grid lpad 0 0 -1) => (throws javax.sound.midi.InvalidMidiDataException)
			(draw-grid lpad 0 0 128) => (throws javax.sound.midi.InvalidMidiDataException)))
)