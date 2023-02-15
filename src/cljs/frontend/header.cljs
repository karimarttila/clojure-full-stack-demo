(ns frontend.header
  (:require [re-frame.core :as re-frame]
            [cljs.pprint]
    ;[clojure.pprint]
            [frontend.state :as sf-state]))

(defn header []
  [:div.flex.grow.bg-gray-200.p-4
   [:div.flex-col.grow
    [:div.flex.justfiy-end
     [:div.flex.justify-right.gap-2
      [:p "TODO: USERXXX"]
      [:p "TODO: LOGOUT"]]]]
   [:div.flex.justify-center
    [:h1.text-3xl.text-center.font-bold "Demo Webtore"]]
   [:div
    (let [jwt @(re-frame/subscribe [::sf-state/jwt])
          current-route @(re-frame/subscribe [::sf-state/current-route])]
      #_(js/console.log "jwt: " jwt)
      [:div
       #_{:clj-kondo/ignore [:missing-else-branch]}
       (if (and (= (:path current-route) "/") (not jwt))
         [:button
          {:on-click #(re-frame/dispatch [::sf-state/navigate ::sf-state/signin])}
          "Sign-In"])
       (if (and (= (:path current-route) "/") (not jwt))
         [:button
          {:on-click #(re-frame/dispatch [::sf-state/navigate ::sf-state/login])}
          "Login"])
       (if jwt
         [:button
          {:on-click #(re-frame/dispatch [::sf-state/logout])}
          "Logout"])])
    [:div] ; Extra div so that we able to see the Sign-in and Login buttons with the 10x tool panel.
    ]])
