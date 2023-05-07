(ns ptonner.dvc-clj
  (:require [babashka.process :refer [shell process]]
            [clj-yaml.core :as yaml]
            [clojure.string :as str]
            [babashka.fs :as fs]))

(defn sym->str
  [sym]
  "Convert symbol to a string that can be resolved"
  (-> sym
      resolve
      symbol
      str))

(defn resolve-string
  [s]
  "Resolve a string representing a symbol"
  (-> s
      symbol
      resolve))

(comment yaml/parse-stream
         (-> "/tmp/dvc-test/abc.txt.dvc"
             slurp
             yaml/parse-string
             (assoc :meta {::read (str (symbol (resolve 'slurp)))})
             yaml/generate-string
             yaml/parse-string
             (get-in [:meta ::read])
             symbol
             resolve)
         (require '[clojure.string :as str])
         ((-> {:a (symbol (resolve 'str/split))}
              str
              clojure.edn/read-string
              :a
              resolve)
          "a b c"
          #" ")
         (clojure.edn/read-string (str))
         (-> (resolve 'yaml/parse-stream)
             symbol
             ;; (.. -ns)
         ))

;; (defn- to-meta-option [key val]
;;   (str "--meta " (str key) "=" val))

(defn- add-dvc-meta
  [f m]
  (spit f
        (-> f
            slurp
            yaml/parse-string
            (update :meta merge m)
            ;; NOTE: use block style to remain similar to original
            ;; style
            (yaml/generate-string :dumper-options {:flow-style :block}))))

(comment (add-dvc-meta "/tmp/dvc-test/abc.txt.dvc"
                       {::read (sym->str 'slurp), ::write (sym->str 'spit)}))

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
    ;; TODO: set default seder operation
    ;; TODO: load target into memory and return it
    (apply shell opt "dvc" "add" targets)
    ;; Add metadata
    ;; NOTE: this is broken b/c it doesn't track :dir option
    (doseq [t targets]
      (add-dvc-meta (str t ".dvc")
                    {::read (sym->str 'slurp), ::write (sym->str 'spit)}))))
