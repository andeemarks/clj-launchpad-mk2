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

	(facts "about #reset"
		(reset lpad) => nil
		(provided (midi/send-midi-sysex lpad 14 0) => nil))

	(facts "about #light-row"
		(light-row lpad 3 65) => nil
		(provided (midi/send-midi-sysex lpad 13 3 65) => nil))

	(future-facts "about #scroll-text-once"
		(scroll-text-once lpad "Hello world" 70) => nil
		(provided (midi/send-midi-sysex lpad 20 70 0 anything) => nil))

	(facts "about #light-cell"
		(fact "valid x/y coordinates and color are mapped onto the MIDI message when being dispatched to midi/send-midi"
			(light-cell lpad 2 3 127) => nil
			(provided (midi/send-midi lpad CHANNEL_1_NOTE_ON 43 127) => nil)
			(light-cell lpad 0 0 13) => nil
			(provided (midi/send-midi lpad CHANNEL_1_NOTE_ON 11 13) => nil) )
		(fact "x coordinate must be within the range 0-7 inclusive"
			(light-cell lpad -1 0 127) => (throws javax.sound.midi.InvalidMidiDataException)
			(light-cell lpad 8 0 127) => (throws javax.sound.midi.InvalidMidiDataException))
		(fact "y coordinate must be within the range 0-7 inclusive"
			(light-cell lpad 0 -1 127) => (throws javax.sound.midi.InvalidMidiDataException)
			(light-cell lpad 0 8 127) => (throws javax.sound.midi.InvalidMidiDataException))
		(fact "color must be within the range 0-127 inclusive"
			(light-cell lpad 0 0 -1) => (throws javax.sound.midi.InvalidMidiDataException)
			(light-cell lpad 0 0 128) => (throws javax.sound.midi.InvalidMidiDataException)))

	(facts "about #dark-cell"
		(fact "valid x/y coordinates and color are mapped onto the MIDI message when being dispatched to midi/send-midi"
			(dark-cell lpad 2 3) => nil
			(provided (midi/send-midi lpad CHANNEL_1_NOTE_ON 43 0) => nil)
			(dark-cell lpad 0 0) => nil
			(provided (midi/send-midi lpad CHANNEL_1_NOTE_ON 11 0) => nil) )
		(fact "x coordinate must be within the range 0-7 inclusive"
			(dark-cell lpad -1 0) => (throws javax.sound.midi.InvalidMidiDataException)
			(dark-cell lpad 8 0) => (throws javax.sound.midi.InvalidMidiDataException))
		(fact "y coordinate must be within the range 0-7 inclusive"
			(dark-cell lpad 0 -1) => (throws javax.sound.midi.InvalidMidiDataException)
			(dark-cell lpad 0 8) => (throws javax.sound.midi.InvalidMidiDataException)))

	(facts "about #clear-grid"
		(fact "midi/send-midi is called once for each grid cell"
			(clear-grid lpad) => nil
			(provided (midi/send-midi lpad CHANNEL_1_NOTE_ON (as-checker #(<= 11 % 88)) 0) => nil :times 64)))

	(facts "about #light-cc"
		(fact "valid x/y coordinates and color are mapped onto the MIDI message when being dispatched to midi/send-midi"
			(light-cc lpad CC_CURSOR_UP 127) => nil
			(provided (midi/send-midi lpad CC_NOTE_ON 0x68 127) => nil)
			(light-cc lpad CC_SESSION 13) => nil
			(provided (midi/send-midi lpad CC_NOTE_ON 0x6C 13) => nil) )
		(fact "color must be within the range 0-127 inclusive"
			(light-cc lpad CC_USER1 -1) => (throws javax.sound.midi.InvalidMidiDataException)
			(light-cc lpad CC_USER2 128) => (throws javax.sound.midi.InvalidMidiDataException)))
)