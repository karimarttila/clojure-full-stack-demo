(ns user
  (:require [integrant.repl :refer [reset]]
            ;[integrant.repl :refer [clear go halt prep init reset reset-all]]
            [integrant.repl.state :as state]
            [backend.core :as core]
            ))

(integrant.repl/set-prep! core/system-config-start)

(defn system [] (or state/system (throw (ex-info "System not running" {}))))

(defn env [] (:backend/env (system)))

(defn profile [] (:backend/profile (system)))

(defn my-dummy-reset []
  (reset))

;; NOTE: In Cursive, Integrant hot keys are:
;; M-h: go
;; M-j: reset
;; M-k: halt

; In Calva the Integrant hot keys are:
;; C-T alt+h: go
;; M-j: reset
;; C-T alt+k: halt


#_(comment
  (user/system)
  (user/env)
   ; alt+j hotkey in Cursive
  (integrant.repl/reset)
  (+ 1 2)
  
  )

