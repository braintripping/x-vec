(ns x-vec.graphql
  (:refer-clojure :exclude [munge])
  (:require [clojure.set :as set]
            [clojure.string :as string]))

(defn munge [s]
  (string/replace (if (keyword? s) (subs (str s) 1) s) #"[^a-zA-Z_0-9]+" "_"))

(defn def-form [kind name full-name value]
  `(do
     (swap! ~'x-vec.graphql/defs assoc-in [~kind ~full-name] ~value)
     (def ~name [~kind {:gql/def ~full-name}])))

(defmacro deffragment [the-name props & body]
  (let [full-name (munge (keyword (str *ns*) (name the-name)))]
    (def-form :gql/fragment the-name full-name `[~full-name (merge {:gql/kind "fragment"}
                                                                   ~(set/rename-keys props {:on :gql/on})) ~@body])))

(defmacro query [the-name arglist & body]
  (let [argmap (apply hash-map arglist)
        bindings (reduce-kv (fn [bindings name _]
                              (into bindings [name `(quote [:gql/var {:gql/name ~name}])])) [] argmap)]
    `(let ~bindings
       (~'x-vec.graphql/emit
         (into [~(name the-name)
                (assoc (quote ~argmap) :gql/kind "query")] ~(vec body))))))

(defmacro defquery
  [the-name arglist & body]
  `(def ~the-name
     (~'x-vec.graphql/query
       ~the-name
       ~arglist
       ~@body)))