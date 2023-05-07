(ns ptonner.dvc-clj
  (:require [babashka.process :refer [shell process]]
            [clj-yaml.core :as yaml]
            [clojure.string :as str]
            [babashka.fs :as fs]))

;; NOTE: only supporting git-based project for now
(defn git-root? [p] (fs/exists? (fs/path p ".git")))

(defn project-root
  "Find the project root of path, if it exists"
  ([] (project-root ""))
  ([f]
   (loop [p (fs/absolutize f)]
     (when p (if (git-root? p) p (recur (fs/parent p)))))))

(def read-dvc "Read a .dvc file" (comp yaml/parse-string slurp))

(comment (:outs (read-dvc "/tmp/dvc-test/abc.txt.dvc")))

(defn- f-in-dot-dvc?
  [f ddvc]
  (let [cfg (read-dvc ddvc)
        wdir ;; (fs/absolutize (fs/parent ddvc))
        (fs/path (or (:wdir cfg) "."))]
    ;; NOTE: can never succeed currently
    (some (constantly false) (:outs cfg))))

(defn dot-dvc
  [f]
  "Find the .dvc file for `f`"
  (if-let [root (project-root f)]
    (loop [p (fs/absolutize f)]
      (if (fs/directory? p) (let [ddvc (fs/match (str p) "glob:*.dvc")])))
    (throw (AssertionError. "no project root"))))


(defn dvc-out
  [f opts]
  (let [{:keys [wdir], :or {wdir (fs/path ".")}} opts
        path (fs/path f)
        relpath (fs/relativize wdir path)])
  ;; {:path}
)

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

(defn add!
  [{:keys [target targets opt]}]
  (let [targets (or targets [target])
        opt (or opt {})]
    ;; TODO: set default seder operation
    ;; TODO: load target into memory and return it
    (apply shell opt "dvc" "add" targets)
    ;; Add metadata
    ;; NOTE: this is broken b/c it doesn't track :dir option
    ;; (doseq [t targets]
    ;;   (add-dvc-meta (str t ".dvc")
    ;;                 {::read (sym->str 'slurp), ::write (sym->str 'spit)}))
  ))
