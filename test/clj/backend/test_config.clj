(ns backend.test-config
  (:require
   [clojure.tools.logging :as log]
   [clojure.pprint]
   [integrant.core :as ig]
   [clj-http.client :as http-client]
   [backend.core :as core])
  (:import (java.net ServerSocket)))

(defonce test-system (atom nil))

(defn random-port []
  (with-open [s (ServerSocket. 0)]
    (.getLocalPort s)))

(defn test-config []
  (let [test-port (random-port)
        ; Overriding the port with random port, see TODO below.
        _ (log/debug (str "test-config, using web-server test port: " test-port))]
    (-> (core/system-config :test)
        ;; Use the same data dir also for test system. It just initializes data.
        (assoc-in [:backend/env :data-dir] "resources/data")
        (assoc-in [:backend/jetty :port] test-port)
        (assoc-in [:backend/nrepl :bind] nil)
        (assoc-in [:backend/nrepl :port] nil)
        (assoc-in [:backend/env :port] nil))))


;; (defmethod ig/init-key :backend/env [_ {:keys [_profile data-dir]}]
;;   (log/debug "ENTER ig/init-key :backend/env")
;;   (atom {:domain (b-domain/get-domain-data data-dir)
;;          :sessions #{}
;;          :users (create-users)}))


;; TODO: For some reason Integrant does not always run halt-key for webserver, really weird.
;; And you lose the reference to the web server and web server keeps the port binded => you have to restart REPL.
#_(defn test-config-orig []
    (let [test-port (get-in (ss-config/create-config) [:web-server :test-server-port])]
      (-> (core/system-config)
          ; Overriding the port with test-port.
          (assoc-in [::core/web-server :port] test-port))))

(defn halt []
  (swap! test-system #(if % (ig/halt! %))))

(defn go []
  (halt)
  (reset! test-system (ig/init (test-config))))


(defn test-system-fixture-runner [init-test-data, testfunc]
  (try
    (go)
    (init-test-data)
    (testfunc)
    (finally
      (halt))))

(defn call-api [verb path headers body]
  (let [my-port (-> @test-system :backend/jetty .getConnectors first .getPort)
        my-fn (cond
                (= verb :get) http-client/get
                (= verb :post) http-client/post)]
    (try
      (select-keys
       (my-fn (str "http://localhost:" my-port "/api/" path)
              {:as :json
               :form-params body
               :headers headers
               :content-type :json
               :throw-exceptions false
               :coerce :always}) [:status :body])
      (catch Exception e
        (log/error (str "ERROR in -call-api: " (.getMessage e)))))))


; Rich comment.
#_(comment
  (user/env)
  (test-config)
  *ns*
  (backend.test-config/-call-api :get "info" nil nil)

  (backend.test-config/go)
  (backend.test-config/halt)
  (backend.test-config/test-config)
  backend.test-config/test-system
  ;(backend.test-config/test-env)
  (backend.test-config/test-service)
  
  (backend.test-config/go)
  
  
  (:domain @(-> @test-system :backend/env :db))
  
  (backend.test-config/halt)
  (->> (:out (clojure.java.shell/sh "netstat" "-an")) (clojure.string/split-lines) (filter #(re-find #".*:::61.*LISTEN.*" %)))
  )
