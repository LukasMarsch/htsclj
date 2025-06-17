(ns build
  (:require [clojure.tools.build.api :as b]))

(def lib 'clojure/htsclj)
(def version (format "0.0.%s" (b/git-count-revs nil)))
(def class-dir "target/classes")
(def uber-file (format "target/%s-%s-standalone.jar" (name lib) version))
(def basis (delay (b/create-basis {:project "deps.edn"})))

(defn clean [_]
  (b/delete {:path "target"}))

(defn uber [_]
  (clean nil)
  (b/write-pom {
    :class-dir class-dir
    :lib lib
    :version version
    :basis @basis
    :src-dirs ["src"]
    })
  (b/copy-dir {:src-dirs ["src" "resources"]
               :target-dir class-dir})
  (b/compile-clj {:basis @basis
                  :ns-compile '[htsclj.server]
                  :class-dir class-dir})
  (b/uber {:class-dir class-dir
           :uber-file uber-file
           :basis @basis
           :main 'htsclj.server.main}))
