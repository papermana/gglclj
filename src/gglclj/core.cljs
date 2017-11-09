(ns gglclj.core
  (:require [clojure.string :as string]
            [cljs.nodejs :as node]
            os
            [child_process :refer [spawn]]))

(node/enable-util-print!)

(defn get-search-func
  [engine]
  (case engine
    :images #(str "https://www.google.com/search?q="
                  (string/join "%20" %)
                  "&tbm=isch")
    :youtube #(str "https://www.youtube.com/results?search_query=\""
                   (string/join "%20" %)
                   "\"")
    :stackoverflow #(str "http://stackoverflow.com/search?q="
                         (string/join "+" %))))

(defn get-search-engine
  [flag]
  (case flag
    ("-i" "--images") :images
    ("-y" "--youtube") :youtube
    ("-s" "--stack" "--stackoverflow") :stackoverflow))

(defn open
  [func query]
  (let [command (if (= (os/platform) "darwin")
                  "open"
                  "xdg-open")]
    (spawn command #js[(func query)])))

(defn print-help!
  []
  (println "Pass a flag and then a query"))

(defn help-flag?
  [flag]
  (or (= flag "-h")
      (= flag "--help")
      (= flag "-?")))

(defn -main
  [flag & query]
  (cond (and (not flag) (not query)) (print-help!)
        (help-flag? flag) (print-help!)
        :else (-> flag
                  get-search-engine
                  get-search-func
                  (open query))))

(set! *main-cli-fn* -main)


