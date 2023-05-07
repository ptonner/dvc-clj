(ns ptonner.dvc-clj-test
  (:require [clojure.test :refer :all]
            [clojure.java.io :as io]
            [test-with-files.tools :refer [with-files]]
            [babashka.fs :as fs]
            [babashka.process :refer [shell process]]
            [ptonner.dvc-clj :refer :all]))

(defn- init-project!
  [dir & files]
  ;; start from empty directoy
  (fs/delete-tree dir)
  (fs/create-dirs dir)
  (shell {:dir (str dir)} "git init")
  (shell {:dir (str dir)} "dvc init")
  (doseq [f files] (fs/copy f dir)))

(deftest add-file
  (testing
   "DVC files added"
   (fs/with-temp-dir [tmp-dir nil]
                     (init-project! tmp-dir (io/resource "abc.txt"))
                     (add {:opt {:dir (str tmp-dir), :out :string, :err :out},
                           :target "abc.txt"})
                     (is (fs/exists? (fs/path tmp-dir "abc.txt.dvc"))))))

(comment (def tdir "/tmp/dvc-testing")
         (init-project! tdir (io/resource "abc.txt"))
         (-> (add {:opt {:dir tdir, :out :string, :err :string, :continue true},
                   :target "abc.txt"})
             :err))
