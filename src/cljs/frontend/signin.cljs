(ns frontend.signin
  (:require
   [re-frame.core :as re-frame]
   [reagent.core :as r]
   [day8.re-frame.http-fx]
  ;;  [frontend.http :as f-http]
   [frontend.state :as f-state]
   [frontend.util :as f-util]
   ))

(defn empty-user []
  {:first-name "" :last-name "" :email "" :password ""})

(re-frame/reg-event-db
 ::signin-ret-ok
 (fn [db [_ res-body]]
   #_(f-util/clog "reg-event-db ok: " res-body)
   (assoc-in db [:signin :response] {:ret :ok
                                     :msg (str "Email address registered: " (:email res-body))})))

(re-frame/reg-event-db
 ::signin-ret-failed
 (fn [db [_ res-body]]
   #_(f-util/clog "reg-event-db failed" db)
   (assoc-in db [:signin :response] {:ret :failed
                                     :msg (get-in res-body [:response :msg])})))

(re-frame/reg-event-db
 ::close-notification
 (fn [db [_ e]]
   (f-util/clog "reg-event-db close-notification, db: " db)
   (f-util/clog "reg-event-db close-notification, e: " e)
   (assoc-in db [:signin :response] nil)))

(re-frame/reg-sub
 ::signin-response
 (fn [db]
   #_(f-util/clog "reg-sub" db)
   (:response (:signin db))))

(re-frame/reg-event-fx
 ::signin-user
 (fn [{:keys [db]} [_ user-data]]
   (f-util/clog "user-data" user-data)
   ;; TODO (f-http/http-post db "/api/signin" user-data ::signin-ret-ok ::signin-ret-failed)
   ))

(defn signin-page
  "Sign-in view."
  []
  ; NOTE: The user data atom needs to be here and not inside the rendering function
  ; or you create a new atom every time the component re-renders.
  (let [user-data (r/atom (empty-user))
        _ (f-util/clog "ENTER signin-page")
        _ (f-util/clog "let user-data" @user-data)]
    (fn []
      ; NOTE: The re-frame subscription needs to be inside the rendering function or the watch
      ; is not registered to the rendering function.
      (let [{:keys [ret msg] :as r-body} @(re-frame/subscribe [::signin-response])
            notify-div (case ret
                         :ok :div.f-ok-notify
                         :failed :div.f-error-notify
                         nil)]
        [:div
         [:h3 "Sign-in"]
         [:div.f-page-container
          [:div.f-page-form
           [:form
            (f-util/input "First name: " :first-name "text" user-data)
            (f-util/input "Last name: " :last-name "text" user-data)
            (f-util/input "Email: " :email "text" user-data)
            (f-util/input "Password: " :password "password" user-data)
            (if (and (not ret) (every? f-util/valid? @user-data))
              [:div
               [:button.f-basic-button
                {;:type :primary
                 :on-click (fn [e]
                             (.preventDefault e)
                             (re-frame/dispatch [::signin-user @user-data]))}
                "Submit"]])]
           (if-not ret
             [:div
              [:button.f-basic-button
               {:on-click (fn [e]
                            (.preventDefault e)
                            (re-frame/dispatch [::f-state/navigate ::f-state/home]))}
               "Go to home"]])]
          (if ret
            [:div.f-page-inner-notification
             [notify-div
              [:span.f-closebtn {:on-click (fn [e]
                                              (.preventDefault e)
                                              (re-frame/dispatch [::close-notification]))}
               "×"]
              msg]])]

         (f-util/debug-panel {:user-data user-data
                               :ret ret
                               :msg msg
                               :r-body r-body})]))))

(comment

  (def foo (re-frame/subscribe [::signin-response]))
  foo

  ;@re-frame.db/app-db
  
  ;(swap! re-frame.db/app-db assoc-in [:signin :response] {:ret :jee :msg "oh, no"})
  ;@re-frame.db/app-db
  ;(swap! re-frame.db/app-db assoc :debug (not (:debug @re-frame.db/app-db)))
  )


