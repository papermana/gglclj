(ns gglclj.core
  (:require [clojure.string :as string]
            [cljs.nodejs :as node]
            os
            [child_process :refer [spawn]]
            fs
            [clojure.reader :refer [read-string]]))

(node/enable-util-print!)

(def default-search-engines
  {:images "https://www.google.com/search?q=<search>%20<query>&tbm=isch"
   :youtube "https://www.youtube.com/results?search_query=\"<search>%20<query>\""
   :stackoverflow "http://stackoverflow.com/search?q=<search>+<query>"
   :ddg "https://duckduckgo.com/?q=<search>%20<query>"
   :wiki "http://www.wikipedia.org/w/index.php?search=<search>+<query>"
   :github "http://github.com/search?q=<search>+<query>"
   :google "https://www.google.com/search?q=<search>%20<query>"})

(def default-flags
  {"-i" :images, "--images" :images
   "-y" :youtube, "--youtube" :youtube
   "-s" :stackoverflow, "--stack" :stackoverflow, "--stackoverflow" :stackoverflow
   "-d" :ddg, "--ddg" :ddg, "--duckduckgo" :ddg
   "-w" :wiki, "--wiki" :wiki, "--wikipedia" :wiki
   "-g" :github, "--git" :github, "--github" :github
   "--google" :google})

(def default-engine :google)

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
        (re-find #"(https?://.*)<search>(.*)<query>(.*)" template)]
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
  (println "Usage:

gglclj [query]
gglclj --google [query]
gglclj -i/--images [query]
gglclj -y/--youtube [query]
gglclj -s/--stack/--stackoverflow [query]
gglclj -w/--wiki/--wikipedia [query]
gglclj -d/--ddg/--duckduckgo [query]
gglclj -g/--git/--github [query]

gglclj -h/--help

You can provide your own config by creating a ~/.gglcljrc file. It
should contain an EDN map which can have keys :search-engines, :flags,
and :default-engine. :search-engines is a map that takes engine
identifiers (as keywords) and matches then to search templates. :flags
is a map that takes command-line options as strings and matches them
to engine identifiers. :default-engine is a single engine identifier.

Search templates are regular URL but where the search query should go,
there should be a string \"<search><query>\". Anything that appears
between the placeholders, e.g. \"%20\" or \"+\", will be consider to
be a separator to be placed between the words in your query.

Example config:
{:search-engines {:my-search \"https://my-search.com?q=<search>%20<query>\"}
 :flags {\"-m\" :my-search}
 :default-engine :my-search}"))

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

(defn get-default-engine
  [config]
  (or (:default-engine config) default-engine))

(defn perform-search!
  [query & [flag]]
  (let [config (get-config)
        engine (if flag
                 (get-search-engine flag config)
                 (get-default-engine config))]
    (-> engine
        (get-search-template config)
        parse-search-template
        make-build-query-func
        (open query))))

(defn flag?
  [word]
  (re-find #"^--?[^-]+$" word))

(defn -main
  ([]
   (print-help!))
  ([flag-or-query]
   (if (flag? flag-or-query)
     (print-help!)
     (perform-search! (list flag-or-query))))
  ([flag-or-query & query]
   (cond (help-flag? flag-or-query) (print-help!)
         (flag? flag-or-query) (perform-search! query flag-or-query)
         :else (perform-search! (conj query flag-or-query)))))

(set! *main-cli-fn* -main)
