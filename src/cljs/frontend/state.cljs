(ns frontend.state
  (:require [re-frame.core :as re-frame]))

;; Subscriptions

(re-frame/reg-sub
 ::current-route
 (fn [db]
   (:current-route db)))

(re-frame/reg-sub
 ::token
 (fn [db]
   (:token db)))

(re-frame/reg-sub
 ::username
 (fn [db]
   (:username db)))

(re-frame/reg-sub
 ::login-status
 (fn [db]
   (:login-status db)))

(re-frame/reg-sub
 ::debug
 (fn [db]
   (:debug db)))
