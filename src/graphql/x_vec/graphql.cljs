(ns x-vec.graphql
  (:refer-clojure :exclude [munge])
  (:require [x-vec.core :as x]
            [clojure.string :as string]
            [clojure.set :as set])
  (:require-macros [x-vec.graphql :as g :refer [defquery query]]))

(defonce defs (atom {}))

(def ^:dynamic *query-deps*
  "Track fragments for a query."
  nil)

(def spacer "  ")

(defn depth-spacing
  ([] (depth-spacing 0))
  ([n]
   (apply str (take (+ x/*depth* n) (repeat spacer)))))

(declare emit-vec)

(defn emit-value [x]
  (cond (string? x) (str \" x \")
        (or (keyword? x) (symbol? x)) (name x)
        (boolean? x) (str x)
        (number? x) x
        (nil? x) "null"
        (vector? x) (if (= :gql/var (first x))
                      ;; special case for variables
                      (apply emit-vec x)
                      (str "["
                           (string/join ", " (mapv emit-value x))
                           "]"))
        (map? x)
        (when-let [fields (seq (keep (fn [[key value]]
                                       (when-not (and (keyword? key) (= "gql" (namespace key)))
                                         (str (name key) ": " (emit-value value)))) x))]
          (str "(" (string/join ", " fields) ")"))))

(defn emit-children [children]
  (when (seq children)
    (str " {"
         (string/join
           (let [spacing (depth-spacing)]
             (for [child children]
               (str \newline spacing child))))
         (str \newline (depth-spacing -1) "}"))))

(comment
  (assert (= (emit-value {:a 1 :b 2}) "(a: 1, b: 2)")))

(defmulti emit-vec identity)

(defmethod emit-vec :gql/fragment
  [_ {fragment-name :gql/def} children]
  ;; emitting a fragment adds it to the set that we'll include with the query
  (swap! *query-deps* update :gql/fragment conj fragment-name)
  (when-not (contains? (:gql/fragment @defs) fragment-name)
    (throw (js/Error. (str "Fragment not found: " fragment-name))))
  (str "..." fragment-name))

(defmethod emit-vec :gql/var
  [_ {var-name :gql/name}]
  (let [var-name (name var-name)]
    ;(assert (string/starts-with? var-name \$))
    (swap! *query-deps* update :gql/var conj var-name)
    var-name))

(defmethod emit-vec :...
  [_ props children]
  ((get-method emit-vec :default)
    :... (set/rename-keys props {:on :gql/on}) children))

(defn vec-wrap [x]
  (cond (keyword? x) [x {}]
        (and (vector? x) (not (map? (second x))))
        (into [(first x) {}] (rest x))
        :else x))

(comment
  (= (vec-wrap :x) [:x {}])
  (= (vec-wrap [:x]) [:x {}])
  (= (vec-wrap [:x :y]) [:x {} :y]))

(defmethod emit-vec :default
  [tag {:keys [gql/alias-of gql/operation gql/on gql/directives] :as props} children]
  (str (when operation (str (name operation) " "))
       (name tag)
       (when on (str " on " (name on)))
       (apply str (for [[directive directive-props] directives]
                    (str " @" (name directive) (emit-value directive-props))))
       (when alias-of
         (str ": " (name alias-of)))
       (emit-value props)
       (emit-children children)))

(defn emit-nonvec [x]
  (cond (string? x) x
        (keyword? x) (name x)
        (number? x) (str x)
        :else (do
                (prn :nonvec-emit-error x)
                (throw (js/Error. (str "Not a keyword or number: " x ", " (type x)))))))

(defn emit*
  ([form]
   (emit* 0 form))
  ([depth form]
   (x/emit {:emit-vec    emit-vec
            :parse-tag   identity
            :emit-list   identity
            :emit-nonvec emit-nonvec} form)))

(defrecord Query [query
                  fragments
                  query-string
                  fragments-string
                  string
                  required-variables]
  Object
  (toString [this] string))

(def emit
  (memoize (fn [form]
             (binding [*query-deps* (or *query-deps*
                                        (atom {:gql/fragment #{}
                                               :gql/var      #{}}))]
               (let [query-string (emit* form)
                     {variables :gql/var
                      fragments :gql/fragment} @*query-deps*
                     defs @defs
                     fragments (mapv (fn [name] (get-in defs [:gql/fragment name])) fragments)
                     fragments-string (->> (for [fragment fragments]
                                             (str "\n\n" (emit* fragment)))
                                           (apply str))
                     whole-string (str query-string fragments-string)]
                 (->Query form fragments query-string fragments-string whole-string variables))))))


(defn directive [name props body]
  (update (vec-wrap body) 1 assoc-in [:gql/directives name] props))

(comment
  (println (:query (query someQuery [$el :String]
                          [:me
                           (directive :include {:if $el}
                                      [:... :name])]))))

;; TODO
;;
;; - test mutations
;; - test input data types (with a ! for non-nullable)
;; - `__typename` http://graphql.org/learn/queries/
;; - array notation http://graphql.org/learn/schema/#object-types-and-fields
