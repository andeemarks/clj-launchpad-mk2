(ns clj-launchpad-mk2.midi.core-test
  (:use midje.sweet) 
  (:require [midi.core :refer :all]))

(defn- message-from [& args]
	(decode-message (javax.sound.midi.ShortMessage. (first args) (second args) (nth args 2) (last args))))

(def sample-message (message-from 144 0 23 127))

(facts "about #decode-message"
	(fact "channel is identified"
		(:channel sample-message) => 0)
	(fact "command is identified"
		(:command sample-message) => 144)
	(fact "note/data1 is identified"
		(:note sample-message) => 23
		(:data1 sample-message) => 23 )
	(fact "x & y are derived from note/data1"
		(:x sample-message) => 2
		(:y sample-message) => 1 )
	(fact "control-button? is derived from note/data1"
		(:control-button? (message-from  176 0 103 0)) => falsey
		(:control-button? (message-from  176 0 104 0)) => truthy
		(:cursor-up-button? (message-from  176 0 104 0)) => truthy
		(:control-button? (message-from  176 0 105 0)) => truthy
		(:cursor-down-button? (message-from  176 0 105 0)) => truthy
		(:control-button? (message-from  176 0 106 0)) => truthy
		(:cursor-left-button? (message-from  176 0 106 0)) => truthy
		(:control-button? (message-from  176 0 107 0)) => truthy
		(:cursor-right-button? (message-from  176 0 107 0)) => truthy
		(:control-button? (message-from  176 0 108 0)) => truthy
		(:session-button? (message-from  176 0 108 0)) => truthy
		(:control-button? (message-from  176 0 109 0)) => truthy
		(:user-1-button? (message-from  176 0 109 0)) => truthy
		(:control-button? (message-from  176 0 110 0)) => truthy
		(:user-2-button? (message-from  176 0 110 0)) => truthy
		(:control-button? (message-from  176 0 111 0)) => truthy
		(:mixer-button? (message-from  176 0 111 0)) => truthy
		(:control-button? (message-from  176 0 112 0)) => falsey)		
	(fact "scene-button? is derived from note/data1"
		(:scene-button? (message-from  144 0 9 0)) => falsey
		(:scene-button? (message-from  144 0 99 0)) => falsey
		(:scene-button? (message-from  144 0 19 0)) => truthy
		(:scene-button? (message-from  144 0 29 0)) => truthy
		(:scene-button? (message-from  144 0 39 0)) => truthy
		(:scene-button? (message-from  144 0 49 0)) => truthy
		(:scene-button? (message-from  144 0 59 0)) => truthy
		(:scene-button? (message-from  144 0 69 0)) => truthy
		(:scene-button? (message-from  144 0 79 0)) => truthy
		(:scene-button? (message-from  144 0 89 0)) => truthy)		
	(fact "button-down? and button-up? are derived from velocity/data2"
		(:button-down? sample-message) => truthy
		(:button-up? sample-message) => falsey
		(:button-down? (message-from  144 0 23 0)) => falsey
		(:button-up? (message-from  144 0 23 0)) => truthy )
	(fact "velocity/data2 is identified"
		(:velocity sample-message) => 127
		(:data2 sample-message) => 127 )
	)


