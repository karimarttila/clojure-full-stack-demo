(ns frontend.routes.products
  (:require
   [re-frame.core :as re-frame]
   [reitit.frontend.easy :as rfe]
   [day8.re-frame.http-fx]
   [frontend.http :as f-http]
   [frontend.state :as f-state]
   [frontend.util :as f-util]))


(re-frame/reg-event-db
 ::ret-ok
 (fn [db [_ res-body]]
   (f-util/clog "reg-event-db")
   (let [pg-id (:pgId res-body)]
     (-> db
         (assoc-in [:products :response] {:ret :ok :res-body res-body})
         (assoc-in [:products :data pg-id] (:products res-body))))))

(re-frame/reg-event-db
 ::ret-failed
 (fn [db [_ res-body]]
   (f-util/clog "reg-event-db failed" db)
   (assoc-in db [:products :response] {:ret :failed
                                       :msg (get-in res-body [:response :msg])})))

(re-frame/reg-sub
 ::products-data
 (fn [db params]
   (f-util/clog "::products-data")
   (f-util/clog "params: " params)
   (let [myParseInt (fn [s] (js/parseInt s 10))
         pgid (f-util/myParseInt (second params))
         data (get-in db [:products :data])]
     (get-in data [pgid]))))



(re-frame/reg-sub
 ::product-group-name
 (fn [db params]
   (f-util/clog "::product-group-name, params" params)
   (let [pg-id (f-util/myParseInt (second params))]
     (:name (first (filter (fn [item]
                             (= pg-id (:pgId item)))
                           (get-in db [:product-groups :data])))))))


;; We could just get the specific product from re-frame :products key,
;; but let's fetch it using the backend API.
(re-frame/reg-event-fx
 ::get-products
 (fn [{:keys [db]} [_ pg-id]]
   (f-util/clog "get-product, pg-id" pg-id)
   (f-http/http-get db (str "/api/products/" pg-id) nil ::ret-ok ::ret-failed)))


(re-frame/reg-event-db
 ::reset-product
 (fn [db param]
   (f-util/clog "reg-event-db reset-product" param)
   (assoc-in db [:product] nil)))

(defn products-table [data]
  [:table
   [:thead
    [:tr
     [:th "Id"]
     [:th "Name"]]]
   [:tbody
    (map (fn [item]
           (let [{pg-id :pgId p-id :pId title :title} item]
             [:tr {:key p-id}
              [:td [:a {:href (rfe/href ::f-state/product {:pgid pg-id :pid p-id})} p-id]]
              [:td title]]))
         data)]])


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
      (let [;; Reset product so that we are forced to fetch the new product in the product page.
            _ (re-frame/dispatch [::reset-product])
            login-status @(re-frame/subscribe [::f-state/login-status])
            token @(re-frame/subscribe [::f-state/token])
            _ (when-not (and login-status token) (re-frame/dispatch [::f-state/navigate ::f-state/login]))
            data @(re-frame/subscribe [::products-data pgid])
            product-group-name @(re-frame/subscribe [::product-group-name pgid])
            title (str "Products - " product-group-name)
            _ (when-not data (re-frame/dispatch [::get-products pgid]))]
        [:div.app
         [:div.p-4
          [:p.text-left.text-lg.font-bold.p-4 title]
          [:div.p-4
           [products-table data]]]]))))


