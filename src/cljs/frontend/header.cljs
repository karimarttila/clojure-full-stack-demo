(ns frontend.header
  (:require [re-frame.core :as re-frame]
            [cljs.pprint]
            [frontend.state :as f-state]))


(defn header []
  (fn []
    (let [login-status @(re-frame/subscribe [::f-state/login-status])
          username @(re-frame/subscribe [::f-state/username])]
      [:div.flex.grow.bg-gray-200.p-4
       [:div.flex.flex-col.grow
        [:div.flex.justify-end
         (when (= login-status :logged-in)
           [:div.flex.justify-right.gap-2
            [:p username]
            ;; NOTE: CSS for a is defined in app.css
            [:a {:on-click #(re-frame/dispatch [::f-state/logout]) } "Logout"]])]
        [:div.flex.justify-center
         [:h1.text-3xl.text-center.font-bold "Demo Webtore"]]]
       ])))


;:className "font-medium text-blue-600 dark:text-blue-500 hover:underline"
