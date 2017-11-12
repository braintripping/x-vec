(ns x-vec.hiccup-tests
  (:require [x-vec.core :as x]
            [x-vec.hiccup :as h]
            [cljs.test :refer [deftest is are testing]]))

(defn element-args [form]
  (let [[props children] (x/parse-args form)]
    (-> (h/parse-tag [(form 0) props])
        (update 1 #(js->clj % :keywordize-keys true)))))

(deftest hiccup
  (testing "parse props"
    (is (= (element-args [:h1#page-header])
           ["h1" {:id "page-header"}])
        "Parse ID from element tag")

    (is (= ["div" {:className "red"}]
           (element-args [:div.red])
           (element-args [:div {:class "red"}])
           (element-args [:div {:classes ["red"]}]))
        "Three ways to specify a class")

    (is (= ["div" {:className "red"}]
           (element-args [:div.red nil]))
        "Three ways to specify a class")

    (is (= (element-args [:.red {:class   "white black"
                                 :classes ["purple"]}])
           ["div" {:className "red white black purple"}])
        "Combine classes from element tag, :class, and :classes")






    (is (= (element-args [:.red])
           ["div" {:className "red"}])
        "If tag name is not specified, use a `div`")

    (is (= (element-args [:div {:data-collapse true
                                :aria-label    "hello"}])
           ["div" {:data-collapse true
                   :aria-label    "hello"}])
        "Do not camelCase data- and aria- attributes")

    (comment

      ;; LIMITATION
      ;; React now passes through custom attributes, but
      ;; still requires camelCase for 'known' attributes.
      ;; So we can't auto-camelCase without maintaining a
      ;; whitelist of 'known' attributes.
      ;;

      (is (= (element-args [:div {:some-attr true
                                  :someAttr  "hello"}])
             ["div" {:some-attr true
                     :someAttr  "hello"}])
          "Do not camelCase custom attributes"))

    (is (= (element-args [:div {:style {:font-family "serif"
                                        :custom-attr "x"}}])
           ["div" {:style {:fontFamily "serif"
                           :customAttr "x"}}])
        "camelCase ALL style attributes")

    (is (= (element-args [:custom-element])
           ["custom-element" {}])
        "Custom element tag")

    (is (= (element-args [:custom-element/special])
           ["custom-element:special" {}])
        "Custom element tag with namespace")

    (is (= (element-args [:special/effect#el.pink {:data-collapse true
                                                   :aria-label    "hello"
                                                   :class         "bg-black"
                                                   :classes       ["white"]
                                                   :style         {:font-family "serif"
                                                                   :font-size   12}}])
           ["special:effect" {:data-collapse true
                              :aria-label    "hello"
                              :className     "pink bg-black white"
                              :style         {:fontFamily "serif"
                                              :fontSize   12}
                              :id            "el"}])
        "All together")))