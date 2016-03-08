(ns twfy.core-test
  (:require [clojure.test :refer :all]
            [twfy.core :refer :all]
            [clj-time.core :as t]))

(deftest test-map2query
  (is (= "?key1=value1&key2=value2" (#'twfy.core/map2query {:key1 "value1" :key2 "value2"}))))

(deftest test-xor
  (println (#'twfy.core/xor false false))
  (is (= false (#'twfy.core/xor false false)))
  (is (= true (#'twfy.core/xor false true)))
  (is (= false (#'twfy.core/xor true true))))

(deftest test-date2string
  (is (= "2016-01-01" (#'twfy.core/date2string (java.util.Date. 116 0 1 13 45 30))))
  (is (= "2016-01-01" (#'twfy.core/date2string (java.sql.Date. 116 0 1))))
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
;  (is (thrown? AssertionError (constituencies {:search "more" :date "2016-01-01T13:45:30.678Z"}))))

(deftest test-person
  (is (= "Jeremy Corbyn" (:full_name (first (person {:id 10133})))))
  (is (thrown? AssertionError (person {}))))
