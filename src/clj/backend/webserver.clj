(ns backend.webserver
  (:require [clojure.tools.logging :as log]
            ;[clojure.string :as string]
            [ring.util.http-response :as ring-response]
            [reitit.ring :as reitit-ring]
            [reitit.coercion.malli]
            [reitit.swagger :as reitit-swagger]
            [reitit.swagger-ui :as reitit-swagger-ui]
            [reitit.ring.malli]
            [reitit.ring.coercion :as reitit-coercion]
            [reitit.ring.middleware.muuntaja :as reitit-muuntaja]
            [reitit.ring.middleware.exception :as reitit-exception]
            [reitit.ring.middleware.parameters :as reitit-parameters]
            [reitit.ring.middleware.dev]
            [muuntaja.core :as mu-core]
            [backend.domaindb.users :as b-users]
            ))


(defn make-response [response-value]
  (if (= (:ret response-value) :ok)
    (ring-response/ok response-value)
    (ring-response/bad-request response-value)))


(defn info
  "Gets the info."
  [_]
  (log/debug "ENTER info")
  {:status 200 :body {:info "/info.html => Info in HTML format"}})

(defn validate-parameters
  "Extremely simple validator - just checks that all fields must have some value.
  `field-values` - a list of fields to validate."
  [field-values]
  (every? #(seq %) field-values))

(defn make-response [response-value]
  (if (= (:ret response-value) :ok)
    (ring-response/ok response-value)
    (ring-response/bad-request response-value)))



(defn login
  "Provides API for login page."
  [env username password]
  (log/debug "ENTER login")
  (let [validation-passed (validate-parameters [username password])
        token (if validation-passed
                (b-users/validate-user env username password)
                nil)
        #_#_credentials-ok (if validation-passed
                         (b-users/check-password env username password)
                         nil)
        #_#_token (if credentials-ok
                (ss-session-s/create-json-web-token env email)
                nil)
        #_#_response-value (if (not validation-passed)
                             {:ret :failed, :msg "Validation failed - some fields were empty"}
                             (if (not credentials-ok)
                               {:ret :failed, :msg "Credentials are not good - either email or password is not correct"}
                               (if (not json-web-token)
                                 {:ret :failed, :msg "Internal error when creating the json web token"}
                                 {:ret :ok, :msg "Credentials ok" :json-web-token json-web-token})))]
    ;(make-response response-value)
    token
    ))

;; UI is in http://localhost:7171/index.html
(defn routes
  "Routes."
  [env]
  ;; http://localhost:7171/swagger.json
  [["/swagger.json"
    {:get {:no-doc true
           :swagger {:info {:title "simpleserver api"
                            :description "SimpleServer Api"}
                     :tags [{:name "api", :description "api"}]}
           :handler (reitit-swagger/create-swagger-handler)}}]
   ;; http://localhost:7171/api-docs/index.html
   ["/api-docs/*"
    {:get {:no-doc true
           :handler (reitit-swagger-ui/create-swagger-ui-handler
                     {:config {:validatorUrl nil}
                      :url "/swagger.json"})}}]
   ["/api"
    {:swagger {:tags ["api"]}}
    ; For development purposes. Try curl http://localhost:7171/api/ping
    ["/ping" {:get {:summary "ping get"
                    ; Don't allow any query parameters.
                    :parameters {:query [:map]}
                    :responses {200 {:description "Ping success"}}
                    :handler (constantly (make-response {:ret :ok, :reply "pong" :env env}))}
              :post {:summary "ping post"
                     :responses {200 {:description "Ping success"}}
                     ;; reitit adds mt/strip-extra-keys-transformer - probably changes in reitit 1.0,
                     ;; and therefore {:closed true} is not used with reitit < 1.0.
                     :parameters {:body [:map {:closed true} [:ping string?]]}
                     :handler (fn [req]
                                (let [body (get-in req [:parameters :body])
                                      myreq (:ping body)]
                                  (-> {:ret :ok :request myreq :reply "pong"}
                                      (make-response))))}}]
    ["/info" {:get {:summary "Get info regarding the api"
                    :parameters {:query [:map]}
                    :responses {200 {:description "Info success"}}
                    :handler (fn [{}] (info env))}}]
    ["/login" {:post {:summary "Login to the web-store"
                      :responses {200 {:description "Login success"}}
                      :parameters {:body [:map
                                          [:username string?]
                                          [:password string?]]}
                      :handler (fn [req]
                                 (let [body (get-in req [:parameters :body])
                                       {:keys [username password]} body]
                                   (login env username password)))}}]
    ]])

;; NOTE: If you want to check what middleware does you can uncomment rows 67-69 in:
;; https://github.com/metosin/reitit/blob/master/examples/ring-swagger/src/example/server.clj#L67-L69

(defn handler
  "Handler."
  [routes]
  (->
   (reitit-ring/ring-handler
    (reitit-ring/router routes {; Use this to debug middleware handling:
                                  ;:reitit.middleware/transform reitit.ring.middleware.dev/print-request-diffs
                                :data {:muuntaja mu-core/instance
                                       :coercion (reitit.coercion.malli/create
                                                  {;; set of keys to include in error messages
                                                   :error-keys #{:type :coercion :in #_:schema #_:value #_:errors :humanized #_:transformed}
                                                      ;; validate request & response
                                                   :validate true
                                                      ;; top-level short-circuit to disable request & response coercion
                                                   :enabled true
                                                      ;; strip-extra-keys (effects only predefined transformers)
                                                   :strip-extra-keys true
                                                      ;; add/set default values
                                                   :default-values true
                                                      ;; malli options
                                                   :options nil}) ;; malli
                                       :middleware [;; swagger feature
                                                    reitit-swagger/swagger-feature
                                                      ;; query-params & form-params
                                                    reitit-parameters/parameters-middleware
                                                      ;; content-negotiation
                                                    reitit-muuntaja/format-negotiate-middleware
                                                      ;; encoding response body
                                                    reitit-muuntaja/format-response-middleware
                                                      ;; exception handling
                                                    (reitit-exception/create-exception-middleware
                                                     (merge
                                                      reitit-exception/default-handlers
                                                      {::reitit-exception/wrap (fn [handler ^Exception e request]
                                                                                 (log/error e (.getMessage e))
                                                                                 (handler e request))}))
                                                      ;; decoding request body
                                                    reitit-muuntaja/format-request-middleware
                                                      ;; coercing response bodys
                                                    reitit-coercion/coerce-response-middleware
                                                      ;; coercing request parameters
                                                    reitit-coercion/coerce-request-middleware]}})
    (reitit-ring/routes
     (reitit-ring/redirect-trailing-slash-handler)
     (reitit-ring/create-file-handler {:path "/", :root "target/shadow/dev/resources/public"})
     (reitit-ring/create-resource-handler {:path "/"})
     (reitit-ring/create-default-handler)))))

; Rich comment.
(comment

  (require '[clj-http.client])

  (clj-http.client/get
   (str "http://localhost:7171/api/info") {:debug true :accept "application/json"})

  (clj-http.client/get
   (str "http://localhost:7171/index.html") {:debug true})

  (clj-http.client/get
   (str "http://localhost:7171") {:debug true})

  (clj-http.client/get
   (str "http://localhost:7171/index.html") {:debug true})

  (user/system)
  (handler {:routes (routes (user/env))})

  (clj-http.client/get
   (str "https://reqres.in/api/users/2") {:debug true :accept "application/json"})

  (clj-http.client/get
   (str "http://localhost:7171/info") {:debug true :accept "application/edn"})
  )


