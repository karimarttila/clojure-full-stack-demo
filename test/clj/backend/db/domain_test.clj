(ns backend.db.domain-test
  (:require [clojure.test :refer [deftest use-fixtures is testing]]
            [clojure.tools.logging :as log]
            [backend.db.domain :as b-domain]
            [backend.test-config :as test-config]))

(defn init-fixture []
  ;; Do nothing.
  )


(defn domain-test-fixture
  [f]
  (log/debug "ENTER domain-test-fixture")
  (test-config/test-system-fixture-runner init-fixture f)
  (log/debug "EXIT domain-test-fixture"))


(use-fixtures :each domain-test-fixture)


;; Experimental testing.
;; (def my-atom (atom nil))


(deftest get-domain-data-test
  (log/debug "ENTER get-domain-data-test")
  (testing "Testing get domain data"
    (let [domain-data (b-domain/get-domain-data (-> (test-config/test-config) :backend/env :data-dir))
          ;;_ (reset! my-atom domain-data)
          ]
      (testing "Testing product groups"
        (let [product-groups (:product-groups domain-data)
              product-groups-len (count product-groups)
              right-vec [{:pgId 1, :name "Books"} {:pgId 2, :name "Movies"}]]
          (is (= product-groups-len 2))
          (is (= product-groups right-vec))))
      (testing "Testing products"
        (let [products (:products domain-data)
              products-len (count products)]
          (is (= products-len 204))))
      (testing "Testing specific product"
        (let [products (:products domain-data)
              product (b-domain/get-product 2 49 products)
              right-product {:pId 49,
                             :pgId 2,
                             :title "Once Upon a Time in the West",
                             :price 14.4,
                             :director "Leone, Sergio",
                             :year 1968,
                             :country "Italy-USA",
                             :genre "Western"}]
          (is (= product right-product))))))
  (testing "Using the domain data in the internal test system"
    (let [domain-data (:domain @(-> @test-config/test-system :backend/env :db))]
      (testing "Testing product groups"
        (let [product-groups (:product-groups domain-data)
              product-groups-len (count product-groups)
              right-vec [{:pgId 1, :name "Books"} {:pgId 2, :name "Movies"}]]
          (is (= product-groups-len 2))
          (is (= product-groups right-vec))))
      (testing "Testing products"
        (let [products (:products domain-data)
              products-len (count products)]
          (is (= products-len 204))))
      (testing "Testing specific product"
        (let [products (:products domain-data)
              product (b-domain/get-product 2 49 products)
              right-product {:pId 49,
                             :pgId 2,
                             :title "Once Upon a Time in the West",
                             :price 14.4,
                             :director "Leone, Sergio",
                             :year 1968,
                             :country "Italy-USA",
                             :genre "Western"}]
          (is (= product right-product))))))
  
  )


(comment
  (test-config/test-config)
  (-> (test-config/test-config) :backend/env :data-dir)
  ;; @my-atom
  ;; (keys @my-atom)
  ;; (:product-groups @my-atom)
  ;; (:products @my-atom)
  ;; (count (:products @my-atom))
  
  )