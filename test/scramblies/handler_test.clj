(ns scramblies.handler-test
  (:require [clojure.test :refer :all]
            [integrant.core :as ig]
            [ring.mock.request :as mock]
            [ring.util.http-status :as status]
            [jsonista.core :as j]))

(def handler (atom nil))

(defn handler-fixture [tests]
  (reset! handler (ig/init-key :scramblies/handler nil))
  (tests)
  (reset! handler nil))

(use-fixtures :once handler-fixture)

(defn read-json [json]
  (j/read-value json j/keyword-keys-object-mapper))

(deftest valid-request
  (let [response (@handler (mock/request :get "/scramble" {:s1 "aaa" :s2 "a"}))
        body (-> response :body read-json)]
    (is (= status/ok (:status response))
        "status set correctly")
    (is (true? (:result body))
        "result is true for valid scramble"))

  (let [response (@handler (mock/request :get "/scramble" {:s1 "aaa" :s2 "bbb"}))
        body (-> response :body read-json)]
    (is (= status/ok (:status response))
        "status set correctly")
    (is (false? (:result body))
        "result is false for invalid scramble")))

(deftest invalid-request
  (let [response (@handler (mock/request :get "/scramble" {}))
        body (-> response :body read-json)]
    (is (= status/bad-request (:status response))
        "status set correctly")
    (is (= #{:s1 :s2} (set (keys body)))
        "response contains error messages for both missing parameters"))

  (let [response (@handler (mock/request :get "/scramble" {:s1 "abc"}))
        body (-> response :body read-json)]
    (is (= status/bad-request (:status response))
        "status set correctly")
    (is (= #{:s2} (set (keys body)))
        "response contains an error message for missing s2 parameter"))

  (let [response (@handler (mock/request :get "/scramble" {:s2 "abc"}))
        body (-> response :body read-json)]
    (is (= status/bad-request (:status response))
        "status set correctly")
    (is (= #{:s1} (set (keys body)))
        "response contains an error message for missing s1 parameter")))

(deftest not-found
  (let [response (@handler (mock/request :get "/not-found" {:s1 "aaa" :s2 "bbb"}))]
    (is (= status/not-found (:status response))
        "status set correctly")))

(deftest cors
  (let [response (@handler (-> (mock/request :options "/scramble")
                               (mock/header "Access-Control-Request-Method" "GET")
                               (mock/header "Access-Control-Request-Headers" "origin, x-requested-with")
                               (mock/header "Origin" "http://localhost")))]
    (is (= status/ok (:status response))
        "status set correctly")
    (is (contains? (:headers response) "access-control-allow-methods")
        "allow-methods header is returned")
    (is (contains? (:headers response) "access-control-allow-origin")
        "allow-origin header is returned")))