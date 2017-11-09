(require '[cljs.build.api :as b])

(println "Building ...")

(let [start (System/nanoTime)]
  (b/build "src"
    {:main 'gglclj.core
     :output-to "out/gglclj.js"
     :output-dir "out"
     :verbose true
     :target :nodejs})
  (println "... done. Elapsed" (/ (- (System/nanoTime) start) 1e9) "seconds"))


