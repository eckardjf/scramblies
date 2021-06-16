(ns scramblies.specs
  (:require [malli.core]))

(def ScrambleString
  [:and
   :string
   [:re {:error/message "should contain only the lowercase letters a-z"}
    #"^[a-z]+$"]])