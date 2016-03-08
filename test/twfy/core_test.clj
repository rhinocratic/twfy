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

(deftest test-convert-url
  (let [res (convert-url {:url "http://www.publications.parliament.uk/pa/cm201314/cmhansrd/cm131106/debtext/131106-0001.htm#131106-0001.htm_spnew47"})]
    (is (.startsWith (:gid res) "uk.org.publicwhip/debate"))
    (is (.startsWith (:url res) "http://www.theyworkforyou.com"))))

(deftest test-constituency
  (is (= "Morecambe and Lunesdale" (:name (constituency {:postcode "LA4 4ET"}))))
  (is (= "412" (:bbc_constituency_id (constituency {:name "Morecambe and Lunesdale"})))))

(deftest test-constituencies
  (is (= ["Morecambe and Lunesdale" "Ogmore"] (map :name (constituencies {:search "more"}))))
  (is (= 650 (count (constituencies {:date "2016-01-01T13:00:00"}))))
  (is (thrown? AssertionError (constituencies {}))))

(deftest test-person
  (is (= "Jeremy Corbyn" (:full_name (first (person {:id "10133"})))))
  (is (thrown? AssertionError (person {}))))

(deftest test-mp
  (is (= "Jeremy Corbyn" (:full_name (mp {:postcode "N1 4AQ"}))))
  (is (= "Jeremy Corbyn" (:full_name (mp {:constituency "Islington North"}))))
  (is (= "Jeremy Corbyn" (:full_name (first (mp {:id "10133"})))))
  (is (thrown? AssertionError (mp {}))))

(deftest test-mp-info
  (is (= "Islington North" (:constituency (mp-info {:id "10133"}))))
  (is (= 1 (count (mp-info {:id "10133" :fields "comments_on_speeches_rank_outof"}))))
  (is (thrown? AssertionError (mp-info {}))))

(deftest test-mps-info
  (let [constituencies (sort (map #(:constituency (val %)) (mps-info {:id "10133,24910,10544", :fields "constituency"})))]
    (is (= ["Bolsover" "Brighton, Pavilion" "Islington North"] constituencies))))
