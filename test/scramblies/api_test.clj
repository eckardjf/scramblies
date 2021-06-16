(ns scramblies.api-test
  (:require [clojure.test :refer :all]
            [scramblies.api :as api]))

(deftest scramble-test

  (testing "true"
    (is (true? (api/scramble? "rekqodlw" "world")))
    (is (true? (api/scramble? "cedewaraaossoqqyt" "codewars")))
    (is (true? (api/scramble? "abc" "abc"))))

  (testing "false"
    (is (false? (api/scramble? "katas" "steak")))
    (is (false? (api/scramble? "a" "aa"))))

  (testing "invalid characters"
    (is (thrown? AssertionError (api/scramble? "REKQODLW" "WORLD")))
    (is (thrown? AssertionError (api/scramble? "44" "world")))
    (is (thrown? AssertionError (api/scramble? "rekqodlw" "&^%")))
    (is (thrown? AssertionError (api/scramble? "\t" "world"))))

  (testing "nil"
    (is (thrown? AssertionError (api/scramble? nil "abc")))
    (is (thrown? AssertionError (api/scramble? "abc" nil))))

  (testing "empty"
    (is (thrown? AssertionError (api/scramble? "" "abc")))
    (is (thrown? AssertionError (api/scramble? "abc" ""))))

  (testing "blank"
    (is (thrown? AssertionError (api/scramble? " " "abc")))
    (is (thrown? AssertionError (api/scramble? "abc" " "))))

  (testing "numbers"
    (is (thrown? AssertionError (api/scramble? 1 "abc")))
    (is (thrown? AssertionError (api/scramble? "abc" 99)))))