(ns frontend.header
  (:require [re-frame.core :as re-frame]
            [cljs.pprint]
    ;[clojure.pprint]
            [frontend.state :as sf-state]))

(defn header []
  [:div.flex.grow.bg-gray-200.p-4
   [:div.flex.flex-col.grow
    [:div.flex.justify-end
     [:div.flex.justify-right.gap-2
      [:p "TODO-user"]
      [:a.font-medium.text-blue-600.dark:text-blue-500.hover:underline "logout"]]]
    [:div.flex.justify-center
     [:h1.text-3xl.text-center.font-bold "Demo Webtore"]]]
   ])
