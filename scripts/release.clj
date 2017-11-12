(require '[cljs.build.api :as b])

(println "Building ...")

(let [start (System/nanoTime)]
  (b/build "src"
    {:output-to "release/gglclj.js"
     :output-dir "release"
     :optimizations :simple
     :optimize-constants true
     :static-fns true
     :pretty-print false
     :verbose true
     :target :nodejs})
  (println "... done. Elapsed" (/ (- (System/nanoTime) start) 1e9) "seconds"))
