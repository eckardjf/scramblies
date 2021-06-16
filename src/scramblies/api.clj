(ns scramblies.api
  (:require [malli.core :as m]
            [malli.error :as me]
            [scramblies.specs :as specs]))

(defn scramble?
  "returns true if a subset of characters of s1 can be rearranged to match s2, otherwise returns false"
  [s1 s2]
  {:pre [(m/validate specs/ScrambleString s1)
         (m/validate specs/ScrambleString s2)]}
  (loop [fm (frequencies s1)
         cs s2]
    (if (empty? cs)
      true
      (let [c (first cs)]
        (if (zero? (get fm c 0))
          false
          (recur (update fm c dec) (rest cs)))))))

(comment
  (scramble? "rekqodlw" "world")
  (scramble? "cedewaraaossoqqyt" "codewars")
  (scramble? "katas" "steak")
  )