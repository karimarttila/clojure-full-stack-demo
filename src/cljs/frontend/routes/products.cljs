(ns frontend.routes.products
  (:require
   [re-frame.core :as re-frame]
   [reitit.frontend.easy :as rfe]
   [day8.re-frame.http-fx]
   [frontend.http :as f-http]
   [frontend.state :as f-state]
   [frontend.util :as f-util]))


(defn products
  "Products view."
  [match] ; NOTE: This is the current-route given as parameter to the view. You can get the pgid also from :path-params.
  (let [_ (f-util/clog "ENTER products-page, match" match)
        {:keys [path]} (:parameters match)
        {:keys [pgid]} path
        pgid (str pgid)
        _ (f-util/clog "path" path)
        _ (f-util/clog "pgid" pgid)]
    (fn []
      (let [
            ;; products-data @(re-frame/subscribe [::products-data pgid])
            ;; product-group-name @(re-frame/subscribe [::product-group-name pgid])
            ;; _ (if-not products-data (re-frame/dispatch [::get-products pgid]))
            ]
        [:div
         [:h3 "Products - " #_product-group-name]
         #_[:div.f-pg-container
          (products-table products-data)]
         #_[:div
          [:button.f-basic-button
           {:on-click (fn [e]
                        (.preventDefault e)
                        (re-frame/dispatch [::f-state/navigate ::f-state/home]))}
           "Go to home"]]
         #_(f-util/debug-panel {:products-data products-data})]))))
