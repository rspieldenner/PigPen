(ns pigpen.runtime-test
  (:require [clojure.test :refer :all]
            [pigpen.runtime :refer :all]))

(deftest test-map->bind
  (let [f (map->bind +)]
    (is (= (f [1 2 3]) [[6]]))
    (is (= (f [2 4 6]) [[12]]))))

(deftest test-mapcat->bind
  (let [f (mapcat->bind identity)]
    (is (= (f [[1 2 3]]) [[1] [2] [3]]))
    (is (= (f [[2 4 6]]) [[2] [4] [6]]))))

(deftest test-filter->bind
  (let [f (filter->bind even?)]
    (is (= (f [1]) []))
    (is (= (f [2]) [[2]]))
    (is (= (f [3]) []))
    (is (= (f [4]) [[4]]))))

(deftest test-key-selector->bind
  (let [f (key-selector->bind first)]
    (is (= (f [[1 2 3]]) [[1 [1 2 3]]]))
    (is (= (f [[2 4 6]]) [[2 [2 4 6]]])))
  (let [f (key-selector->bind :foo)]
    (is (= (f [{:foo 1, :bar 2}]) [[1 {:foo 1, :bar 2}]]))))

(deftest test-keyword-field-selector->bind
  (let [f (keyword-field-selector->bind [:foo :bar :baz])]
    (is (= (f [{:foo 1, :bar 2, :baz 3}]) [[1 2 3]]))))

(deftest test-indexed-field-selector->bind
  (let [f (indexed-field-selector->bind 2 clojure.string/join)]
    (is (= (f [[1 2 3 4]]) [[1 2 "34"]]))))

(deftest test-eval-string
  (let [f-str "(fn [x] (* x x))"
        f (eval-string f-str)]
    (is (= (f 2) 4))
    (is (= (f 42) 1764))))