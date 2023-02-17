(ns frontend.routes.index
  (:require [re-frame.core :as re-frame]
            [frontend.state :as f-state]
            ))

(defn landing-page []
  [:div.app
   [:div
    [:div.p-4
     [:p.text-left.p-4
      "This is a demo webstore built for learning the following technologies:"]
     [:div.p-4
      [:p.text-left.font-bold "Backend:"]
      [:ul.list-disc.list-inside
       [:li "Clojure"]
       [:li "JVM"]]]
     [:div.p-4
      [:p.text-left.font-bold "Frontend:"]
      [:ul.list-disc.list-inside
       [:li "Clojurescript"]
       [:li "React with Reagent"]
       [:li "Re-frame"]
       [:li "Tailwind"]]]
     [:div.p-4.text-center
      [:button 
       {:on-click (fn [e]
                    (.preventDefault e)
                    (re-frame/dispatch [::f-state/navigate ::f-state/product-groups]))}
       "Enter"]]]]])