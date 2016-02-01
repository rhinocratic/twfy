(defproject twfy "0.2.0-SNAPSHOT"
  :description "Clojure bindings for the [TheyWorkForYou](http://www.theyworkforyou.com/) [API](http://www.theyworkforyou.com/api/)"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [cheshire "5.5.0"]
                 [environ "1.0.2"]
                 [clj-time "0.11.0"]]
  :plugins [[lein-environ "1.0.2"]
            [com.jakemccrary/lein-test-refresh "0.12.0"]
            [venantius/ultra "0.4.0"]
            [lein-marginalia "0.7.1"]])
