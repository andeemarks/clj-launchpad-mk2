(ns clj-launchpad-mk2.test.core
  (:use midje.sweet) 
  (:require [midi.core :as midi])
  (:require [clj-launchpad-mk2 :refer :all]))

(def lpad (atom (open)))

(with-state-changes [(after :contents (close @lpad))]
	(facts "about #flash"
		(flash lpad 5 5 60) => nil
		(provided (midi/send-midi lpad CHANNEL_2_NOTE_ON 66 60) => nil)
		(fact "x coordinate must be within the range 0-7 inclusive"
			(flash lpad -1 0 127) => (throws javax.sound.midi.InvalidMidiDataException)
			(flash lpad 8 0 127) => (throws javax.sound.midi.InvalidMidiDataException))
		(fact "y coordinate must be within the range 0-7 inclusive"
			(flash lpad 0 -1 127) => (throws javax.sound.midi.InvalidMidiDataException)
			(flash lpad 0 8 127) => (throws javax.sound.midi.InvalidMidiDataException))
		(fact "color must be within the range 0-127 inclusive"
			(flash lpad 0 0 -1) => (throws javax.sound.midi.InvalidMidiDataException)
			(flash lpad 0 0 128) => (throws javax.sound.midi.InvalidMidiDataException)))

	(facts "about #pulse"
		(pulse lpad 6 6 40) => nil
		(provided (midi/send-midi lpad CHANNEL_3_NOTE_ON 77 40) => nil)
		(fact "x coordinate must be within the range 0-7 inclusive"
			(pulse lpad -1 0 127) => (throws javax.sound.midi.InvalidMidiDataException)
			(pulse lpad 8 0 127) => (throws javax.sound.midi.InvalidMidiDataException))
		(fact "y coordinate must be within the range 0-7 inclusive"
			(pulse lpad 0 -1 127) => (throws javax.sound.midi.InvalidMidiDataException)
			(pulse lpad 0 8 127) => (throws javax.sound.midi.InvalidMidiDataException))
		(fact "color must be within the range 0-127 inclusive"
			(pulse lpad 0 0 -1) => (throws javax.sound.midi.InvalidMidiDataException)
			(pulse lpad 0 0 128) => (throws javax.sound.midi.InvalidMidiDataException)))

	(future-facts "about #reset"
		(reset lpad) => nil
		(provided (midi/send-midi-sysex lpad 14 0) => nil))

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