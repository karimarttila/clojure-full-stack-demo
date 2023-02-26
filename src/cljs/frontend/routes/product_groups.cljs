(ns frontend.routes.product-groups
  (:require [re-frame.core :as re-frame]
            [reitit.frontend.easy :as rfe]
            ;["@tanstack/react-table" :as rt]
            [frontend.state :as f-state]
            [frontend.http :as f-http]
            [frontend.util :as f-util]))

(re-frame/reg-event-db
 ::ret-ok
 (fn [db [_ res-body]]
   (f-util/clog "reg-event-db ok: " res-body)
   (-> db
       (assoc-in [:product-groups :response] {:ret :ok :res-body res-body})
       (assoc-in [:product-groups :data] (:product-groups res-body)))))

(re-frame/reg-event-db
 ::ret-failed
 (fn [db [_ res-body]]
   (f-util/clog "reg-event-db failed" db)
   (assoc-in db [:product-groups :response] {:ret :failed
                                             :msg (get-in res-body [:response :msg])})))

(re-frame/reg-sub
 ::product-groups-data
 (fn [db]
   (get-in db [:product-groups :data])))


(re-frame/reg-event-fx
 ::get-product-groups
 (fn [{:keys [db]} [_]]
   (f-util/clog "get-product-groups")
   (f-http/http-get db "/api/product-groups" nil ::ret-ok ::ret-failed)))


;; Let's implement a simple basic html table first, 
;; and later provide an example using @tanstack/react-table.
(defn product-groups-simple-table 
  [data]
  (let [_ (f-util/clog "ENTER product-groups-table")]
    [:div.p-4
     [:table
      [:thead
       [:tr
        [:th "Id"]
        [:th "Name"]]]
      [:tbody
       (map (fn [item]
              (let [{pg-id :pgId pg-name :name} item]
                [:tr {:key pg-id}
                 [:td [:a {:href (rfe/href ::f-state/products {:pgid pg-id})} pg-id]]
                 [:td pg-name]]))
            data)]]]))


(defn product-groups []
  (let [_ (f-util/clog "ENTER product-groups")]
    (fn []
      (let [title " Product Groups"
            login-status @(re-frame/subscribe [::f-state/login-status])
            token @(re-frame/subscribe [::f-state/token])
            _ (when-not (and login-status token) (re-frame/dispatch [::f-state/navigate ::f-state/login]))
            product-groups-data @(re-frame/subscribe [::product-groups-data])
            _ (when-not product-groups-data (re-frame/dispatch [::get-product-groups]))
            ]
        [:div.app
         [:div.p-4
          [:p.text-left.text-lg.font-bold.p-4 title]
          [:div.p-4
           [product-groups-simple-table product-groups-data]]]]))))

