(ns daycare.activities.counting-test
  (:require
   [clojure.test :refer [deftest is testing]]
   [clojure.test.check.clojure-test :refer [defspec]]
   [clojure.test.check.generators :as gen]
   [clojure.test.check.properties :as prop]
   [daycare.activities.counting :as counting]))

;; A "count the objects" round: show N things, offer a few numerals, one correct.
;;
;; Everything here is a pure function of (difficulty, seed). No React, no
;; storage, no device. That is what keeps this test suite fast enough to leave
;; running while we work.

(deftest difficulty-bounds
  (testing "difficulty 1 stays in the range a two-year-old can subitize"
    (let [{:keys [target]} (counting/generate 1 0)]
      (is (<= 1 target 3))))

  (testing "difficulty 5 reaches into the teens"
    (let [{:keys [target]} (counting/generate 5 0)]
      (is (<= 1 target 20)))))

(deftest deterministic-for-a-given-seed
  (testing "same seed produces the same round, so a failure is reproducible"
    (is (= (counting/generate 3 42) (counting/generate 3 42))))

  (testing "different seeds vary the round"
    (is (not= (map :target (map #(counting/generate 3 %) (range 20)))
              (repeat 20 (:target (counting/generate 3 0)))))))

(defspec round-is-always-answerable 200
  (prop/for-all [difficulty (gen/choose 1 5)
                 seed       gen/nat]
                (let [{:keys [target choices]} (counting/generate difficulty seed)]
                  (and ;; there is exactly one right answer
                   (= 1 (count (filter #(= target %) choices)))
           ;; no duplicate numerals to confuse a child
                   (apply distinct? choices)
           ;; never ask for zero objects, and never offer a negative numeral
                   (pos? target)
                   (every? pos? choices)
           ;; few enough buttons to fit a tablet row and a small attention span
                   (<= 2 (count choices) 4)))))

(defspec harder-is-not-easier 100
  (prop/for-all [seed gen/nat]
                (<= (:target (counting/generate 1 seed))
                    (:target (counting/generate 5 seed)))))
