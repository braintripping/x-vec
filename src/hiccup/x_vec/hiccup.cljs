(ns x-vec.hiccup
  (:require [x-vec.core :as x]
            ["react" :as react]
            [clojure.string :as string]))

;; patch IPrintWithWriter to print javascript symbols without throwing errors
(when (exists? js/Symbol)
  (extend-protocol IPrintWithWriter
    js/Symbol
    (-pr-writer [sym writer _]
      (-write writer (str "\"" (.toString sym) "\"")))))

(defn return-list [form]
  (reduce (fn [out el]
            (doto ^js out (.push (x/-emit el)))) #js [] form))

(defn ^string camelCase [s]
  (string/replace s #"-([a-z])" (fn [[_ s]] (string/upper-case s))))

(defn ^boolean camelCase?
  "CamelCase by default, only exceptions are data- and aria- attributes."
  [attr-name]
  (not (re-find #"^(?:data\-|aria\-)" attr-name)))

(defn key->react-attr
  "CamelCase react keys, except for aria- and data- attributes"
  [k]
  (if (keyword-identical? k :for)
    "htmlFor"
    (let [k-str (name k)]
      (cond-> k-str
              (camelCase? k-str) (camelCase)))))

(defn map->js
  "Return javascript object with camelCase keys. Not recursive."
  [style]
  (let [style-js (js-obj)]
    (doseq [[k v] style]
      (aset style-js (camelCase (name k)) v))
    style-js))

(defn concat-classes
  "Build className from keyword classes, :class and :classes."
  [^js/String k-classes ^js/String class classes]
  (->> (cond-> []
               k-classes (conj k-classes)
               class (conj class)
               classes (into classes))
       (string/join " ")))

(def ^:dynamic *wrap-props* identity)

(defn props->js
  "Returns a React-conformant javascript object. An alternative to clj->js,
  allowing for key renaming without an extra loop through every prop map."
  [props tag k-id k-classes]
  (when (or props k-id k-classes)
    (let [{:keys [class class-name classes] :as props} props
          prop-js (cond-> (js-obj)
                          k-id (doto (aset "id" k-id))
                          (or k-classes class class-name classes) (doto (aset "className" (concat-classes k-classes (or class class-name) classes))))]
      (doseq [[k v] props]
        (cond
          ;; convert :style and :dangerouslySetInnerHTML to js objects
          (or (keyword-identical? k :style)
              (keyword-identical? k :dangerouslySetInnerHTML))
          (aset prop-js (name k) (map->js v))
          ;; ignore className-related keys
          (or (keyword-identical? k :classes)
              (keyword-identical? k :class)) nil
          ;; passthrough all other values
          :else (aset prop-js (key->react-attr k) v)))
      prop-js)))

(defn parse-tag
  "Parses a hiccup key like :div#id.class1.class2 to return the tag name, id, and classes.
   If tag-name is ommitted, defaults to 'div'. Class names are padded with spaces.

   Returns [whole-tag element-name id classes]"
  [[key props]]
  (let [[key tag id classes] (-> (re-find #":([^#.]*)(?:#([^.]+))?(.*)?" (str key))
                                 (update 1 #(if (= "" %) "div" (string/replace % "/" ":")))
                                 (update 3 #(when %
                                              (string/replace (subs % 1) "." " "))))]

    [tag (props->js (*wrap-props* props) tag id classes)]))

(comment
  (assert (= (update (parse-tag [:div.one {}]) 1 js->clj)
             (update (parse-tag [:.one {}]) 1 js->clj)
             ["div" {"className" "one"}]))
  (assert (= (update (parse-tag [:div#a.one {}]) 1 js->clj)
             ["div" {"id"        "a"
                     "className" "one"}])))

;; parse-key is an ideal target for memoization, because keyword forms are
;; frequently reused (eg. in lists) and almost never generated dynamically.
(def parse-tag-memoized (memoize parse-tag))

(defn element [form]
  (x/emit {:parse-tag parse-tag-memoized
           :emit-list return-list
           :emit-vec  react/createElement} form))