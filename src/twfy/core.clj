;; ## Clojure bindings for the TheyWorkForYou API

(ns twfy.core
  (:require [clojure.string :as str]
            [cheshire.core :as ch]
            [clojure.xml :as xml]
            [clojure.zip :as zip]
            [environ.core :refer [env]]
            [clj-time.format :as f]))

(def ^{:private true} api-key
  "The \"They Work For You\" API key"
  (env :twfy-api-key))

(def ^{:private true} base-uri
  "The base URI of the \"They Work For You\" API. May be overridden by setting environment variable :twfy-base-api"
  (or (env :twfy-base-api) "http://theyworkforyou.com/api/"))

(def ^{:private true} global-defaults
  "Global default options for invoking the API."
  {:output "clj-json"})

(defn- url-encode
  "URL-encode a string"
  [s]
  (java.net.URLEncoder/encode s "UTF-8"))

(defn- encode-kv-pair
  "URL-encode a key-value pair"
  [[k v]]
  (str/join "=" (map url-encode [(name k) v])))

(defn- map2query
  "Translates a map into a URL-encoded query string."
  [m]
  (str "?" (str/join "&" (map encode-kv-pair m))))

(defn- build-uri
  "Build the URI for an API function from the function name and arguments"
  [fname args]
  (->> {:key api-key}
    (merge args)
    (map2query)
    (str base-uri fname)))

(defn- invoke-twfy
  "Invokes the \"They Work For You\" API"
  ([fname]
   (invoke-twfy fname {}))
  ([fname args]
   (-> fname
    (build-uri args)
    (slurp)
    (ch/parse-string true))))


;; ## Main API Functions

(defn convert-url
  "Converts a parliament.uk Hansard URL into a TheyWorkForYou one, if possible."
  [url]
  (invoke-twfy "convertURL" {:url url}))

(defn constituency
  "Search for a UK parliamentary constituency.  The search terms should be a map containing at least one of :name, :postcode"
  [{:keys [name postcode] :as terms}]
  {:pre [(< 0 (count terms))]}
  (invoke-twfy "getConstituency" terms))

(defn constituencies
  "Get a list of UK parliamentary constituencies. The search terms should be a map containing one of :date (a java.util.Date) or :search (a string).
   If :date is specified, a list of constituencies as at the given date is returned.
   If :search is specified, a list of constituencies matching the given search term is returned.
   At present, only one of :date, :search can be given."
  [{:keys [date search] :as terms}]
  {:pre [(= 1 (count terms))]}
  (println terms)
  (if date
    (invoke-twfy "getConstituencies" {:date (f/unparse (f/formatters :date) date)})
    (invoke-twfy "getConstituencies" {:search search})))

(defn person
  "Get details for the person with the given id."
  [id]
  (invoke-twfy "getPerson" {:id id}))

;
; (defn get-mp
;   "Return details for a particular MP.
;    Options - at least one of the following must be supplied:
;
;   - :postcode (optional)
;   - :constituency (optional) The name of a constituency.  Note that this will only return the current/most recent entry in the database.
;   - :id (optional) The person ID for the member
;   - :always_return (optional) Whether to try to return an MP even if the seat is currently vacant"
;   [& {:as opts}]
;   (call-api "getMP" opts nil))
;
; (defn get-mp-info
;   "Returns additional information for a particular person
;    Options:
;
;   - :id (required) The person ID
;   - :fields (optional) The fields required in the response, comma separated (blank for all)"
;   [& {:as opts}]
;   (call-api "getMPInfo" opts nil))
;
; (defn get-mps-info
;   "Return additional information for one or more people.
;   Options:
;
;   - :id (required) The person IDs, as a comma separated string
;   - :fields (optional) The fields required in the response, comma separated (blank for all)"
;   [& {:as opts}]
;   (call-api "getMPsInfo" opts nil))
;
; (defn get-mps
;   "Return a list of MPs
;    Options:
;
;    - :date (optional) Return the list of MPs as at this date
;    - :party (optional) Return the list of MPs from the given party
;    - :search (optional) Return the MPs whose names contain the given search string"
;   [& {:as opts}]
;   (call-api "getMPs" opts nil))
;
; (defn get-lord
;   "Return a particular lord.
;    Options:
;
;    - :id (required) The person ID of the lord"
;   [& {:as opts}]
;   (call-api "getLord" opts nil))
;
; (defn get-lords
;   "Return a list of lords.
;    Options:
;
;   - :date (optional) Return the list of lords as at this date (NB date is when the lord is introduced in Parliament)
;   - :party (optional) Return the lords from the given party
;   - :search (optional) Return the lords whose names contain the given search string"
;   [& {:as opts}]
;   (call-api "getLords" opts nil))
;
; (defn get-mla
;   "Return a particular MLA.
;    Options - at least one of the following must be supplied:
;
;   - :postcode (optional) Return the MLA for the given postcode
;   - :constituency (optional) The name of a constituency
;   - :id (optional) The person ID of the MLA"
;   [& {:as opts}]
;   (call-api "getMLA" opts nil))
;
; (defn get-mlas
;   "Return a list of MLAs.
;    Options:
;
;   - :date (optional) Return the list of MLAs as at the given date
;   - :party (optional) Return the list of MLAs from the given party
;   - :search (optional) Return the list of MLAs whose names contain the given search string"
;   [& {:as opts}]
;   (call-api "getMLAs" opts nil))
;
; (defn get-msp
;   "Return a particular MSP.
;    Options - at least one of the following must be supplied:
;
;   - :postcode (optional) Return the MSP for a particular postcode
;   - :constituency (optional) The name of a constituency
;   - :id (optional) The person ID of the MSP"
;   [& {:as opts}]
;   (call-api "getMSP" opts nil))
;
; (defn get-msps
;   "Return a list of MSPs.
;    Options:
;
;   - :date (optional) Return the list of MSPs as at the given date
;   = :party (optional) Return the list of MSPs from the given party
;   = :search (optional) Return the list of MSPs whose names contain the given search string"
;   [& {:as opts}]
;   (call-api "getMSPs" opts nil))
;
; (defn get-geometry
;   "Return geometry information for a constituency.
;    Options:
;
;   - :name (required) The name of the constituency"
;   [& {:as opts}]
;   (call-api "getGeometry" opts nil))
;
; (defn get-boundary
;   "Return the KML file for a UK Parliament constituency.
;    Options:
;
;   - :name (required) The name of the constituency"
;   [& {:as opts}]
;   (call-api "getBoundary" opts {:output "clj-xml"}))
;
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
