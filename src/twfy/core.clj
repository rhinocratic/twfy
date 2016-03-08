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
                    (= k :date) [k (date2string v)]
                    (= k :id) [k (str v)]
                    :default [k v]))
                args)))

(defn- invoke-twfy
  "Invokes the \"They Work For You\" API"
  ([fname terms]
   (-> fname
    (build-uri (preprocess-terms terms))
    slurp
    (ch/parse-string true))))


;; ## Main API Functions

(defn convert-url
  "Converts a parliament.uk Hansard URL into a TheyWorkForYou one, if possible. Accepts a map containing :url (the URL to be converted)"
  [terms]
  {:pre [(some #{:url} (keys terms))]}
  (invoke-twfy "convertURL" terms))

(defn constituency
  "Search for a UK parliamentary constituency.  The search terms should be a map containing at least one of :name, :postcode"
  [terms]
  {:pre [(some #{:name :postcode} (keys terms))]}
  (invoke-twfy "getConstituency" terms))

(defn constituencies
  "Get a list of UK parliamentary constituencies. The search terms should be a map containing one of :date or :search (a string).
   :date may be a string (e.g. \"2016-01-01T13:45:42.094Z\"), a java.util.Date, a Long, a java.sql.Date, a java.sql.Timestamp or an org.joda.time.ReadableInstant (e.g. a Clojure instant).
   If :date is specified, a list of constituencies as at the given date is returned.
   If :search is specified, a list of constituencies matching the given search term is returned.
   At present, only one of :date, :search is accepted by the They Work For You API.  If both are provided, the date will be used in preference to the search string."
  [terms]
  {:pre [(some #{:date :search} (keys terms))]}
  (invoke-twfy "getConstituencies" terms))

(defn person
  "Get details for the person with a given id. Accepts a map containing :id (a string)"
  [terms]
  {:pre [(some #{:id} (keys terms))]}
  (invoke-twfy "getPerson" terms))

(defn mp
  "Return details for a particular MP.
   Options - at least one of the following must be supplied:
  - :postcode (optional)
  - :constituency (optional) The name of a constituency.  Note that this will only return the current/most recent entry in the database.
  - :id (optional) The person ID for the member
  Additionally, for the postcode and constituency options, the following may be provided:
  - :always_return (optional) whether to try to return an MP even if the seat is currently vacant."
  [terms]
  {:pre [(some #{:postcode :constituency :id} (keys terms))]}
  (invoke-twfy "getMP" terms))

(defn mp-info
  "Returns additional information for a particular person
   Options:
  - :id (required) The person ID
  - :fields (optional) The fields required in the response, comma separated (blank for all)"
  [terms]
  {:pre [(some #{:id} (keys terms))]}
  (invoke-twfy "getMPInfo" terms))

(defn mps-info
  "Return additional information for one or more people.
  Options:
  - :id (required) The person IDs, as a comma separated string
  - :fields (optional) The fields required in the response, comma separated (blank for all)"
  [terms]
  {:pre [(some #{:id} (keys terms))]}
  (invoke-twfy "getMPsInfo" terms))

(defn mps
  "Return a list of MPs
   Options:
   - :date (optional) Return the list of MPs as at this date
   - :party (optional) Return the list of MPs from the given party
   - :search (optional) Return the MPs whose names contain the given search string"
  [terms]
  {:pre [(some #{:date :party :search} (keys terms))]}
  (invoke-twfy "getMPs" terms))

(defn lord
  "Return a particular lord.
   Options:
   - :id (required) The person ID of the lord"
  [terms]
  {:pre [(some #{:id} (keys terms))]}
  (invoke-twfy "getLord" terms))

(defn lords
  "Return a list of lords.
   Options:
  - :date (optional) Return the list of lords as at this date (NB date is when the lord is introduced in Parliament)
  - :party (optional) Return the lords from the given party
  - :search (optional) Return the lords whose names contain the given search string
  If :date is provided, it will be used in preference to the other terms, which will be ignored."
  [terms]
  {:pre [(some #{:date :party :search} (keys terms))]}
  (invoke-twfy "getLords" terms))

(defn mla
  "Return a particular MLA.
   Options - at least one of the following must be supplied:
  - :postcode (optional) Return the MLA for the given postcode
  - :constituency (optional) The name of a constituency
  - :id (optional) The person ID of the MLA"
  [terms]
  {:pre [(some #{:postcode :constituency :id} (keys terms))]}
  (invoke-twfy "getMLA" terms))

(defn mlas
  "Return a list of MLAs.
   Options:
  - :date (optional) Return the list of MLAs as at the given date
  - :party (optional) Return the list of MLAs from the given party
  - :search (optional) Return the list of MLAs whose names contain the given search string"
  [terms]
  {:pre [(some #{:date :party :search} (keys terms))]}
  (invoke-twfy "getMLAs" terms))

(defn msp
  "Return a particular MSP.
   Options - at least one of the following must be supplied:
  - :postcode (optional) Return the MSP for a particular postcode
  - :constituency (optional) The name of a constituency
  - :id (optional) The person ID of the MSP"
  [terms]
  {:pre [(some #{:postcode :constituency :id} (keys terms))]}
  (invoke-twfy "getMSP" terms))

(defn msps
  "Return a list of MSPs.
   Options:
  - :date (optional) Return the list of MSPs as at the given date
  = :party (optional) Return the list of MSPs from the given party
  = :search (optional) Return the list of MSPs whose names contain the given search string"
  [terms]
  {:pre [(some #{:date :party :search} (keys terms))]}
  (invoke-twfy "getMSPs" terms))

(defn geometry
  "Return geometry information for a constituency.
   Options:
  - :name (required) The name of the constituency"
  [terms]
  {:pre [(some #{:name} (keys terms))]}
  (invoke-twfy "getGeometry" terms))

(defn- parse-xml
  [x]
  (xml/parse (java.io.ByteArrayInputStream. (.getBytes x))))

(defn boundary
  "Return the KML file for a UK Parliament constituency.
   Options:
  - :name (required) The name of the constituency"
  [terms]
  {:pre [(some #{:name} (keys terms))]}
  (-> "getBoundary"
   (build-uri (preprocess-terms terms))
   slurp
   (parse-xml)))

; (defn get-committee
;   "Return the members of a select committee.
;    Options - at least one of the following must be supplied:
;
;   - :name (optional) Return the members of the committee matching this name or, if more than one
;     committee is found, the names of the committees
;   - :date (optional) Return the members of the committee as at this date.
;   N.B. As at 16/11/2012, a date prior to that of the 2010 general election must be supplied in order
;   to yield any results"
;   [& {:as opts}]
;   (call-api "getCommittee" opts nil))
;
; (defn get-debates
;   "Returns debates.
;    Options - note that (as at 16/11/2012) only one of the optional items may be supplied:
;
;    - :type (required) One of \"commons\", \"westminsterhall\", \"lords\", \"scotland\" or \"northernireland\"
;    - :date (optional) Return debates for this date
;    - :search (optional) Return debates containing this term
;    - :person (optional) Return debates by person ID
;    - :gid (optional) Return the speech or debate matching this GID
;    - :order (optional, in conjunction with search or person) 'd' for date ordering, 'r' for relevance ordering
;    - :page (optional, in conjunction with search or person) The page of results to return
;    - :num (optional, in conjunction with search or person) The number of results to return"
;   [& {:as opts}]
;   (call-api "getDebates" opts nil))
;
; (defn get-wrans
;   "Returns written answers.
;    Options - note that (as at 16/11/2012) only one of the following may be supplied:
;
;    - :date (optional) Return written answers for this date
;    - :search (optional) Return written answers containing this term
;    - :person (optional) Return written answers by person ID
;    - :gid (optional) Return the written answer matching this GID
;    - :order (optional, in conjunction with search or person) 'd' for date ordering, 'r' for relevance ordering
;    - :page (optional, in conjunction with search or person) The page of results to return
;    - :num (optional, in conjunction with search or person) The number of results to return"
;   [& {:as opts}]
;   (call-api "getWrans" opts nil))
;
; (defn get-wms
;   "Returns written ministerial statements.
;   Options - note that (as at 16/11/2012) only one of the following may be supplied:
;
;    - :date (optional) Return written ministerial statements for this date
;    - :search (optional) Return written ministerial statements containing this term
;    - :person (optional) Return written ministerial statements by person ID
;    - :gid (optional) Return the written ministerial statement matching this GID
;    - :order (optional, in conjunction with search or person) 'd' for date ordering, 'r' for relevance ordering
;    - :page (optional, in conjunction with search or person) The page of results to return
;    - :num (optional, in conjunction with search or person) The number of results to return"
;   [& {:as opts}]
;   (call-api "getWMS" opts nil))
;
; (defn get-hansard
;   "Return all of Hansard.
;    Options - note that (as at 16/11/2012) only one of the following may be supplied:
;
;    - :search (optional) Return data containg this term
;    - :person (optional) Return data by person ID
;    - :order (optional, in conjunction with search or person) 'd' for date ordering, 'r' for relevance ordering
;    - :page (optional, in conjunction with search or person) The page of results to return
;    - :num (optional, in conjunction with search or person) The number of results to return"
;   [& {:as opts}]
;   (call-api "getHansard" opts nil))
;
; (defn get-comments
;   "Return comments left on TheyWorkForYou.  With no arguments, returns the most recent comments in
;    reverse date order.
;    Options:
;
;    - :pid (required) Return comments made on a particular person ID (MP or Lord)
;    - :start_date (optional) Return comments made on or after this date
;    - :end_date (optional) Return comments made on or before this date
;    - :search (optional) Return comments containing this term
;    - :page (optional) The page of results to return
;    - :num (optional) The number of result to return"
;   [& {:as opts}]
;   (call-api "getComments" opts nil))
