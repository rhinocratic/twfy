(defproject twfy "0.3.1-SNAPSHOT"
  :description "Clojure bindings for the [TheyWorkForYou](http://www.theyworkforyou.com/) [API](http://www.theyworkforyou.com/api/)"
  :url "https://github.com/rhinocratic/twfy"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [cheshire "5.5.0"]
                 [environ "1.0.2"]
                 [clj-time "0.11.0"]]
  :plugins [[lein-environ "1.0.2"]
            [com.jakemccrary/lein-test-refresh "0.14.0"]])
