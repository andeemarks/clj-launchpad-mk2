(ns clj-launchpad-mk2.core
  (:require [clj-launchpad-mk2.midi.core :as midi]))

(defn- validate-coordinates [x y]
  (when (or (> x 7) (< x 0))
    (throw (IllegalArgumentException. "x must be in range 0-7 inclusive")))

  (when (or (> y 7) (< y 0))
    (throw (IllegalArgumentException. "y must be in range 0-7 inclusive"))))

(defn- validate-rgb-component [component]
  (when (or (> component 63) (< component 0))
    (throw (IllegalArgumentException. "RGB components must be in range 0-63 inclusive"))))

(defn- validate-rgb [red green blue]
  (validate-rgb-component red)
  (validate-rgb-component green)
  (validate-rgb-component blue))

(defn- validate-color [color]
  (when (or (> color 127) (< color 0))
    (throw (IllegalArgumentException. "color must be in range 0-127 inclusive"))))

(defn- coordinate-pair-to-index [x y] (+ (+ x 1) (* 10 (+ y 1))))

(defn light-cell
  "set a cell grid on or off.

  * x can be 0 to 8 inclusive (8 is for the top buttons)
  * y can be 0 to 8 inclusive (8 is for the most right buttons)
  * color-description must be 0 to 127 inclusive

  Examples:
  ```
  (light-cell lpad 1 2 127)
  (light-cell lpad 1 2 0)
  ```
  "
  [lpad x y color-description]
  (validate-coordinates x y)
  (validate-color color-description)
  (midi/send-midi lpad midi/CHANNEL_1_NOTE_ON (coordinate-pair-to-index x y) color-description))

