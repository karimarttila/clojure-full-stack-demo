(ns frontend.routes.product
  (:require
   [re-frame.core :as re-frame]
   [day8.re-frame.http-fx]
   [frontend.http :as f-http]
   [frontend.state :as f-state]
   [frontend.util :as f-util]))


(re-frame/reg-event-db
 ::ret-ok
 (fn [db [_ res-body]]
   (f-util/clog "reg-event-db ok: " res-body)
   (let [pgid (:pg-id res-body)
         pid (:p-id res-body)]
     (-> db
         (assoc-in [:product :response] {:ret :ok :res-body res-body})
         (assoc-in [:product :data] (:product res-body))))))


(re-frame/reg-event-db
 ::ret-failed
 (fn [db [_ res-body]]
   (f-util/clog "reg-event-db failed" db)
   (assoc-in db [:product :response] {:ret :failed
                                      :msg (get-in res-body [:response :msg])})))


(re-frame/reg-sub
 ::product-data
 (fn [db params]
   (f-util/clog "::product-data, params" params)
   (let [data (get-in db [:product :data]) ]
     data)))


(re-frame/reg-event-fx
 ::get-product
 (fn [{:keys [db]} [_ pg-id p-id]]
   (f-util/clog "get-product, pg-id, p-id" {:pg-id pg-id :p-id p-id})
   (f-http/http-get db (str "/api/product/" pg-id "/" p-id) nil ::ret-ok ::ret-failed)))


(defn product-table [data]
  [:table
   [:thead
    [:tr
     [:th "Field"]
     [:th "Value"]]]
   [:tbody
    (map (fn [item]
           (let [[field value] item]
             [:tr {:key field}
              [:td field]
              [:td value]]))
         data)]])


(defn product
  "Product view."
  [match]
   (let [_ (f-util/clog "ENTER product-page, match" match)
           {:keys [path]} (:parameters match)
           {:keys [pgid pid]} path
           pgid (str pgid)
           pid (str pid)
           _ (f-util/clog "path" path)
           _ (f-util/clog "pgid" pgid)
           _ (f-util/clog "pid" pid)]

       (fn []
         (let [title "Product"
               login-status @(re-frame/subscribe [::f-state/login-status])
               token @(re-frame/subscribe [::f-state/token])
               _ (when-not (and login-status token) (re-frame/dispatch [::f-state/navigate ::f-state/login]))
               data @(re-frame/subscribe [::product-data pgid pid])
               _ (when-not data (re-frame/dispatch [::get-product pgid pid]))]
           [:div.app
            [:div.p-4
             [:p.text-left.text-lg.font-bold.p-4 title]
             [:div.p-4
              [product-table data]]]]))))

