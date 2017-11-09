(require '[cljs.build.api :as b])

(b/watch "src"
  {:main 'gglclj.core
   :output-to "out/gglclj.js"
   :output-dir "out"
   :target :nodejs})
