(ns frontend.routes.product-groups
  (:require [re-frame.core :as re-frame]
            [frontend.state :as f-state]
            [frontend.util :as f-util]))


(defn product-groups []
  (let [_ (f-util/clog "ENTER product-groups")]
    (fn []
      (let [login-status @(re-frame/subscribe [::f-state/login-status])
            token @(re-frame/subscribe [::f-state/token])
            _ (if-not (and login-status token) (re-frame/dispatch [::f-state/navigate ::f-state/login]))]
        [:div.app
         [:h3 "Product Groups"]
        ;;  [:div.sf-pg-container
        ;;   (product-groups-table product-groups-data)]
        ;;  [:div
        ;;   [:button.sf-basic-button
        ;;    {:on-click (fn [e]
        ;;                 (.preventDefault e)
        ;;                 (re-frame/dispatch [::sf-state/navigate ::sf-state/home]))}
        ;;    "Go to home"]]
         
         ])))
  
  
  )