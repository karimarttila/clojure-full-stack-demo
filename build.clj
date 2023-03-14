(ns build
  (:refer-clojure :exclude [test])
  (:require [clojure.tools.build.api :as b]))

(def lib 'karimarttila/webstore)
(def main 'backend.core)
(def class-dir "target/classes")

(defn- uber-opts [opts]
  (assoc opts
         :lib lib 
         :main main
         :uber-file (format "target/%s-standalone.jar" lib)
         :basis (b/create-basis {:project "deps.edn"
                                 :aliases [:common :backend :frontend]
                                 }
                                )
         :class-dir class-dir
         :src-dirs ["src/clj" "src/cljc"]
         :ns-compile [main]))

(defn uber [opts]
  (println "Cleaning...")
  (b/delete {:path "target"})
  (let [opts (uber-opts opts)]
    (println "Copying files...")
    (b/copy-dir {:src-dirs   ["resources" "prod-resources" "src/clj" "src/cljc"]
                 :target-dir class-dir})
    (println "Compiling files...")
    (b/compile-clj opts)
    (println "Creating uberjar...")
    (b/uber opts)))
