(ns frontend.routes.product-groups
  (:require [re-frame.core :as re-frame]
            [reitit.frontend.easy :as rfe]
            ["@tanstack/react-table" :as rt]
            ["react" :as react :default useMemo]
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

;; Example of Clojurescript / Javascript interop.
(defn product-groups-react-table
  [data]
  (let [_ (f-util/clog "ENTER product-groups-table") 
        columnHelper (rt/createColumnHelper)
        columns #js [ (.accessor columnHelper "pgId" #js {:header "Id" :cell (fn [info] (.getValue info) )})
                     (.accessor columnHelper "name" #js {:header "Name" :cell (fn [info] (.getValue info))})]
        table (rt/useReactTable #js {:columns columns :data (clj->js data) :getCoreRowModel (rt/getCoreRowModel)})
        ^js headerGroups (.getHeaderGroups table)]
    [:div.p-4
     [:table
      [:thead
       (for [^js headerGroup headerGroups]
         [:tr {:key (.-id headerGroup) }
          (for [^js header (.-headers headerGroup)]
            [:th {:key (.-id header) }
             (if (.-isPlaceholder header)
               nil
               (rt/flexRender (.. header -column -columnDef -header) (.getContext header)))])])]
      [:tbody
       (for [^js row (.-rows (.getRowModel table))]
         [:tr {:key (.-id row)}
          (for [^js cell (.getVisibleCells row)]
            [:td {:key (.-id cell)}
             (rt/flexRender (.. cell -column -columnDef -cell) (.getContext cell))])])]]]))


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
           ;[product-groups-simple-table product-groups-data]
           [:f> product-groups-react-table product-groups-data]
           ]]]))))

