(ns scramblies.ui.app-test
  (:require [cljs.test :as t :refer [deftest testing is]]
            [scramblies.ui.app :as app]))

(deftest set-errors-from-response-test
  (let [state {}
        path [:scramble]
        response {:s1 ["a"] :s2 ["b"]}]
    (is (= {:scramble {:server {"s1" {:errors ["a"]}
                                "s2" {:errors ["b"]}}}}
           (app/set-errors-from-response state path response)))))