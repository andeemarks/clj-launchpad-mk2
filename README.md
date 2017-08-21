# clj-launchpad-mk2 
[<img src="https://travis-ci.org/andeemarks/clj-launchpad-mk2.png?branch=master" alt="Build Status" />](https://travis-ci.org/andeemarks/clj-launchpad-mk2) [![Clojars Project](https://img.shields.io/clojars/v/clj-launchpad-mk2.svg)](https://clojars.org/clj-launchpad-mk2) [![MIT Licence](https://badges.frapsoft.com/os/mit/mit.svg?v=103)](https://opensource.org/licenses/mit-license.php)

This library provides a Clojure interface to access the [Novation Launchpad MK2](https://global.novationmusic.com/launch/launchpad#) programmatically. The code started life as a clone of [Moumar's Clojar](https://github.com/moumar/clj-launchpad) and was subsequently updated to handle the MK2 version of the Launchpad.  The mapping of buttons and specification of colours completely changed when the MK2 was released, so lots of the original code in these areas has been re-written, but much of the interaction code is still largely intact.

## More Info

*   Novation's Launchpad MK2 MIDI [programmer's reference](https://global.novationmusic.com/sites/default/files/novation/downloads/10529/launchpad-mk2-programmers-reference-guide_0.pdf) was the sole source for helping me understand how to interact with the Launchpad.

## Installation

#### Leiningen/Boot

```[clj-launchpad-mk2 "0.0.3"]```

#### Gradle

```compile "clj-launchpad-mk2:clj-launchpad-mk2:0.0.3"```

#### Maven
```
<dependency>
  <groupId>clj-launchpad-mk2</groupId>
  <artifactId>clj-launchpad-mk2</artifactId>
  <version>0.0.3</version>
</dependency>
```

## REPL Usage

```clojure
(require '[clj-launchpad-mk2 :refer :all])

(def lpad (open))			; first argument to all subsequent calls

(light-grid lpad 23) 			; light all buttons
(light-row lpad 3 66) 			; light a single row of buttons
(light-column lpad 3 99) 		; light a single column of buttons
(flash lpad 5 5 60) 			; flash a single cell between it's current colour and 60
(pulse lpad 4 4 10) 			; pulse (adjust brightness) of a single cell 
(light-cc lpad CC_CURSOR_RIGHT 11) 	; light a specific control button
(light-cell lpad 0 0 35) 		; light a specific cell
(clear-grid lpad) 			; clear all cells
(scroll-text-once lpad "Launchpad" 54)	; scrolling text once only
(scroll-text lpad "MK2" 60)		; scroll text in a loop
(scroll-stop lpad)			; stop looped scrolling
(rgb lpad 0 0 0 0 0)			; progressively increase the RED component of a series of cells
(rgb lpad 0 1 16 0 0)
(rgb lpad 0 2 32 0 0)
(rgb lpad 0 3 48 0 0)
(rgb lpad 0 4 63 0 0)
(set-button-press-handler 		; "random" light pressed button
	lpad 		
	(fn 
		[msg timestamp]
		(light-cell 
			lpad 
			(:x msg) 
			(:y msg) 
			(- 127 (+ (:x msg) (:y msg))))))

(close lpad)				; disconnect
 ```

## To Do

* Fix input handling

## Copyright

Copyright (c) 2017 Andy Marks. See LICENSE for details.

