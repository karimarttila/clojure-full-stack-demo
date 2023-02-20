(ns backend.db.domain
  "Loads the domain data from csv"
  (:require
   [clojure.tools.logging :as log]
   [clojure.data.csv :as csv]
   [clojure.java.io :as io]
   [clojure.string :as str])
  (:import (java.io FileNotFoundException)))

(defn get-product-groups [data-dir]
  (log/debug (str "ENTER load-product-groups"))
  (let [raw (with-open [reader (io/reader (str data-dir "/product-groups.csv"))]
              (doall
               (csv/read-csv reader)))
        ;;_ #p raw
        ]
    (into [] (map
              (fn [[item]]
                (let [[pgId name] (str/split item #"\t")]
                  {:pgId (Integer/parseInt pgId) :name name}))
              raw))))


(defn get-raw-products [data-dir pg-id]
  (log/debug (str "ENTER get-raw-products, pg-id: " pg-id))
  (let [raw-products (try
                       (with-open [reader (io/reader (str data-dir "/pg-" pg-id "-products.csv"))]
                         (doall
                          (csv/read-csv reader :separator \tab)))
                       (catch FileNotFoundException _ nil))
        ;_ #p raw-products
        ]
    raw-products))


(defn create-book [item]
  (try
    {:pId (Integer/parseInt (get item 0))
     :pgId (Integer/parseInt (get item 1))
     :title (get item 2)
     :price (Double/parseDouble (get item 3))
     :author (get item 4)
     :year (Integer/parseInt (get item 5))
     :country (get item 6)
     :language (get item 7)}
    (catch Exception e {:msg (str "Exception: " (.getMessage e))
                        :item item})))


(defn create-movie [item]
  (try
    {:pId (Integer/parseInt (get item 0))
     :pgId (Integer/parseInt (get item 1))
     :title (get item 2)
     :price (Double/parseDouble (get item 3))
     :director (get item 4)
     :year (Integer/parseInt (get item 5))
     :country (get item 6)
     :genre (get item 7)}
    (catch Exception e {:msg (str "Exception: " (.getMessage e))
                        :item item})))


(defn convert-products [raw-products]
  (map (fn [item]
         (if (= (count item) 8)
           (let [pg-id (Integer/parseInt (get item 1))]
             (cond
               (not= (count item) 8) nil
               (= pg-id 1) (create-book item)
               (= pg-id 2) (create-movie item)
               :else nil))
           nil)
         )
       raw-products))


(defn get-domain-data [data-dir]
  (let [product-groups (get-product-groups data-dir)
        ;_ #p product-groups
        pg-ids (map (fn [item] (:pgId item)) product-groups)
        ;_ #p pg-ids
        raw-products (mapcat (fn [pg-id]
                               (get-raw-products data-dir pg-id)) pg-ids)
        ;_ #p raw-products
        products (convert-products raw-products)]
    {:product-groups product-groups
     :products products}))

(defn get-product [pgId pId products]
  (first (filter (fn [item] (and (= (:pgId item) pgId) (= (:pId item) pId))) products)))


(comment
  (def my-data-dir "resources/data")
  (def my-product-groups (get-product-groups my-data-dir))
  my-product-groups
  (def my-pg-ids (map (fn [item] (:pgId item)) my-product-groups))
  my-pg-ids
  (def my-raw-products (mapcat (fn [pg-id]
                                 (get-raw-products my-data-dir pg-id)) my-pg-ids))
  my-raw-products
  (count (first my-raw-products))
  (convert-products my-raw-products)
  
  (get-domain-data "resources/data")
  (keys (get-domain-data "resources/data"))
  (count (:products (get-domain-data "resources/data")))
  (get-product-groups "resources/data")
  (get-raw-products "resources/data" 1)
  
  (let [products (:products (get-domain-data "resources/data"))]
    (first (filter (fn [item] (and (= (:pgId item) 2) (= (:pId item) 49))) products)))
  
  (get-product 2 49 (:products (get-domain-data "resources/data")))
  
  )



