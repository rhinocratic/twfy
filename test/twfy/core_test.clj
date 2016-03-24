(ns twfy.core-test
  (:require [clojure.test :refer :all]
            [twfy.core :refer :all]
            [clj-time.core :as t]))

(deftest test-map2query
  (is (= "?key1=value1&key2=value2" (#'twfy.core/map2query {:key1 "value1" :key2 "value2"}))))

(deftest test-date2string
  (is (= "2016-01-01" (#'twfy.core/date2string (java.util.Date. 116 0 1 13 45 30))))
  (is (= "2016-01-01" (#'twfy.core/date2string (java.sql.Date. 116 0 1))))
  (is (= "2016-01-01" (#'twfy.core/date2string (java.sql.Timestamp. 1451655930678))))
  (is (= "2016-01-01" (#'twfy.core/date2string 1451655930678)))
  (is (= "2016-01-01" (#'twfy.core/date2string "2016-01-01T13:45:30.678Z")))
  (is (= "2016-01-01" (#'twfy.core/date2string #inst "2016-01-01T13:45:30.678Z"))))

(deftest test-preprocess-terms
  (is (= {:search "splinge!" :date "2016-01-01" :id "456"} (#'twfy.core/preprocess-terms {:search "splinge!" :date (java.util.Date. 116 0 1 13 45 30) :id 456}))))

(deftest test-convert-url
  (let [res (convert-url {:url "http://www.publications.parliament.uk/pa/cm201314/cmhansrd/cm131106/debtext/131106-0001.htm#131106-0001.htm_spnew47"})]
    (is (.startsWith (:gid res) "uk.org.publicwhip/debate"))
    (is (.startsWith (:url res) "http://www.theyworkforyou.com"))
    (is (thrown? AssertionError (convert-url {})))))

(deftest test-constituency
  (is (= "Morecambe and Lunesdale" (:name (constituency {:postcode "LA4 4ET"}))))
  (is (= "412" (:bbc_constituency_id (constituency {:name "Morecambe and Lunesdale"})))))

(deftest test-constituencies
  (is (= ["Morecambe and Lunesdale" "Ogmore"] (map :name (constituencies {:search "more"}))))
  (is (= 650 (count (constituencies {:date "2016-01-01T13:00:00"}))))
  (is (thrown? AssertionError (constituencies {}))))

(deftest test-person
  (is (= "Jeremy Corbyn" (:full_name (first (person {:id 10133})))))
  (is (thrown? AssertionError (person {}))))

(deftest test-mp
  (is (= "Jeremy Corbyn" (:full_name (mp {:postcode "N1 4AQ"}))))
  (is (= "Jeremy Corbyn" (:full_name (mp {:constituency "Islington North"}))))
  (is (= "Jeremy Corbyn" (:full_name (first (mp {:id 10133})))))
  (is (thrown? AssertionError (mp {}))))

(deftest test-mp-info
  (is (= "Islington North" (:constituency (mp-info {:id "10133"}))))
  (is (= 1 (count (mp-info {:id 10133 :fields "comments_on_speeches_rank_outof"}))))
  (is (thrown? AssertionError (mp-info {}))))

(deftest test-mps-info
  (let [constituencies (sort (map #(:constituency (val %)) (mps-info {:id "10133,24910,10544", :fields "constituency"})))]
    (is (= ["Bolsover" "Brighton, Pavilion" "Islington North"] constituencies))
    (is (thrown? AssertionError (mps-info {})))))

(deftest test-mps
  (is (= 650 (count (mps {:date "2016-01-01"}))))
  (is (= 1 (count (mps {:date "2016-01-01" :party "green"}))))
  (is (< 0 (count (mps {:search "lucas"}))))
  (is (thrown? AssertionError (mps {}))))

(deftest test-lord
  (is (= "Baroness Andrews" (:full_name (first (lord {:id 13108})))))
  (is (thrown? AssertionError (lord {}))))

(deftest test-lords
  (is (= 868 (count (lords {:date "2016-01-01"}))))
  (is (< 0 (count (lords {:party "labour"}))))
  (is (< 0 (count (lords {:search "Andrews"}))))
  (is (thrown? AssertionError (lords {}))))

(deftest test-mla
  (is (= "Steven Agnew" (:full_name (first (mla {:id 25121})))))
  (is (< 0 (count (mla {:constituency "North Down"}))))
  (is (< 0 (count (mla {:postcode "BT18 9LL"}))))
  (is (thrown? AssertionError (mla {}))))

(deftest test-mlas
  (is (< 0 (count (mlas {:date "2016-01-01"}))))
  (is (< 0 (count (mlas {:party "green"}))))
  (is (< 0 (count (mlas {:search "Agnew"}))))
  (is (thrown? AssertionError (mlas {}))))

(deftest test-msp
  (is (= "Alison Johnstone" (:full_name (first (msp {:id 25091})))))
  (is (< 0 (count (msp {:constituency "Lothian"}))))
  (is (< 0 (count (msp {:postcode "EH39 5NN"}))))
  (is (thrown? AssertionError (msp {}))))

(deftest test-msps
  (is (< 0 (count (msps {:date "2016-01-01"}))))
  (is (< 0 (count (msps {:party "green"}))))
  (is (< 0 (count (msps {:search "Johnstone"}))))
  (is (thrown? AssertionError (msps {}))))

(deftest test-geometry
  (is (= 370195.4 (:max_e (geometry {:name "Morecambe and Lunesdale"}))))
  (is (thrown? AssertionError (geometry {}))))

(deftest test-boundary
  (is (< 0 (count (boundary {:name "Morecambe and Lunesdale"}))))
  (is (thrown? AssertionError (boundary {}))))

(deftest test-committee
  (is (= ["EU Home Affairs Sub-Committee" "Home Affairs Committee"] (sort (map :name (:committees (committee {:name "Home Affairs Committee"}))))))
  (is (thrown? AssertionError (committee {}))))

(deftest test-debates
  (is (= "11:30:00" (get-in (first (debates {:type :commons :date "2016-03-01"})) [:entry :htime])))
  (is (< 0 (get-in (debates {:type :lords :search "fish"}) [:info :total_results])))
  (is (< 0 (get-in (debates {:type :commons :person 10133}) [:info :total_results])))
  (is (= "101" (:major (second (debates {:type :lords :gid "2006-07-14a.946.0"})))))
  (is (some? (:hdate (first (:rows (debates {:type :lords :search "fish" :order :d :page 1}))))))
  (is (thrown? AssertionError (debates {})))
  (is (thrown? AssertionError (debates {:date "2016-03-01"})))
  (is (thrown? AssertionError (debates {:date "2016-03-01" :person 10133}))))

(deftest test-wrans
  (is (= "Department for Transport" (get-in (first (wrans {:date "2016-03-01"})) [:entry :body])))
  (is (< 0 (:total_results (second (first (wrans {:search "fish"}))))))
  (is (< 0 (:total_results (second (first (wrans {:person 10133}))))))
  (is (= "Department for Business, Innovation and Skills" (:body (first (wrans {:gid "2015-07-21.8285.q0"})))))
  (is (thrown? AssertionError (wrans {})))
  (is (thrown? AssertionError (wrans {:date "2016-03-01" :person 10133}))))

(deftest test-wms
  (is (= "Department for Work and Pensions" (get-in (first (wms {:date "2016-03-01"})) [:entry :body])))
  (is (< 0 (:total_results (last (first (wms {:search "fish"}))))))
  (is (= 0 (:total_results (last (first (wms {:person 10133}))))))
  (is (= "Deputy Prime Minister" (:body (first (wms {:gid "2005-10-27a.13WS.0"})))))
  (is (thrown? AssertionError (wms {})))
  (is (thrown? AssertionError (wms {:search "fish" :person 10133}))))

(deftest test-hansard
  (is (< 0 (:total_results (last (first (hansard {:search "fish"}))))))
  (is (< 0 (:total_results (last (first (hansard {:person 10133}))))))
  (is (thrown? AssertionError (hansard {})))
  (is (thrown? AssertionError (hansard {:search "fish" :person 10133}))))

(deftest test-comments
  (is (< 0 (count (:comments (comments {:start_date "2016-01-01" :end_date "2016-03-01"})))))
  (is (< 0 (count (:comments (comments {:search "fish" :num 12})))))
  (is (< 0 (count (:comments (comments {:pid 10133}))))))

(deftest test-async
  (is (< 0 (lords {:party "labour"} (fn [result] (count result)))))
  (is (< 0 (boundary {:name "Morecambe and Lunesdale"} (fn [result] (count result))))))

(deftest test-error-handling
  (is (thrown? Exception (person {:id 1054})))
  (is (thrown? Exception (boundary {:name "Morecambe und Lunesdale"}))))
