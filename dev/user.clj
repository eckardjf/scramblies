(ns user
  (:require
    [integrant.repl :refer [set-prep! go halt reset]]
    [scramblies.server]))

(set-prep! (constantly scramblies.server/system-config))
