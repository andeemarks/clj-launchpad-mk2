(ns clj-launchpad-mk2.midi.core-test
  (:use midje.sweet) 
  (:require [midi.core :refer :all]))

(def sample-message (javax.sound.midi.ShortMessage. 144 0 23 127))

(facts "about #decode-message"
	(fact "channel is identified"
		(:channel (decode-message sample-message)) => 0)
	(fact "command is identified"
		(:command (decode-message sample-message)) => 144)
	(fact "note/data1 is identified"
		(:note (decode-message sample-message)) => 23
		(:data1 (decode-message sample-message)) => 23 )
	(fact "x & y are derived from note/data1"
		(:x (decode-message sample-message)) => 2
		(:y (decode-message sample-message)) => 1 )
	(fact "control-button? is derived from note/data1"
		(:control-button? (decode-message (javax.sound.midi.ShortMessage. 176 0 103 0))) => falsey
		(:control-button? (decode-message (javax.sound.midi.ShortMessage. 176 0 104 0))) => truthy
		(:control-button? (decode-message (javax.sound.midi.ShortMessage. 176 0 105 0))) => truthy
		(:control-button? (decode-message (javax.sound.midi.ShortMessage. 176 0 106 0))) => truthy
		(:control-button? (decode-message (javax.sound.midi.ShortMessage. 176 0 107 0))) => truthy
		(:control-button? (decode-message (javax.sound.midi.ShortMessage. 176 0 108 0))) => truthy
		(:control-button? (decode-message (javax.sound.midi.ShortMessage. 176 0 109 0))) => truthy
		(:control-button? (decode-message (javax.sound.midi.ShortMessage. 176 0 110 0))) => truthy
		(:control-button? (decode-message (javax.sound.midi.ShortMessage. 176 0 111 0))) => truthy
		(:control-button? (decode-message (javax.sound.midi.ShortMessage. 176 0 112 0))) => falsey)		
	(fact "scene-button? is derived from note/data1"
		(:scene-button? (decode-message (javax.sound.midi.ShortMessage. 144 0 9 0))) => falsey
		(:scene-button? (decode-message (javax.sound.midi.ShortMessage. 144 0 99 0))) => falsey
		(:scene-button? (decode-message (javax.sound.midi.ShortMessage. 144 0 19 0))) => truthy
		(:scene-button? (decode-message (javax.sound.midi.ShortMessage. 144 0 29 0))) => truthy
		(:scene-button? (decode-message (javax.sound.midi.ShortMessage. 144 0 39 0))) => truthy
		(:scene-button? (decode-message (javax.sound.midi.ShortMessage. 144 0 49 0))) => truthy
		(:scene-button? (decode-message (javax.sound.midi.ShortMessage. 144 0 59 0))) => truthy
		(:scene-button? (decode-message (javax.sound.midi.ShortMessage. 144 0 69 0))) => truthy
		(:scene-button? (decode-message (javax.sound.midi.ShortMessage. 144 0 79 0))) => truthy
		(:scene-button? (decode-message (javax.sound.midi.ShortMessage. 144 0 89 0))) => truthy)		
	(fact "button-down? and button-up? are derived from velocity/data2"
		(:button-down? (decode-message sample-message)) => truthy
		(:button-up? (decode-message sample-message)) => falsey
		(:button-down? (decode-message (javax.sound.midi.ShortMessage. 144 0 23 0))) => falsey
		(:button-up? (decode-message (javax.sound.midi.ShortMessage. 144 0 23 0))) => truthy )
	(fact "velocity/data2 is identified"
		(:velocity (decode-message sample-message)) => 127
		(:data2 (decode-message sample-message)) => 127 )
	)


