;; ## Clojure bindings for the TheyWorkForYou API

(ns twfy.core
  (:require [clojure.string :as str]
            [cheshire.core :as ch]
            [clojure.xml :as xml]
            [environ.core :refer [env]]
            [clj-time.coerce :as c]
            [clj-time.format :as f]))


(def ^{:private true} api-key
  "The \"They Work For You\" API key"
  (env :twfy-api-key))

(if (not api-key)
  (println "No key found for the \"They Work For You\" API!"))

(def ^{:private true} base-uri
  "The base URI of the \"They Work For You\" API. May be overridden by setting environment variable :twfy-base-api"
  (or (env :twfy-base-api) "http://theyworkforyou.com/api/"))

(defn- map2query
  "Translates a map into a query string."
  [m]
  (letfn [(urlencode [s] (java.net.URLEncoder/encode s "UTF-8"))]
    (->> m
      (map (fn [[k v]] (str (name k) "=" (urlencode v))))
      (str/join "&")
      (str "?"))))

(defn- build-uri
  "Build the URI for an API function from the function name and arguments"
  [fname args]
  (->> (assoc args :key api-key)
   map2query
   (str base-uri fname)))

(defmulti to-joda "Convert dates to Joda time instances" class)
(defmethod to-joda java.util.Date [d] (c/from-date d))
(defmethod to-joda java.sql.Date [d] (c/from-sql-date d))
(defmethod to-joda java.sql.Timestamp [d] (c/from-sql-time d))
(defmethod to-joda Long [d] (c/from-long d))
(defmethod to-joda String [d] (c/from-string d))
(defmethod to-joda org.joda.time.ReadableInstant [d] d)

(defn date2string
 "Convert a date to a string of the form yyyy-MM-dd"
 [d]
 (f/unparse (f/formatters :date) (to-joda d)))

