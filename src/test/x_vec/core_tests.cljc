(ns x-vec.core-tests
  (:require [x-vec.core :as x]
            [x-vec.hiccup-tests]
            [x-vec.graphql-tests]
            [cljs.test :refer [deftest is are testing]]))

(deftest x-vec-core


  (are [x]
    (= x (x/emit {} x))
    [:div {}]
    [:div {:a "a"}]
    [:div {:a "a"} "b" "c"])

  (is (= (x/emit {:parse-tag (fn [[tag props]] [(name tag) props])}
                 [:div {}])
         ["div" {}]))

  (are [in out]
    (is (= (x/emit {:emit-vec (fn [tag props children]
                                (str tag "-" (count props) "-" (apply str (mapv x/-emit children))))}
                   in) out))

    [:div {} [:div "a" "b"]] ":div-0-:div-0-ab"
    "a" "a")


  (are [in out]
    (is (= (x/emit {:emit-nonvec (fn [x]
                                   (str "Element(" x ")"))}
                   in) out))

    [:div {} [:div "a" 1]]
    [:div {} [:div {} "Element(a)" "Element(1)"]]

    "a" "Element(a)"

    1 "Element(1)"))

