(ns ptonner.dvc-clj-test
  (:require [clojure.test :refer :all]
            [clojure.java.io :as io]
            [test-with-files.tools :refer [with-files]]
            [babashka.fs :as fs]
            [babashka.process :refer [shell process]]
            [ptonner.dvc-clj :refer :all]))

(def dvc-clj-test-template-dir (fs/path "/" "tmp" "dvc-clj-test-template"))

(defn- init-template!
  "Create a initial project to use as a template for tests"
  [t]
  (fs/delete-tree dvc-clj-test-template-dir)
  (fs/create-dirs dvc-clj-test-template-dir)
  (shell {:dir (str dvc-clj-test-template-dir)} "git init")
  (shell {:dir (str dvc-clj-test-template-dir)} "dvc init")
  (t)
  (fs/delete-tree dvc-clj-test-template-dir))

(defn- copy-template!
  "Copy template to `dir`"
  [dir]
  (fs/delete-tree dir)
  (fs/copy-tree dvc-clj-test-template-dir dir))

(use-fixtures :once init-template!)

(defn- init-project!
  "Initialize a project at location `dir` with `files` copied over"
  [dir & files]
  ;; start from empty directoy
  (fs/delete-tree dir)
  (fs/create-dirs dir)
  (shell {:dir (str dir)} "git init")
  (shell {:dir (str dir)} "dvc init")
  (doseq [f files] (fs/copy f dir)))

(deftest find-root
  (testing "root found"
           (is (= (project-root dvc-clj-test-template-dir)
                  dvc-clj-test-template-dir)))
  (testing "no root" (is (fs/with-temp-dir [td {}] (nil? (project-root td))))))

(deftest add-file
  (testing
   "DVC files added"
   (fs/with-temp-dir [tmp-dir nil]
                     (init-project! tmp-dir (io/resource "abc.txt"))
                     (add! {:opt {:dir (str tmp-dir), :out :string, :err :out},
                            :target "abc.txt"})
                     (is (fs/exists? (fs/path tmp-dir "abc.txt.dvc"))))))

(comment (def tdir "/tmp/dvc-testing")
         (init-template!)
         (copy-template! "/tmp/dvc-test1")
         (init-project! tdir (io/resource "abc.txt"))
         (-> (add {:opt {:dir tdir, :out :string, :err :string, :continue true},
                   :target "abc.txt"})
             :err))