(defn- scroll-text-common
  [lpad text color-description loop-flag]
  (let [encoded-text (map #(int (char %)) text)]
    (validate-color color-description)
    (midi/send-midi-sysex-scroll lpad encoded-text midi/SYSEX_SCROLL_STATUS color-description loop-flag)))

(defn scroll-text-once
  "Scroll (right->left) the specified text once only.  The text will be displayed in the specified color.

  * text is the text to scroll.
  * color-description is the integer code for the text color (0-127 inclusive).

  Examples:
  ```
  (scroll-text-once lpad \"Hello, world!\" 54)
  ```
  "
  [lpad text color-description]
  (scroll-text-common lpad text color-description 0))

(defn scroll-text
  "Scroll (right->left) the specified text continually.  The text will be displayed in the specified color.
  
  * text is the text to scroll.
  * color-description is the integer code for the text color (0-127 inclusive).

  Examples:
  ```
  (scroll-text lpad \"Hello, world!\" 54)
  ```
  "
  [lpad text color-description]
  (scroll-text-common lpad text color-description 1))

(defn scroll-stop
  "Stop any currently scrolling text.

  Examples:
  ```
  (scroll-stop lpad)
  ```"
  [lpad]
  (midi/send-midi-sysex lpad midi/SYSEX_SCROLL_STATUS))

(defn light-row
  "Set all the buttons in the specified row to the specified color.
  
  * y can be 0 to 8 inclusive (8 is for the most right buttons)
  * color-description must be 0 to 127 inclusive

  Examples:
  ```
  (light-row lpad 0 43)
  ```
  "
  [lpad y color-description]
  (validate-coordinates 0 y)
  (validate-color color-description)
  (midi/send-midi-sysex lpad midi/SYSEX_LIGHT_ROW_STATUS y color-description))

(defn rgb
  "Set the specified buttons to the specified combination of red, green and blue.
  
  * x can be 0 to 8 inclusive (8 is for the most right buttons)
  * y can be 0 to 8 inclusive (8 is for the most right buttons)
  * red is the brightness of the red component of the button's LED (0-63 inclusive)
  * green is the brightness of the green component of the button's LED (0-63 inclusive)
  * blue is the brightness of the blue component of the button's LED (0-63 inclusive)

  Examples:
  ```
  (rgb lpad 3 2 63 0 24)
  ```
  "
  [lpad x y red green blue]
  (validate-coordinates x y)
  (validate-rgb red green blue)
  (midi/send-midi-sysex lpad midi/SYSEX_RGB_STATUS (coordinate-pair-to-index x y) red green blue))

(defn light-column
  "Set all the buttons in the specified column to the specified color.
  
  * x can be 0 to 8 inclusive (8 is for the most right buttons)
  * color-description must be 0 to 127 inclusive

  Examples:
  ```
  (light-column lpad 3 12)
  ```
  "
  [lpad x color-description]
  (validate-coordinates x 0)
  (validate-color color-description)
  (midi/send-midi-sysex lpad midi/SYSEX_LIGHT_COLUMN_STATUS x color-description))

(defn light-grid
  "Set all the buttons on the grid to the specified color.
  
  * color-description must be 0 to 127 inclusive

  Examples:
  ```
  (light-grid lpad 52)
  ```
  "
  [lpad color-description]
  (validate-color color-description)
  (midi/send-midi-sysex lpad midi/SYSEX_LIGHT_GRID_STATUS color-description))

(defn clear-cell
  "set the specified button off.
  
  * x can be 0 to 8 inclusive (8 is for the top buttons)
  * y can be 0 to 8 inclusive (8 is for the most right buttons)

  Examples:
  ```
  (clear-cell lpad 0 2)
  ```
  "
  [lpad x y]
  (validate-coordinates x y)
  (midi/send-midi lpad midi/CHANNEL_1_NOTE_ON (coordinate-pair-to-index x y) 0))

(defn light-cc
  "set a top row control button to the specified color.
  
  * cc-ref is the integer/hex number identifying the button (see CC_ constants in midi/core.clj).
  * color-description must be 0 to 127 inclusive

  Examples:
  ```
  (light-cc lpad CC_CURSOR_RIGHT 78)
  ```
  "
  [lpad cc-ref color-description]
  (validate-color color-description)
  (midi/send-midi lpad midi/CC_NOTE_ON cc-ref color-description))

(defn flash
  "flash the specified button between the current color and specified color

  * x can be 0 to 8 inclusive (8 is for the top buttons)
  * y can be 0 to 8 inclusive (8 is for the most right buttons)
  * color-description must be 0 to 127 inclusive

  Examples:
  ```
  (flash lpad 1 2 127)
  (flash lpad 1 2 0)
  ```
  "
  [lpad x y color-description]
  (validate-coordinates x y)
  (validate-color color-description)
  (midi/send-midi lpad midi/CHANNEL_2_NOTE_ON (coordinate-pair-to-index x y) color-description))

(defn pulse
  "pulse (i.e., vary the brightness) the specified button from off to the specified color
  
  * x can be 0 to 8 inclusive (8 is for the top buttons)
  * y can be 0 to 8 inclusive (8 is for the most right buttons)
  * color-description must be 0 to 127 inclusive

  Examples:
  ```
  (pulse lpad 1 2 127)
  ```
  "
  [lpad x y color-description]
  (validate-coordinates x y)
  (validate-color color-description)
  (midi/send-midi lpad midi/CHANNEL_3_NOTE_ON (coordinate-pair-to-index x y) color-description))

(defn clear-grid
  "Turn off all the grid buttons.

  Examples:
  ```
  (clear-grid lpad)
  ```
  "
  [lpad]
  (midi/send-midi-sysex lpad midi/SYSEX_LIGHT_GRID_STATUS 0))

(defn open
  "open a connection to the launchpad and return a launchpad object suitable for the calls of this library.

  See [[midi.core/open]].
  "
  []
  (midi/open))

(defn close
  "close the launchpad device.

  See [[midi.core/close]].

  Examples:
  ```
  (close lpad)
  ```
  "
  [lpad]
  (midi/close lpad))
