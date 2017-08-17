(ns clj-launchpad-mk2.test.core
  (:use midje.sweet) 
  (:require [clj-launchpad-mk2 :refer :all]))

(facts "about `split`"
  (clojure.string/split "a/b/c" #"/") => ["a" "b" "c"]
  (clojure.string/split "" #"irrelvant") => [""]
  (clojure.string/split "no regexp matches" #"a+\s+[ab]") => ["no regexp matches"])