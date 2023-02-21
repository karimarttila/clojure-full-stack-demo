(ns backend.webserver-test
  (:require [clojure.test :refer [deftest use-fixtures is testing]]
            [clojure.tools.logging :as log]
            [clojure.data.codec.base64 :as base64]
            [backend.db.users :as b-users]
            [backend.test-config :as test-config]))


(defn init-fixture
  []
  (log/debug "ENTER init-fixture")
  (b-users/clear-sessions (:backend/env @test-config/test-system)))

(defn webserver-test-fixture
  [f]
  (log/debug "ENTER webserver-test-fixture")
  (test-config/test-system-fixture-runner init-fixture f)
  (log/debug "EXIT webserver-test-fixture"))

(use-fixtures :each webserver-test-fixture)

(deftest info-test
  (log/debug "ENTER info-test")
  (testing "GET: /api/info"
    (let [ret (test-config/call-api :get "info" nil nil)]
      (is (= 200 (ret :status) 200))
      (is (= {:info "/info.html => Info in HTML format"} (ret :body))))))

(deftest ping-get-test
  (log/debug "ENTER ping-get-test")
  (testing "GET: /api/ping"
    (let [ret (test-config/call-api :get "ping" nil nil)]
      (is (= 200 (ret :status)))
      (let [body (ret :body)]
        (is (= "pong" (:reply body)))
        (is (= "ok" (:ret body)))))))

(deftest failed-ping-get-extra-query-params-test
  (log/debug "ENTER failed-ping-get-extra-query-params-test")
  (testing "GET: /api/ping"
    (let [ret (test-config/call-api :get "ping?a=1" nil nil)]
      (is (= 400 (ret :status) 400))
      (is (= {:coercion "malli"
              :humanized {:a ["disallowed key"]}
              :in ["request"
                   "query-params"]
              :type "reitit.coercion/request-coercion"}
             (ret :body))))))

(deftest ping-post-test
  (log/debug "ENTER ping-post-test")
  (testing "POST: /api/ping"
    (let [ret (test-config/call-api :post "ping" nil {:ping "hello"})]
      (is (= 200 (ret :status)))
      (is (= {:reply "pong" :request "hello" :ret "ok"} (ret :body) )))))

(deftest failed-ping-post-missing-key-test
  (log/debug "ENTER failed-ping-post-missing-key-test")
  (testing "POST: /api/ping"
    (let [ret (test-config/call-api :post "ping" nil {:wrong-key "hello"})]
      (is (= 400 (ret :status) 400))
      (is (= {:coercion "malli"
              :humanized {:ping ["missing required key"]}
              :in ["request"
                   "body-params"]
              :type "reitit.coercion/request-coercion"} 
             (ret :body))))))

(deftest login-test
  (log/debug "ENTER login-test")
  (testing "POST: /api/login"
    ; First with good credentials.
    (let [good-test-body {:username "jartsa" :password "joo"}
          bad-test-body {:username "jartsa" :password "WRONG-PASSWORD"}]
      (let [ret (test-config/call-api :post "login" nil good-test-body)]
        (is (= 200 (ret :status)))
        (is (= "ok" (get-in ret [:body :ret]) "ok"))
        (is (= java.lang.String (type (get-in ret [:body :token]))))
        (is (< 30 (count (get-in ret [:body :token])))))
      ; Next with bad credentials.
      (let [ret (test-config/call-api :post "login" nil bad-test-body)]
        (is (= 400 (ret :status)))
        (is (= "failed" (get-in ret [:body :ret])))
        (is (= "Login failed" (get-in ret [:body :msg])))))))

(deftest product-groups-test
  (log/debug "ENTER product-groups-test")
  (testing "GET: /api/product-groups"
    (let [creds {:username "jartsa" :password "joo"}
          login-ret (test-config/call-api :post "login" nil creds)
          _ (log/debug (str "Got login-ret: " login-ret))
          token (get-in login-ret [:body :token])
          params {:x-token token}
          get-ret (test-config/call-api :get "/product-groups" params nil)
          status (:status get-ret)
          body (:body get-ret)
          right-body {:ret "ok", :product-groups [{:pgId 1, :name "Books"} {:pgId 2, :name "Movies"}]}]
      (is (= true (not (nil? token))))
      (is (= 200 status))
      (is (= right-body body)))))


(deftest products-test
  (log/debug "ENTER products-test")
  (testing "GET: /api/products"
    (let [creds {:username "jartsa" :password "joo"}
          login-ret (test-config/call-api :post "login" nil creds)
          _ (log/debug (str "Got login-ret: " login-ret))
          token (get-in login-ret [:body :token])
          params {:x-token token}
          get-ret (test-config/call-api :get "/products/1" params nil)
          status (:status get-ret)
          body (:body get-ret)]
      (is (= (not (nil? token)) true))
      (is (= 200 status))
      (is (= "ok" (:ret body)))
      (is (= 1 (:pgId body)))
      (is (= 35 (count (:products body)))))))


(deftest product-test
  (log/debug "ENTER product-test")
  (testing "GET: /api/product"
    (let [creds {:username "jartsa" :password "joo"}
          login-ret (test-config/call-api :post "login" nil creds)
          _ (log/debug (str "Got login-ret: " login-ret))
          token (get-in login-ret [:body :token])
          params {:x-token token}
          get-ret (test-config/call-api :get "/product/2/49" params nil)
          status (:status get-ret)
          body (:body get-ret)]
      (is (= (not (nil? token)) true))
      (is (= 200 status))
      (is (= "ok" (:ret body)))
      (is (= 2 (:pgId body)))
      (is (= 49 (:pId body)))
      (is (= {:pId 49,
              :pgId 2,
              :title "Once Upon a Time in the West",
              :price 14.4,
              :director "Leone, Sergio",
              :year 1968,
              :country "Italy-USA",
              :genre "Western"}
             (:product body))))))


