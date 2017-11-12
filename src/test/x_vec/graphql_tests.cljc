(ns x-vec.graphql-tests
  (:require [x-vec.core :as x]
            [clojure.string :as string]
            [x-vec.graphql :as g :refer [query] :include-macros true]
            [cljs.test :refer [deftest is are testing]]))


(defn trim [s] (string/replace s #"\n\s*" " "))
(def emit (comp trim :string g/emit))

(deftest xvec-graphql

  (testing "graphql-emit"

    (are [in out]
      (= (emit in) (trim out))
      [:user] "user"
      [:user {:id 4}] "user(id: 4)"


      [:user {:id 4} :name]
      "user(id: 4) { name }"

      [:user {:id 4} :name
       [:parent {:id 5} :name]]
      "user(id: 4) { name parent(id: 5) { name } }")


    ;; g/query
    (is (= (trim (:string (query MyQuery [$some :Boolean]
                                 :user
                                 :id)))
           "query MyQuery($some: Boolean) { user id }"))

    ;; aliases
    (is (= (emit [:user {:id 4}
                  [:smallPic {:gql/alias-of :profilePic
                              :size         1024}]])
           "user(id: 4) { smallPic: profilePic(size: 1024) }"))

    (is (= (emit [:zuck {:gql/alias-of :user
                         :id           4} :id :name])
           "zuck: user(id: 4) { id name }"))

    ;; fragments
    (g/deffragment friend-fields {:on :SomeType}
      :name :id)

    (is (= (trim (:string (query withNestedFragments []
                                 [:user {:id 4}
                                  [:friends {:first 10}
                                   friend-fields]

                                  [:mutualFriends {:first 10}
                                   friend-fields]])))
           (trim "query withNestedFragments {
                    user(id: 4) {
                      friends(first: 10) {
                        ...x_vec_graphql_tests_friend_fields
                      }
                      mutualFriends(first: 10) {
                        ...x_vec_graphql_tests_friend_fields
                      }
                    }
                  }

                  fragment x_vec_graphql_tests_friend_fields on SomeType {
                    name
                    id
                  }")))



    (g/deffragment user-fields {:on :User}
      [:friends :count])
    (g/deffragment page-fields {:on :Page}
      [:likers :count])
    (g/defquery FragmentTyping []
      [:profiles {:handles ["zuck", "cocacola"]}
       :handle
       user-fields
       page-fields])

    (is (= (trim (:string FragmentTyping))
           (trim "query FragmentTyping {
                    profiles(handles: [\"zuck\", \"cocacola\"]) {
                      handle
                      ...x_vec_graphql_tests_user_fields
                      ...x_vec_graphql_tests_page_fields
                    }
                  }

                  fragment x_vec_graphql_tests_user_fields on User {
                    friends {
                      count
                    }
                  }

                  fragment x_vec_graphql_tests_page_fields on Page {
                    likers {
                      count
                    }
                  }")))))



(comment
  (defquery inlineFragmentNoType [$expandedInfo :Boolean]
            [:user {:handle "zuck"}
             :id
             :name
             (fragment {:gql/directives {:include {:if :$expandedInfo}}}
                       :firstName
                       :lastName
                       :birthday)])
  (defquery skipDirective [$someTest :Boolean]
            [:experimentalField-1 {:gql/directives {:skip    {:if :$someTest}
                                                    :include {:if :$otherTest}}}]
            [:experimentalField-2 {:gql/directives {:include {:if :$someTest}}}])
  (defquery customDirective []
            [:search {:text "cat"}
             :text
             [:id {:gql/directives {:instrument {:tag "search.id"}}}]])
  (println
    inlineFragmentNoType
    \newline
    skipDirective
    \newline
    customDirective))

(comment
  (defquery inlineFragmentTyping []
            [:profiles {:handles ["zuck" "cocacola"]}
             :handle
             (fragment {:on :User}
                       [:likers :count])
             (fragment {:on :Page}
                       [:likers :count])
             ])
  (println inlineFragmentTyping))

(comment
  (g/deffragment user-fields {:on :User}
    [:friends :count])
  (g/deffragment page-fields {:on :Page}
    [:likers :count])
  (g/defquery FragmentTyping []
    [:profiles {:handles ["zuck", "cocacola"]}
     :handle
     user-fields
     page-fields])
  (prn FragmentTyping))
