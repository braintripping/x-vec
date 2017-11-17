(ns x-vec.graphql-doc
  (:require [x-vec.graphql :as g :include-macros true]
            [x-vec.hiccup :as hiccup]
            ["react" :as react]
            ["react-dom" :as react-dom]
            [cljs.js :as cljs]
            [shadow.cljs.bootstrap.browser :as boot]
            [cljs.pprint :as pprint]
            [clojure.string :as string]))

;; npx shadow-cljs watch doc-bootstrap doc
;# X-Vec: GraphQL
;
;Simple query:
;
;```clj
;[:user :name]
;```
;
;    With parameter:
;
;
;```clj
;[:user {:id 4} :name]
;```
;
;    Multiple fields:
;
;```clj
;[:user {:id 4} :id :name]
;```
;
;
;    ```clj
;[:user {:id 4} :id :name]
;```

(defonce c-state (cljs/empty-state))
(defonce eval-ready? false)

(def opts {:load    (partial boot/load c-state)
           :eval    cljs/js-eval
           :context :expr})

(defn eval [form]
  (let [result (atom nil)]
    (cljs/eval-str c-state
                   (str form)
                   "eval-example"
                   opts (fn [{:keys [error value]}]
                          (if error (prn error)
                                    (reset! result value))))
    @result))

(defn highlight-region [s]
  (if-not (.indexOf s "•")
    s
    (let [[before middle after] (string/split s #"•")]
      [:span
       before
       [:span {:style {:background "rgba(0,182,255,0.1)"}} middle]
       after])))

(defn h3 [s]
  [:h3 {:key (hash s)} s])

(defn p [s]
  [:p {:key (hash s)} s])

(defn el [kind s]
  [kind {:key (hash s)} s])

(defn eg [source]
  [:div.monospace
   {:key   (hash source)
    :style {:white-space "pre"
            :margin      "20px 0"
            :background  "rgba(0,0,0,0.05)"}}
   [:div
    {:key   1
     :style {:padding    20
             :background "rgba(0,0,0,0.05)"}}
    (-> source
        (string/replace #"\n            " "\n")
        (highlight-region))]
   (when-let [evaled (:string (eval (string/replace source "•" "")))]
     [:div
      {:key   2
       :style {:padding 20}}
      evaled])])

(defn examples []
  (hiccup/element
    [:div
     {:style {:padding 20}}
     (if-not eval-ready?
       "Loading..."
       [:div
        {:div 3}

        (el :h3 "Query")

        (eg "(•g/query MyQuery• []
              [:user {:id 4}
                     :name])")

        (el :h3 "Query with variables")

        (eg "(g/query MyQuery •[$userId :String]•
              [:user {:id $userId}
                      :name])")

        (el :h3 "Aliases")

        (eg "(g/emit [:zuck {•:gql/alias-of :user•
                            :id 4}
                        :id :name])")

        (el :h3 "Fragment - define")

        (eg "(•g/deffragment• friend-fields {:on :User}
              :name :id)")

        (el :h3 "Fragment - use as ordinary Clojure vars (namespaced)")

        (eg "(g/query withFragment []
               [:user {:id 3}
                 [:friends {:first 10}
                   •friend-fields•]])")

        (el :h3 "Fragment - inline, with directive")

        (eg "(g/defquery inlineFragmentNoType [$expandedInfo :Boolean]
      [:user
         :name
         (•g/directive• :include {:if $expandedInfo}
           [:... :birthday])])")

        (el :h3 "Fragment - inline typing")

        (eg "(g/defquery inlineFragmentTyping []
              [:profiles {:handles [\"zuck\" \"cocacola\"]}
               :handle
               [:... •{:on :User}•
                [:likers :count]]])")


        ])]))

(defn ^:export render []
  (react-dom/render (examples)
                    (js/document.getElementById "app")))

(defn ^:export init []
  (boot/init c-state
             {:path         "/js/compiled/bootstrap"
              :load-on-init '#{x-vec.graphql-doc}}
             (fn []
               (cljs/eval-str
                 c-state
                 (str '(require '[x-vec.graphql :as g :include-macros true]))
                 "cljs-init"
                 opts
                 (fn [{:keys [value error] :as result}]
                   (when error (throw error))
                   (set! eval-ready? true)
                   (render))))))

