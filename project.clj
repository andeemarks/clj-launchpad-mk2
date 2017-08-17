(defproject clj-launchpad-mk2 "0.0.1"
  :description "Novation Launchpad MK2 library for clojure"
  :url "https://github.com/andeemarks/clj-launchpad-mk2"
  :license {:name "MIT"
            :url "https://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.8.0"]]
  :plugins
    [
      [lein-cljfmt "0.5.6"]]
  :profiles 
  	{:dev 
  		{:dependencies 
  			[
          [com.jakemccrary/lein-test-refresh "0.20.0"]
          [lein-midje "3.2.1"]
          [clansi "1.0.0"]
          [com.taoensso/timbre "4.10.0"]
  				[midje "1.8.3" :exclusions [org.clojure/clojure]]]}})  
