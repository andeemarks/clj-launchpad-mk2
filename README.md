# clj-launchpad-mk2 [<img src="https://travis-ci.org/andeemarks/clj-launchpad-mk2.png?branch=master" alt="Build Status" />](https://travis-ci.org/andeemarks/clj-launchpad-mk2) [![Clojars Project](https://img.shields.io/clojars/v/clj-launchpad-mk2.svg)](https://clojars.org/clj-launchpad-mk2)

This gem provides a Clojure interface to access the [Novation Launchpad MK2](https://global.novationmusic.com/launch/launchpad#) programmatically. The code started life as a clone of [Moumar's Clojar](https://github.com/moumar/clj-launchpad) and was subsequently updated to handle the MK2 version of the Launchpad.  The mapping of buttons and specification of colours completely changed when the MK2 was released, so lots of the original code in these areas has been re-written, but the interaction code is still largely intact.

## More Info

*   Novation's Launchpad MK2 MIDI [programmer's reference](https://global.novationmusic.com/sites/default/files/novation/downloads/10529/launchpad-mk2-programmers-reference-guide_0.pdf) was the sole source for helping me understand how to interact with the Launchpad.

## Requirements

## Compatibility

## Installation

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
(close lpad)				; disconnect
 ```

## To Do

## Copyright

Copyright (c) 2017 Andy Marks. See LICENSE for details.

