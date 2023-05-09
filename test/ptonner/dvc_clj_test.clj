(ns ptonner.dvc-clj-test
  (:require [clojure.test :refer :all]
            [clojure.java.io :as io]
            [test-with-files.tools :refer [with-files]]
            [babashka.fs :as fs]
            [babashka.process :refer [shell process]]
            [ptonner.dvc-clj :refer :all]))

(def tmp-dir (fs/path "/" "tmp" "dvc-clj"))
(prn tmp-dir)
(fs/create-dirs tmp-dir)
(def template-dir (fs/absolutize (fs/create-temp-dir {:path tmp-dir})))
(prn template-dir)

(defn- init-template!
  "Create a initial project to use as a template for tests"
  [t]
  (fs/delete-tree template-dir)
  (fs/create-dirs template-dir)
  (let [cfg {:dir (str template-dir), :out :string, :err :string}]
    (shell cfg "git init")
    (shell cfg "dvc init"))
  (t)
  (fs/delete-tree tmp-dir))

(defn- copy-template!
  "Copy template to `dir`"
  [dir]
  (fs/delete-tree dir)
  (fs/copy-tree template-dir dir))

(use-fixtures :once init-template!)

(defn- init-project!
  "Initialize a project at location `dir` with `files` copied over"
  [dir & files]
  (copy-template! dir)
  (doseq [f files] (fs/copy f dir)))

(deftest find-root
  (testing "root found" (is (= (project-root template-dir) template-dir)))
  (testing "no root"
           (is (fs/with-temp-dir [td {:path tmp-dir}]
                                 (nil? (project-root td))))))

(deftest add-file
  (testing
   "DVC files added"
   (fs/with-temp-dir [tmp-dir {:path tmp-dir}]
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
