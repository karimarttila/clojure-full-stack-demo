(ns backend.core
  (:require
   [clojure.tools.logging :as log]
   [clojure.pprint]
   [clojure.java.io :as io]
   [ring.adapter.jetty :as jetty]
   [nrepl.server :as nrepl]
   [integrant.repl :as ig-repl]
   [integrant.core :as ig]
   [aero.core :as aero]
   [backend.webserver :as b-webserver]
   [backend.db.domain :as b-domain]
   [clojure.tools.reader.edn :as edn]
   [potpuri.core :as p]))

(defn env-value [key default]
  (some-> (or (System/getenv (name key)) default)))

(defmethod aero/reader 'ig/ref [_ _ value] (ig/ref value))

(defmethod ig/init-key :backend/profile [_ profile]
  profile)

(defn create-users []
  [{:username "jartsa" :password "joo"}
   {:username "rane" :password "jee"}
   {:username "d" :password "d"}])

(defmethod ig/init-key :backend/env [_ {:keys [_profile data-dir] :as m}]
  (log/debug "ENTER ig/init-key :backend/env")
  ;; Simulates our database.
  (conj m {:db (atom {:domain (b-domain/get-domain-data data-dir)
                      :sessions #{}
                      :users (create-users)})}))

(defmethod ig/halt-key! :backend/env [_ this]
  (log/debug "ENTER ig/halt-key! :backend/env")
  this)

(defmethod ig/suspend-key! :backend/env [_ this]
  (log/debug "ENTER ig/suspend-key! :backend/env")
  this)

(defmethod ig/resume-key :backend/env [_ _ _ old-impl]
  (log/debug "ENTER ig/resume-key :backend/env")
  old-impl)

(defmethod ig/init-key :backend/jetty [_ {:keys [port join? env]}]
  (log/debug "ENTER ig/init-key :backend/jetty")
  (-> (b-webserver/handler (b-webserver/routes env))
      (jetty/run-jetty {:port port :join? join?})))

(defmethod ig/halt-key! :backend/jetty [_ server]
  (log/debug "ENTER ig/halt-key! :backend/jetty")
  (.stop server))

(defmethod ig/init-key :backend/options [_ options]
  (log/debug "ENTER ig/init-key :backend/options")
  options)

(defmethod ig/init-key :backend/nrepl [_ {:keys [bind port]}]
  (log/debug "ENTER ig/init-key :backend/nrepl")
  (if (and bind port)
    (nrepl/start-server :bind bind :port port)
    nil))

(defmethod ig/halt-key! :backend/nrepl [_ this]
  (log/debug "ENTER ig/halt-key! :backend/nrepl")
  (if this
    (nrepl/stop-server this)))

(defmethod ig/suspend-key! :backend/nrepl [_ this]
  (log/debug "ENTER ig/suspend-key! :backend/nrepl")
  this)

(defmethod ig/resume-key :backend/nrepl [_ _ _ old-impl]
  (log/debug "ENTER ig/resume-key :backend/nrepl")
  old-impl)

; You can add to the container script PROFILE=prod
(defn read-config [profile]
  (let [local-config (let [file (io/file "config-local.edn")]
                       #_{:clj-kondo/ignore [:missing-else-branch]}
                       (if (.exists file) (edn/read-string (slurp file))))]
    (cond-> (aero/read-config (io/resource "config.edn") {:profile profile})
      local-config (p/deep-merge local-config))))

(defn system-config [myprofile]
  (let [profile (or myprofile (some-> (System/getenv "PROFILE") keyword) :dev)
        _ (log/info "Using profile " profile)
        config (read-config profile)]
    config))

(defn system-config-start []
  (system-config nil))

(defn -main []
  (log/info "System starting...")
  (let [config (system-config-start)
        _ (log/info "Config: " config)]
    (ig-repl/set-prep! (constantly config))
    (ig-repl/go)))


(comment
  (ig-repl/reset)
  (ig-repl/halt)
  (user/system)
  (keys (user/system))
  (:backend/env (user/system))
  (keys (:backend/env (user/system)))
  (keys @(:backend/env (user/system)))
  (type @(:backend/env (user/system)))
  (:users @(:backend/env (user/system)))
  (:users @(:backend/env (user/system)))
  (user/env)
  (user/profile)
  (System/getenv)
  (env-value :PATH :foo)
  )