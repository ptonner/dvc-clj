(ns ptonner.dvc-clj
  (:require [babashka.process :refer [shell process]]))

;; Use cases:
;; - add an existing file (optionally load data?)
;; - add an in-memory thing
;;   - default nippy serialize, with option to override
;;   - additional (optional) argument for save location
;; - add an existing directory
;; - add a collection of things

(defn add
  [{:keys [target targets opt]}]
  (let [targets (or targets [target])
        opt (or opt {})]
    (apply shell opt "dvc" "add" targets)))
