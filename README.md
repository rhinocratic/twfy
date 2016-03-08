# twfy

Clojure bindings for the [TheyWorkForYou](http://www.theyworkforyou.com) [API](http://www.theyworkforyou.com/api/)

## Usage

To use twfy with Leiningen, add the following to the `:dependencies` section of your `project.clj`:

```clojure
[twfy "0.2.0-SNAPSHOT"]
```

Alternatively, add this to your Maven project's `pom.xml`:

```xml
<dependency>
  <groupId>twfy</groupId>
  <artifactId>twfy</artifactId>
  <version>0.2.0-SNAPSHOT</version>
</dependency>
```

In order to invoke any of the API methods, you'll need to obtain a TWFY [API key](http://www.theyworkforyou.com/api/key), which this library expects to pick up from an environment variable called TWFY_API_KEY.  For testing purposes, you can put the key in your profiles.clj file:
```
{:dev
  {:env
    {:twfy-api-key "your-api-key-here"}}
 :test
  {:env
    {:twfy-api-key "your-api-key-here"}}}
```

## Examples

From a project REPL:

```clojure
=> (require '[twfy.core :as twfy])
nil
=> (require '[clojure.pprint :as pp])
nil
=> (pp/pprint (twfy/person {:id 10544}))
({:given_name "Dennis",
  :party "Labour",
  :left_reason "still_in_office",
  :lastupdate "2015-05-08 07:12:04",
  :person_id "10544",
  :constituency "Bolsover",
  :image_width 49,
  :member_id "41215",
  :title "",
  :family_name "Skinner",
  :entered_reason "general_election",
  :url "/mp/10544/dennis_skinner/bolsover",
  :image "/images/mps/10544.jpg",
  :entered_house "2015-05-08",
  :house "1",
  :full_name "Dennis Skinner",
  :image_height 59,
  :left_house "9999-12-31"}
...

=> (def evil (twfy/hansard {:search "Investigatory Powers"}))
#'user/evil
=> (pp/pprint (first (:rows evil)))
{:body "uk",
 :calendar_id "8399",
 :chamber "Commons: Main Chamber",
 :committee_name "",
 :created "2016-03-04 06:25:10",
 :debate_type "Legislation",
 :deleted "0",
 :event_date "2016-03-15",
 :extract "<span class=\"hi\">Investigatory Powers</span>  Bill - 2nd reading &#8211; Theresa May. <span class=\"future_meta\">Legislation</span>",
 :gid "8399",
 :hdate "2016-03-15",
 :hpos "2",
 :id "8399",
...
```

The API calls return Clojure-native data structures.

## License

Copyright &copy; 2016 Andrew Baxter

Distributed under the Eclipse Public License, the same as Clojure.
