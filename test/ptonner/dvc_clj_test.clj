(ns ptonner.dvc-clj-test
  (:require [clojure.test :refer :all]
            [clojure.java.io :as io]
            [test-with-files.tools :refer [with-files]]
            [babashka.fs :as fs]
            [babashka.process :refer [shell process]]
            [ptonner.dvc-clj :refer :all]))

(defn- init-project
  [dir & files]
  (shell {:dir (str dir)} "git init")
  (shell {:dir (str dir)} "dvc init")
  (doseq [f files] (fs/copy f dir)))

(deftest add-file
  (testing
   "DVC files added"
   (fs/with-temp-dir [tmp-dir nil]
                     (init-project tmp-dir (io/resource "abc.txt"))
                     (add {:opt {:dir (str tmp-dir)}, :target "abc.txt"})
                     (is (fs/exists? (fs/path tmp-dir "abc.txt.dvc"))))))
