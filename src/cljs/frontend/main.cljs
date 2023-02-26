(ns frontend.main
  (:require [re-frame.core :as re-frame]
            [re-frame.db]
            [reagent.dom :as r-dom]
            [day8.re-frame.http-fx] ; Needed to register :http-xhrio to re-frame.
            [reagent-dev-tools.core :as dev-tools]
            [reitit.coercion.spec :as rss]
            [reitit.frontend :as rf]
            [reitit.frontend.controllers :as rfc]
            [reitit.frontend.easy :as rfe]
            [frontend.util :as f-util]
            [frontend.header :as f-header]
            [frontend.routes.index :as f-index]
            [frontend.state :as f-state]
            [frontend.routes.login :as f-login]
            [frontend.routes.product-groups :as f-product-group]
            [frontend.routes.products :as f-products]
            [frontend.routes.product :as f-product]
            ))

;; ******************************************************************
;; NOTE: When starting ClojureScript REPL, give first command:
; (shadow.cljs.devtools.api/repl :app)
; to connect the REPL to the app running in the browser.
;; ******************************************************************

;;; Events ;;;


(re-frame/reg-event-db
 ::initialize-db
 (fn [_ _]
   {:current-route nil
    :token nil
    :debug true
    :login-status nil
    :username nil
    :product-groups nil
    :products nil
    :product nil
    }))

(re-frame/reg-event-fx
 ::f-state/navigate
 (fn [_ [_ & route]]
    ;; See `navigate` effect in routes.cljs
   {::navigate! route}))

(re-frame/reg-event-db
 ::f-state/navigated
 (fn [db [_ new-match]]
   (let [old-match (:current-route db)
         new-path (:path new-match)
         controllers (rfc/apply-controllers (:controllers old-match) new-match)]
     (js/console.log (str "new-path: " new-path))
     (cond-> (assoc db :current-route (assoc new-match :controllers controllers))
       (= "/" new-path) (->  (assoc :login-status nil)
                             (assoc :user nil))))))

(re-frame/reg-event-fx
 ::f-state/logout
 (fn [cofx [_]]
   (let [db (:db cofx)]
     {:db (-> db
              (assoc-in [:login] nil)
              (assoc-in [:username] nil)
              (assoc-in [:login-status] nil)
              (assoc-in [:token] nil))
      :fx [[:dispatch [::f-state/navigate ::f-state/login]]]})))

#_(re-frame/reg-event-fx
 ::f-state/logout
 (fn [cofx [_]]
   {:db (assoc (:db cofx) :token nil)
    :fx [[:dispatch [::f-state/navigate ::f-state/home]]]}))

;;; Views ;;;


(defn home-page []
  (let [token @(re-frame/subscribe [::f-state/token])]
    ; If we have jwt in app db we are logged-in.
    (f-util/clog "ENTER home-page")
    [f-index/landing-page]
    #_(if jwt
      (re-frame/dispatch [::f-state/navigate ::f-state/product-group])
      ;; NOTE: You need the div here or you are going to see only the debug-panel!
      [:div
       (welcome)
       (f-util/debug-panel {:token token})])))


;;; Effects ;;;

;; Triggering navigation from events.
(re-frame/reg-fx
 ::navigate!
 (fn [route]
   (apply rfe/push-state route)))

;;; Routes ;;;

(defn href
  "Return relative url for given route. Url can be used in HTML links."
  ([k]
   (href k nil nil))
  ([k params]
   (href k params nil))
  ([k params query]
   (rfe/href k params query)))


(def routes-dev
  ["/"
   [""
    {:name ::f-state/home
     :view home-page
     :link-text "Home"
     :controllers
     [{:start (fn [& params] (js/console.log (str "Entering home page, params: " params)))
       :stop (fn [& params] (js/console.log (str "Leaving home page, params: " params)))}]}]
   ["login"
      {:name ::f-state/login
       :view f-login/login
       :link-text "Login"
       :controllers [{:start (fn [& params] (js/console.log (str "Entering login, params: " params)))
                      :stop (fn [& params] (js/console.log (str "Leaving login, params: " params)))}]}]
   ["product-group"
      {:name ::f-state/product-groups
       :view f-product-group/product-groups
       :link-text "Product group"
       :controllers [{:start (fn [& params] (js/console.log (str "Entering product-group, params: " params)))
                      :stop (fn [& params] (js/console.log (str "Leaving product-group, params: " params)))}]}]
   ["products/:pgid"
      {:name ::f-state/products
       :parameters {:path {:pgid int?}}
       :view f-products/products
       :link-text "Products"
       :controllers [{:start (fn [& params] (js/console.log (str "Entering products, params: " params)))
                      :stop (fn [& params] (js/console.log (str "Leaving products, params: " params)))}]}]
   ["product/:pgid/:pid"
      {:name ::f-state/product
       :parameters {:path {:pgid int?
                           :pid int?}}
       :view f-product/product
       :link-text "Product"
       :controllers [{:start (fn [& params] (js/console.log (str "Entering product, params: " params)))
                      :stop (fn [& params] (js/console.log (str "Leaving product, params: " params)))}]}]])

(def routes routes-dev)

(defn on-navigate [new-match]
  (f-util/clog "on-navigate, new-match" new-match)
  (when new-match
    (re-frame/dispatch [::f-state/navigated new-match])))

(def router
  (rf/router
   routes
   {:data {:coercion rss/coercion}}))

(defn init-routes! []
  (js/console.log "initializing routes")
  (rfe/start!
   router
   on-navigate
   {:use-fragment true}))

(defn router-component [_] ; {:keys [router] :as params}
  (f-util/clog "ENTER router-component")
  (let [current-route @(re-frame/subscribe [::f-state/current-route])
        path-params (:path-params current-route)
        _ (f-util/clog "router-component, path-params" path-params)]
    [:div
     [f-header/header]
     ; NOTE: when you supply the current-route to the view it can parse path-params there (from path)
     (when current-route
       [(-> current-route :data :view) current-route])]))

;;; Setup ;;;

(def debug? ^boolean goog.DEBUG)

(defn dev-setup []
  (when debug?
    (enable-console-print!)
    (println "dev mode")))


(defn ^:dev/after-load start []
  (js/console.log "ENTER start")
  (re-frame/clear-subscription-cache!)
  (init-routes!)
  (r-dom/render [router-component {:router router}
                 ;; TODO.
                ;;  (if (:open? @dev-tools/dev-state)
                ;;    {:style {:padding-bottom (str (:height @dev-tools/dev-state) "px")}})
                 ]
                (.getElementById js/document "root")))


(defn ^:export init []
  (js/console.log "ENTER init")
  (re-frame/dispatch-sync [::initialize-db])
  (dev-tools/start! {:state-atom re-frame.db/app-db})
  (dev-setup)
  (start))

(comment
  ; With Calva, no need for this:
  ;(shadow.cljs.devtools.api/repl :app)
  ; But you have to start the browser since the browser is the runtime for the repl!
  ; http://localhost:7171
  (+ 1 2)
  ;(reagent.dom/render [])
  ;(require '[hashp.core :include-macros true])
  ;(let [a #p (range 5)] a)
  )


