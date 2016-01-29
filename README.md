# twfy

Clojure bindings for the [TheyWorkForYou](http://www.theyworkforyou.com) [API](http://www.theyworkforyou.com/api/)

## Usage

To use twfy with Leiningen, add the following to the `:dependencies` section of your Leiningen `project.clj`, then execute `lein deps` to bring in the dependency:

```clojure
[twfy "0.1.0-SNAPSHOT"]
```

Or, add this to your Maven project's `pom.xml`:

```xml
<dependency>
  <groupId>twfy</groupId>
  <artifactId>twfy</artifactId>
  <version>0.1.0-SNAPSHOT</version>
</dependency>
```

Once that's done, you can import the namespace.  You'll need to set your TWFY API [key](http://www.theyworkforyou.com/api/key) before invoking any of the API methods:

```clojure
=> (require '[twfy.core :as twfy])
nil
=> (twfy/set-api-key! "YourKeyHere")
"YourKeyHere"
=> (pprint (twfy/get-person :id 10544))
[{:last_name "Skinner",
  :constituency "Bolsover",
  :person_id "10544",
  :lastupdate "2010-05-07 03:40:47",
  :image_height 59,
  :entered_reason "general_election",
  :member_id "40098",
  :image_width 49,
  :party "Labour",
  :image "/images/mps/10544.jpg",
  :entered_house "2010-05-06",
  :title "",
  :url "/mp/dennis_skinner/bolsover",
  :left_house "9999-12-31",
  :full_name "Dennis Skinner",
  :first_name "Dennis",
  :house "1",
  :left_reason "still_in_office"}
...
```

By default, the API calls return data as Clojure-native data structures, but other formats are available by providing an `:output` value, as described on the TWFY API page.

## License

Copyright &copy; 2016 Andrew Baxter

Distributed under the Eclipse Public License, the same as Clojure.
