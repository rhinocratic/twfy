(ns twfy.core-test
  (:require [clojure.test :refer :all]
            [twfy.core :refer :all]))

(deftest test-url-encode
  (is (= "%21%23%24%26%27%28%29" (#'twfy.core/url-encode "!#$&'()"))))

(deftest test-encode-kv-pair
  (is (= "key=value" (#'twfy.core/encode-kv-pair [:key "value"]))))

(deftest test-map2query
  (is (= "?key1=value1&key2=value2" (#'twfy.core/map2query {:key1 "value1" :key2 "value2"}))))

(deftest test-call-twfy
   (is (= 1 (count (#'twfy.core/invoke-twfy "getMPs" {:party "green"})))))

(deftest test-convert-url
  (let [res (convert-url "http://www.publications.parliament.uk/pa/cm201314/cmhansrd/cm131106/debtext/131106-0001.htm#131106-0001.htm_spnew47")]
    (is (.startsWith (:gid res) "uk.org.publicwhip/debate"))
    (is (.startsWith (:url res) "http://www.theyworkforyou.com"))))
