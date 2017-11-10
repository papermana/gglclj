(ns gglclj.core
  (:require [clojure.string :as string]
            [cljs.nodejs :as node]
            os
            [child_process :refer [spawn]]
            fs
            [clojure.reader :refer [read-string]]))

(node/enable-util-print!)

(def default-search-engines
  {:images "https://www.google.com/search?q=,,search,,%20,,query,,&tbm=isch"
   :youtube "https://www.youtube.com/results?search_query=\",,search,,%20,,query,,\""
   :stackoverflow "http://stackoverflow.com/search?q=,,search,,+,,query,,"})

(def default-flags
  {"-i" :images, "--images" :images
   "-y" :youtube, "--youtube" :youtube
   "-s" :stackoverflow, "--stack" :stackoverflow, "--stackoverflow" :stackoverflow})

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
  [engine config]
  (let [engines (merge default-search-engines (:search-engines config))]
    (engines engine)))

(defn get-search-engine
  [flag config]
  (let [flags (merge default-flags (:flags config))]
    (flags flag)))

(defn print-help!
  []
  (println "Pass a flag and then a query"))

(defn help-flag?
  [flag]
  (or (= flag "-h")
      (= flag "--help")
      (= flag "-?")))

(defn get-config-path
  []
  (str (os/homedir) "/.gglcljrc"))

(defn get-config
  []
  (let [path (get-config-path)]
    (if (fs/existsSync path)
      (-> path
          (fs/readFileSync "utf-8")
          read-string))))

(defn perform-search!
  [flag query]
  (let [config (get-config)]
    (-> flag
        (get-search-engine config)
        (get-search-template config)
        parse-search-template
        make-build-query-func
        (open query))))

(defn -main
  [flag & query]
  (cond (and (not flag) (not query)) (print-help!)
        (help-flag? flag) (print-help!)
        :else (perform-search! flag query)))

(set! *main-cli-fn* -main)
