(defproject clj-launchpad-mk2 "1.0.1"
  :description "Novation Launchpad MK2 library for clojure"
  :url "https://github.com/andeemarks/clj-launchpad-mk2"
  :license {:name "MIT"
            :url "https://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.8.0"]]
  :deploy-repositories [["clojars"  {:url "https://clojars.org/repo" :creds :gpg}]]  
  :main clj-launchpad-mk2.demo
  :aot [clj-launchpad-mk2.demo]
  :codox {:output-path "docs" 
          :metadata {:doc/format :markdown} 
          :source-uri "https://github.com/andeemarks/clj-launchpad-mk2/blob/master/{filepath}#L{line}"}
  :plugins
    [
      [lein-cljfmt "0.5.6"]]
  :profiles 
  	{:dev 
  		{	:plugins [[lein-midje "3.2.1"]]
  			:dependencies 
  			[
          [com.jakemccrary/lein-test-refresh "0.20.0"]
          [clansi "1.0.0"]
          [com.taoensso/timbre "4.10.0"]
  				[midje "1.8.3" :exclusions [org.clojure/clojure]]]}})  
