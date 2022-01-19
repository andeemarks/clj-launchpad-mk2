(ns clj-launchpad-mk2.core-test
  (:use midje.sweet)
  (:require [clj-launchpad-mk2.midi.core :as midi]
            [clj-launchpad-mk2.core :as lp]))

(def lpad "stub-launchpad")

(against-background [(lp/open) => lpad]
                    (facts "about #flash"
                           (lp/flash lpad 5 5 60) => nil
                           (provided (midi/send-midi lpad 0x91 66 60) => nil)
                           (fact "x coordinate must be within the range 0-7 inclusive"
                                 (lp/flash lpad -1 0 127) => (throws IllegalArgumentException)
                                 (lp/flash lpad 8 0 127) => (throws IllegalArgumentException))
                           (fact "y coordinate must be within the range 0-7 inclusive"
                                 (lp/flash lpad 0 -1 127) => (throws IllegalArgumentException)
                                 (lp/flash lpad 0 8 127) => (throws IllegalArgumentException))
                           (fact "color must be within the range 0-127 inclusive"
                                 (lp/flash lpad 0 0 -1) => (throws IllegalArgumentException)
                                 (lp/flash lpad 0 0 128) => (throws IllegalArgumentException)))

                    (facts "about #pulse"
                           (lp/pulse lpad 6 6 40) => nil
                           (provided (midi/send-midi lpad 0x92 77 40) => nil)
                           (fact "x coordinate must be within the range 0-7 inclusive"
                                 (lp/pulse lpad -1 0 127) => (throws IllegalArgumentException)
                                 (lp/pulse lpad 8 0 127) => (throws IllegalArgumentException))
                           (fact "y coordinate must be within the range 0-7 inclusive"
                                 (lp/pulse lpad 0 -1 127) => (throws IllegalArgumentException)
                                 (lp/pulse lpad 0 8 127) => (throws IllegalArgumentException))
                           (fact "color must be within the range 0-127 inclusive"
                                 (lp/pulse lpad 0 0 -1) => (throws IllegalArgumentException)
                                 (lp/pulse lpad 0 0 128) => (throws IllegalArgumentException)))

                    (facts "about #light-row"
                           (fact "a sysex message with the appropriate status byte is sent"
                                 (lp/light-row lpad 3 65) => nil
                                 (provided (midi/send-midi-sysex lpad 13 3 65) => nil))
                           (fact "y coordinate must be within the range 0-7 inclusive"
                                 (lp/light-row lpad -1 65) => (throws IllegalArgumentException)
                                 (lp/light-row lpad 8 65) => (throws IllegalArgumentException))
                           (fact "color must be within the range 0-127 inclusive"
                                 (lp/light-row lpad 5 -1) => (throws IllegalArgumentException)
                                 (lp/light-row lpad 5 128) => (throws IllegalArgumentException)))

                    (facts "about #rgb"
                           (fact "a sysex message with the appropriate status byte is sent"
                                 (lp/rgb lpad 4 7 0 0 0) => nil
                                 (provided (midi/send-midi-sysex lpad 11 85 0 0 0) => nil))
                           (fact "y coordinate must be within the range 0-7 inclusive"
                                 (lp/rgb lpad 4 -1 0 0 0) => (throws IllegalArgumentException)
                                 (lp/rgb lpad 4 8 0 0 0) => (throws IllegalArgumentException))
                           (fact "x coordinate must be within the range 0-7 inclusive"
                                 (lp/rgb lpad -1 7 0 0 0) => (throws IllegalArgumentException)
                                 (lp/rgb lpad 8 7 0 0 0) => (throws IllegalArgumentException))
                           (fact "each RGB component must be within the range 0-63 inclusive"
                                 (lp/rgb lpad 4 7 -1 0 0) => (throws IllegalArgumentException)
                                 (lp/rgb lpad 4 7 64 0 0) => (throws IllegalArgumentException)
                                 (lp/rgb lpad 4 7 0 -1 0) => (throws IllegalArgumentException)
                                 (lp/rgb lpad 4 7 0 64 0) => (throws IllegalArgumentException)
                                 (lp/rgb lpad 4 7 0 0 -1) => (throws IllegalArgumentException)
                                 (lp/rgb lpad 4 7 0 0 64) => (throws IllegalArgumentException)))

                    (facts "about #light-column"
                           (fact "a sysex message with the appropriate status byte is sent"
                                 (lp/light-column lpad 3 65) => nil
                                 (provided (midi/send-midi-sysex lpad 12 3 65) => nil))
                           (fact "x coordinate must be within the range 0-7 inclusive"
                                 (lp/light-row lpad -1 65) => (throws IllegalArgumentException)
                                 (lp/light-row lpad 8 65) => (throws IllegalArgumentException))
                           (fact "color must be within the range 0-127 inclusive"
                                 (lp/light-row lpad 5 -1) => (throws IllegalArgumentException)
                                 (lp/light-row lpad 5 128) => (throws IllegalArgumentException)))

                    (facts "about #light-grid"
                           (fact "a sysex message with the appropriate status byte is sent"
                                 (lp/light-grid lpad 65) => nil
                                 (provided (midi/send-midi-sysex lpad 14 65) => nil))
                           (fact "color must be within the range 0-127 inclusive"
                                 (lp/light-grid lpad -1) => (throws IllegalArgumentException)
                                 (lp/light-grid lpad 128) => (throws IllegalArgumentException)))

                    (facts "about #scroll-text-once"
                           (fact "a sysex message with the appropriate status byte and ASCII codes for characters is sent"
                                 (lp/scroll-text-once lpad "Hello" 70) => nil
                                 (provided (midi/send-midi-sysex-scroll lpad '(72 101 108 108 111) 20 70 0) => nil))
                           (fact "color must be within the range 0-127 inclusive"
                                 (lp/scroll-text-once lpad "Hello world" -1) => (throws IllegalArgumentException)
                                 (lp/scroll-text-once lpad "Hello world" 128) => (throws IllegalArgumentException)))

                    (facts "about #scroll-text"
                           (fact "a sysex message with the appropriate status byte and ASCII codes for characters is sent"
                                 (lp/scroll-text lpad "Hello" 70) => nil
                                 (provided (midi/send-midi-sysex-scroll lpad '(72 101 108 108 111) 20 70 1) => nil))
                           (fact "color must be within the range 0-127 inclusive"
                                 (lp/scroll-text lpad "Hello world" -1) => (throws IllegalArgumentException)
                                 (lp/scroll-text lpad "Hello world" 128) => (throws IllegalArgumentException)))

                    (facts "about #scroll-stop"
                           (fact "a sysex message with the appropriate status byte an empty payload"
                                 (lp/scroll-stop lpad) => nil
                                 (provided (midi/send-midi-sysex lpad 20) => nil)))

                    (facts "about #light-cell"
                           (fact "valid x/y coordinates and color are mapped onto the MIDI message when being dispatched to midi/send-midi"
                                 (lp/light-cell lpad 2 3 127) => nil
                                 (provided (midi/send-midi lpad 0x90 43 127) => nil)
                                 (lp/light-cell lpad 0 0 13) => nil
                                 (provided (midi/send-midi lpad 0x90 11 13) => nil))
                           (fact "x coordinate must be within the range 0-7 inclusive"
                                 (lp/light-cell lpad -1 0 127) => (throws IllegalArgumentException)
                                 (lp/light-cell lpad 8 0 127) => (throws IllegalArgumentException))
                           (fact "y coordinate must be within the range 0-7 inclusive"
                                 (lp/light-cell lpad 0 -1 127) => (throws IllegalArgumentException)
                                 (lp/light-cell lpad 0 8 127) => (throws IllegalArgumentException))
                           (fact "color must be within the range 0-127 inclusive"
                                 (lp/light-cell lpad 0 0 -1) => (throws IllegalArgumentException)
                                 (lp/light-cell lpad 0 0 128) => (throws IllegalArgumentException)))

                    (facts "about #clear-cell"
                           (fact "valid x/y coordinates and color are mapped onto the MIDI message when being dispatched to midi/send-midi"
                                 (lp/clear-cell lpad 2 3) => nil
                                 (provided (midi/send-midi lpad 0x90 43 0) => nil)
                                 (lp/clear-cell lpad 0 0) => nil
                                 (provided (midi/send-midi lpad 0x90 11 0) => nil))
                           (fact "x coordinate must be within the range 0-7 inclusive"
                                 (lp/clear-cell lpad -1 0) => (throws IllegalArgumentException)
                                 (lp/clear-cell lpad 8 0) => (throws IllegalArgumentException))
                           (fact "y coordinate must be within the range 0-7 inclusive"
                                 (lp/clear-cell lpad 0 -1) => (throws IllegalArgumentException)
                                 (lp/clear-cell lpad 0 8) => (throws IllegalArgumentException)))

                    (facts "about #clear-grid"
                           (fact "midi/send-midi is called once for each grid cell"
                                 (lp/clear-grid lpad) => nil
                                 (provided (midi/send-midi-sysex lpad 14 0) => nil)))

                    (facts "about #light-cc"
                           (fact "valid x/y coordinates and color are mapped onto the MIDI message when being dispatched to midi/send-midi"
                                 (lp/light-cc lpad midi/CC_CURSOR_UP 127) => nil
                                 (provided (midi/send-midi lpad 0xB0 0x68 127) => nil)
                                 (lp/light-cc lpad midi/CC_SESSION 13) => nil
                                 (provided (midi/send-midi lpad 0xB0 0x6C 13) => nil))
                           (fact "color must be within the range 0-127 inclusive"
                                 (lp/light-cc lpad midi/CC_USER1 -1) => (throws IllegalArgumentException)
                                 (lp/light-cc lpad midi/CC_USER2 128) => (throws IllegalArgumentException))))