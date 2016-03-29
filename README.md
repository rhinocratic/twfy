# twfy

Clojure bindings for the [TheyWorkForYou](http://www.theyworkforyou.com) [API](http://www.theyworkforyou.com/api/)

## Usage

To use twfy with Leiningen, add the following to the `:dependencies` section of your `project.clj`:

```clojure
[twfy "0.3.0-SNAPSHOT"]
```

Alternatively, add this to your Maven project's `pom.xml`:

```xml
<dependency>
  <groupId>twfy</groupId>
  <artifactId>twfy</artifactId>
  <version>0.3.0-SNAPSHOT</version>
</dependency>
```
The API methods all accept a map of parameters and (optionally, for asynchronous calls) a callback function that will be invoked with the response.

In order to invoke any of the API methods, you'll need to obtain a TWFY [API key](http://www.theyworkforyou.com/api/key), which may be supplied to the API methods via a :key entry in the parameters map.

Alternatively, the library can pick up the API key from an environment variable called TWFY_API_KEY, removing the need to supply it with each method invocation.  For testing purposes, you can add a reference to [`lein-environ`](https://github.com/weavejester/environ) to the `:plugins` section of your `project.clj`, and put the key in your `profiles.clj`:
```
{:dev
  {:env
    {:twfy-api-key "your-api-key-here"}}
 :test
  {:env
    {:twfy-api-key "your-api-key-here"}}}
```

## Examples

From a project REPL (synchronous call):

```clojure
=> (require '[twfy.core :as twfy])
nil
=> (require '[clojure.pprint :as pp])
nil
=> (pp/pprint (twfy/person {:id 10544 :key "your-api-key-here"}))
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
 {:given_name "Dennis",
  :party "Labour",
...
```
Asynchronous call:
```clojure
=> (twfy/hansard
     {:search "Investigatory Powers" :key "your-api-key-here"}
     (fn [result] (pp/pprint (first (:rows result)))))
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