(defn- preprocess-terms
  "Preprocess a map of arguments for an API function"
  [args]
  (into {} (map (fn [[k v]]
                  (cond
                    (some #{k} [:date :start_date :end_date]) [k (date2string v)]
                    (some #{k} [:id :person :pid :page :num]) [k (str v)]
                    (some #{k} [:order :type]) [k (name v)]
                    :default [k v]))
                args)))

(defn- invoke-twfy
  "Invokes the \"They Work For You\" API"
  [fname terms callback]
  (if (not api-key)
    "No key found for the \"They Work For You\" API!"
    (-> fname
      (build-uri (preprocess-terms terms))
      slurp
      (ch/parse-string true)
      callback)))

(defmacro def-twfy-call
  "Wraps the invocation of a twfy API method in a function that optionally accepts a
   callback. Arguments:
   - fname : The name of the function to be defined.
   - twfy-api-name : The name of the twfy API method to be invoked.
   - docstring : The docstring for the function.
   - pre : A precondition to be applied to the arguments of the function"
  [fname twfy-api-name docstring pre]
  `(defn ~(symbol fname) {:arglists '([~'terms] [~'terms ~'callback]) :doc ~docstring}
    ([terms#]
     (~(symbol fname) terms# identity))
    ([terms# callback#]
     {:pre [(~pre terms#)]}
     (invoke-twfy ~twfy-api-name terms# callback#))))

(defn- parse-xml
  "Parse XML into a Clojure data structure (used by the \"boundary\" API call)"
  [x]
  (xml/parse (java.io.ByteArrayInputStream. (.getBytes x))))

;; ## Main API Functions

(def-twfy-call
  "convert-url"
  "convertURL"
  "Converts a parliament.uk Hansard URL into a TheyWorkForYou one, if possible.
   Accepts a map containing :url (the URL to be converted)"
   (fn [terms] (some #{:url} (keys terms))))

(def-twfy-call
  "constituency"
  "getConstituency"
  "Search for a UK parliamentary constituency.  The search terms should be a
   map containing at least one of :name, :postcode"
   (fn [terms] (some #{:name :postcode} (keys terms))))

(def-twfy-call
  "constituencies"
  "getConstituencies"
  "Get a list of UK parliamentary constituencies. The search terms should be a
   map containing one of :date or :search (a string).
   :date may be a string (e.g. \"2016-01-01T13:45:42.094Z\"), a java.util.Date,
   a Long, a java.sql.Date, a java.sql.Timestamp or an
   org.joda.time.ReadableInstant (e.g. a Clojure instant).
   If :date is specified, a list of constituencies as at the given date is
   returned.
   If :search is specified, a list of constituencies matching the given search
   term is returned.
   At present, only one of :date, :search is accepted by the They Work For You
   API.  If both are provided, the date will be used in preference to the
   search string."
   (fn [terms] (some #{:date :search} (keys terms))))

(def-twfy-call
  "person"
  "getPerson"
  "Get details for the person with the given id.
   Accepts a map of options and an optional callback.
   Options:
   - :id (required) The person ID for which to retrieve details"
  (fn [terms] (some #{:id} (keys terms))))

(def-twfy-call
  "mp"
  "getMP"
  "Return details for a particular MP.
   Accepts a map of options and an optional callback.
  Options - at least one of the following must be supplied:
  - :postcode (optional)
  - :constituency (optional) The name of a constituency.  Note that this will
    only return the current/most recent entry in the database.
  - :id (optional) The person ID for the member
  Additionally, for the postcode and constituency options, the following may
  be provided:
  - :always_return (optional) whether to try to return an MP even if the seat
    is currently vacant."
  (fn [terms] (some #{:postcode :constituency :id} (keys terms))))

(def-twfy-call
  "mp-info"
  "getMPInfo"
  "Returns additional information for a particular person.
   Accepts a map of options and an optional callback.
   Options:
  - :id (required) The person ID
  - :fields (optional) The fields required in the response, comma separated
    (blank for all)"
  (fn [terms] (some #{:id} (keys terms))))

(def-twfy-call
  "mps-info"
  "getMPsInfo"
  "Return additional information for one or more people.
   Accepts a map of options and an optional callback.
  Options:
  - :id (required) The person IDs, as a comma separated string
  - :fields (optional) The fields required in the response, comma separated
    (blank for all)"
  (fn [terms] (some #{:id} (keys terms))))

(def-twfy-call
  "mps"
  "getMPs"
  "Return a list of MPs.
   Accepts a map of options and an optional callback.
   Options:
   - :date (optional) Return the list of MPs as at this date
   - :party (optional) Return the list of MPs from the given party
   - :search (optional) Return the MPs whose names contain the given search
     string"
  (fn [terms] (some #{:date :party :search} (keys terms))))

(def-twfy-call
  "lord"
  "getLord"
  "Return a particular lord.
   Accepts a map of options and an optional callback.
   Options:
   - :id (required) The person ID of the lord"
   (fn [terms] (some #{:id} (keys terms))))

(def-twfy-call
  "lords"
  "getLords"
  "Return a list of lords.
   Accepts a map of options and an optional callback.
   Options:
  - :date (optional) Return the list of lords as at this date (NB date is when
    the lord is introduced in Parliament)
  - :party (optional) Return the lords from the given party
  - :search (optional) Return the lords whose names contain the given search
    string
  If :date is provided, it will be used in preference to the other terms,
  which will be ignored."
  (fn [terms] (some #{:date :party :search} (keys terms))))

(def-twfy-call
  "mla"
  "getMLA"
  "Return a particular MLA.
   Accepts a map of options and an optional callback.
   Options - at least one of the following must be supplied:
  - :postcode (optional) Return the MLA for the given postcode
  - :constituency (optional) The name of a constituency
  - :id (optional) The person ID of the MLA"
  (fn [terms] (some #{:postcode :constituency :id} (keys terms))))

(def-twfy-call
  "mlas"
  "getMLAs"
  "Return a list of MLAs.
   Accepts a map of options and an optional callback.
   Options:
  - :date (optional) Return the list of MLAs as at the given date
  - :party (optional) Return the list of MLAs from the given party
  - :search (optional) Return the list of MLAs whose names contain the given
    search string"
  (fn [terms] (some #{:date :party :search} (keys terms))))

(def-twfy-call
  "msp"
  "getMSP"
  "Return a particular MSP.
   Accepts a map of options and an optional callback.
   Options - at least one of the following must be supplied:
  - :postcode (optional) Return the MSP for a particular postcode
  - :constituency (optional) The name of a constituency
  - :id (optional) The person ID of the MSP"
  (fn [terms] (some #{:postcode :constituency :id} (keys terms))))

(def-twfy-call
  "msps"
  "getMSPs"
  "Return a list of MSPs.
   Accepts a map of options and an optional callback.
   Options:
  - :date (optional) Return the list of MSPs as at the given date
  - :party (optional) Return the list of MSPs from the given party
  - :search (optional) Return the list of MSPs whose names contain the given
    search string"
  (fn [terms] (some #{:date :party :search} (keys terms))))

(def-twfy-call
  "geometry"
  "getGeometry"
  "Return geometry information for a constituency.
   Accepts a map of options and an optional callback.
   Options:
  - :name (required) The name of the constituency"
  (fn [terms] (some #{:name} (keys terms))))

(defn boundary
  "Return the KML file for a UK Parliament constituency.
   Accepts a map of options and an optional callback.
   Options:
  - :name (required) The name of the constituency"
  ([terms]
   (boundary terms identity))
  ([terms callback]
   {:pre [(some #{:name} (keys terms))]}
   (-> "getBoundary"
    (build-uri (preprocess-terms terms))
    slurp
    parse-xml
    callback)))

(def-twfy-call
  "committee"
  "getCommittee"
  "Return the members of a select committee.
   Accepts a map of options and an optional callback.
   Options - at least one of the following must be supplied:
  - :name (optional) Return the members of the committee matching this name or,
    if more than one committee is found, the names of the committees
  - :date (optional) Return the members of the committee as at this date.
  N.B. As at 16/11/2012, a date prior to that of the 2010 general election must
  be supplied in order to yield any results"
  (fn [terms] (some #{:name :date} (keys terms))))

(def-twfy-call
  "debates"
  "getDebates"
  "Returns debates for the search terms provided.
   Accepts a map of options and an optional callback.
   Options - note that (as at 16/11/2012) only one of the optional items may be
   supplied:
   - :type (required) One of :commons, :westminsterhall, :lords, :scotland or
       :northernireland
   - :date (optional) Return debates for this date
   - :search (optional) Return debates containing this term
   - :person (optional) Return debates by person ID
   - :gid (optional) Return the speech or debate matching this GID
   - :order (optional, in conjunction with search or person) :d for date
     ordering, :r for relevance ordering
   - :page (optional, in conjunction with search or person) The page of results
     to return
   - :num (optional, in conjunction with search or person) The number of
     results to return"
  (fn [terms] (and (some? (:type terms)) (= 1 (count (select-keys terms #{:date :search :person :gid}))))))

(def-twfy-call
  "wrans"
  "getWrans"
  "Returns written answers for the search terms provided.
   Accepts a map of options and an optional callback.
   Options - note that (as at 16/11/2012) only one of the following may be
   supplied:
   - :date (optional) Return written answers for this date
   - :search (optional) Return written answers containing this term
   - :person (optional) Return written answers by person ID
   - :gid (optional) Return the written answer matching this GID
   - :order (optional, in conjunction with search or person) :d for date
     ordering, :r for relevance ordering
   - :page (optional, in conjunction with search or person) The page of results
     to return
   - :num (optional, in conjunction with search or person) The number of
     results to return"
  (fn [terms] (= 1 (count (select-keys terms #{:date :search :person :gid})))))

(def-twfy-call
  "wms"
  "getWMS"
  "Returns written ministerial statements for the search terms provided.
   Accepts a map of options and an optional callback.
  Options - note that (as at 16/11/2012) only one of the following may be
  supplied:
   - :date (optional) Return written ministerial statements for this date
   - :search (optional) Return written ministerial statements containing this
     term
   - :person (optional) Return written ministerial statements by person ID
   - :gid (optional) Return the written ministerial statement matching this GID
   - :order (optional, in conjunction with search or person) :d for date
     ordering, :r for relevance ordering
   - :page (optional, in conjunction with search or person) The page of results
     to return
   - :num (optional, in conjunction with search or person) The number of
     results to return"
  (fn [terms] (= 1 (count (select-keys terms #{:date :search :person :gid})))))

(def-twfy-call
  "hansard"
  "getHansard"
  "Return all of Hansard for the search terms provided.
   Accepts a map of options and an optional callback.
   Options - note that (as at 16/11/2012) only one of the following may be
   supplied:
   - :search (optional) Return data containg this term
   - :person (optional) Return data by person ID
   - :order (optional, in conjunction with search or person) :d for date
     ordering, :r for relevance ordering
   - :page (optional, in conjunction with search or person) The page of results
     to return
   - :num (optional, in conjunction with search or person) The number of
     results to return"
  (fn [terms] (= 1 (count (select-keys terms #{:search :person})))))

(def-twfy-call
  "comments"
  "getComments"
  "Return comments left on TheyWorkForYou.  With no arguments, returns the most
   recent comments in reverse date order.
   Accepts a map of options and an optional callback.
   Options:
   - :pid (optional) Return comments made on a particular person ID (MP or Lord)
   - :start_date (optional) Return comments made on or after this date
   - :end_date (optional) Return comments made on or before this date
   - :search (optional) Return comments containing this term
   The following options are as yet only implemented by the API in combination
   with :search
   - :page (optional) The page of results to return
   - :num (optional) The number of result to return"
   (fn [terms] true))
