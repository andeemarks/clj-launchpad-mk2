(ns clj-launchpad-mk2.core-test
  (:use midje.sweet) 
  (:require [midi.core :as midi])
  (:require [clj-launchpad-mk2 :refer :all]))

(def lpad "stub-launchpad")

(against-background [(open) => lpad]
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

	(facts "about #light-row"
		(fact "a sysex message with the appropriate status byte is sent"
			(light-row lpad 3 65) => nil
			(provided (midi/send-midi-sysex lpad 13 3 65) => nil))
		(fact "y coordinate must be within the range 0-7 inclusive"
			(light-row lpad -1 65) => (throws javax.sound.midi.InvalidMidiDataException)
			(light-row lpad 8 65) => (throws javax.sound.midi.InvalidMidiDataException))
		(fact "color must be within the range 0-127 inclusive"
			(light-row lpad 5 -1) => (throws javax.sound.midi.InvalidMidiDataException)
			(light-row lpad 5 128) => (throws javax.sound.midi.InvalidMidiDataException) ))

	(facts "about #light-column"
		(fact "a sysex message with the appropriate status byte is sent"
			(light-column lpad 3 65) => nil
			(provided (midi/send-midi-sysex lpad 12 3 65) => nil))
		(fact "x coordinate must be within the range 0-7 inclusive"
			(light-row lpad -1 65) => (throws javax.sound.midi.InvalidMidiDataException)
			(light-row lpad 8 65) => (throws javax.sound.midi.InvalidMidiDataException))
		(fact "color must be within the range 0-127 inclusive"
			(light-row lpad 5 -1) => (throws javax.sound.midi.InvalidMidiDataException)
			(light-row lpad 5 128) => (throws javax.sound.midi.InvalidMidiDataException) ))

	(facts "about #light-grid"
		(fact "a sysex message with the appropriate status byte is sent"
			(light-grid lpad 65) => nil
			(provided (midi/send-midi-sysex lpad 14 65) => nil))
		(fact "color must be within the range 0-127 inclusive"
			(light-grid lpad -1) => (throws javax.sound.midi.InvalidMidiDataException)
			(light-grid lpad 128) => (throws javax.sound.midi.InvalidMidiDataException) ))

	(facts "about #scroll-text-once"
		(fact "a sysex message with the appropriate status byte and ASCII codes for characters is sent"
			(scroll-text-once lpad "Hello" 70) => nil
			(provided (midi/send-midi-sysex-scroll lpad '(72 101 108 108 111) 20 70 0 ) => nil))
		(fact "color must be within the range 0-127 inclusive"
			(scroll-text-once lpad "Hello world" -1) => (throws javax.sound.midi.InvalidMidiDataException)
			(scroll-text-once lpad "Hello world" 128) => (throws javax.sound.midi.InvalidMidiDataException) ))

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

	(facts "about #clear-cell"
		(fact "valid x/y coordinates and color are mapped onto the MIDI message when being dispatched to midi/send-midi"
			(clear-cell lpad 2 3) => nil
			(provided (midi/send-midi lpad CHANNEL_1_NOTE_ON 43 0) => nil)
			(clear-cell lpad 0 0) => nil
			(provided (midi/send-midi lpad CHANNEL_1_NOTE_ON 11 0) => nil) )
		(fact "x coordinate must be within the range 0-7 inclusive"
			(clear-cell lpad -1 0) => (throws javax.sound.midi.InvalidMidiDataException)
			(clear-cell lpad 8 0) => (throws javax.sound.midi.InvalidMidiDataException))
		(fact "y coordinate must be within the range 0-7 inclusive"
			(clear-cell lpad 0 -1) => (throws javax.sound.midi.InvalidMidiDataException)
			(clear-cell lpad 0 8) => (throws javax.sound.midi.InvalidMidiDataException)))

	(facts "about #clear-grid"
		(fact "midi/send-midi is called once for each grid cell"
			(clear-grid lpad) => nil
			(provided (midi/send-midi-sysex lpad 14 0) => nil)))

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