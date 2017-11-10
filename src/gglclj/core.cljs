(ns gglclj.core
  (:require [clojure.string :as string]
            [cljs.nodejs :as node]
            os
            [child_process :refer [spawn]]))

(node/enable-util-print!)

(defn open
  [func query]
  (let [command (if (= (os/platform) "darwin")
                  "open"
                  "xdg-open")]
    ;; spawn must be passed a JS array of arguments, not a vector
    (spawn command #js[(func query)])))

(defn make-build-query-func
  [{:keys [prefix separator postfix]}]
  (fn [query]
    (str prefix
         (string/join separator query)
         postfix)))

(defn parse-search-template
  [template]
  (let [[_ prefix separator postfix]
        (re-find #"(https?://.*),,search,,(.*),,query,,(.*)" template)]
    {:prefix prefix, :separator separator, :postfix postfix}))

(defn get-search-template
  [engine]
  (case engine
    :images "https://www.google.com/search?q=,,search,,%20,,query,,&tbm=isch"
    :youtube "https://www.youtube.com/results?search_query=\",,search,,%20,,query,,\""
    :stackoverflow "http://stackoverflow.com/search?q=,,search,,+,,query,,"))

(defn get-search-engine
  [flag]
  (case flag
    ("-i" "--images") :images
    ("-y" "--youtube") :youtube
    ("-s" "--stack" "--stackoverflow") :stackoverflow))

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
                  get-search-template
                  parse-search-template
                  make-build-query-func
                  (open query))))

(set! *main-cli-fn* -main)
