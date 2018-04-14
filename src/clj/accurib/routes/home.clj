(ns accurib.routes.home
  (:require [accurib.layout :as layout]
            [accurib.db.core]
            [compojure.core :refer [defroutes GET]]
            [ring.util.http-response :as response]
            [clojure.java.io :as io]
            [clojure-csv.core :as csv]
            [clj-time.core :as t]))

(defn home-page []
  (layout/render "home.html"))

(defn return-csv-data [filename]
  (let [data (slurp filename)]
    (csv/parse-csv data)))

(defonce zipcode-density-data (return-csv-data "resources/public/zips_to_density.csv"))
(defonce zipcode-income-data (return-csv-data "resources/public/zips_to_income.csv"))
(defonce processed-zipcode-data
  (let [density-map (reduce (fn [v [zipcode population land-sq-mi density]]
                              (assoc v zipcode {:zipcode zipcode
                                                :population population
                                                :land-sq-mi land-sq-mi
                                                :density density}))
                      {}
                      (rest zipcode-density-data))
        density-and-income-map (reduce (fn [v [zipcode income]]
                                         (assoc v zipcode (merge (get density-map zipcode) {:income income})))
                                 {}
                                 (rest zipcode-income-data))]
    density-and-income-map))

(defn month->text [m]
  (case m
    1 "January"
    2 "February"
    3 "March"
    4 "April"
    5 "May"
    6 "June"
    7 "July"
    8 "August"
    9 "September"
    10 "October"
    11 "November"
    12 "December"))

; source for futures: taken from "LAST" column of http://futures.tradingcharts.com/marketquotes/HE.html
; source for history: "PRICE" https://www.investing.com/commodities/lean-hogs-historical-data
; date updated: 4/13/2018
; [YYYY M Price]
(def monthly-pork-futures {2018 {1 73.22
                                 2 67.22
                                 3 57.25
                                 4 69.78
                                 5 69.700
                                 6 77.650
                                 7 79.575
                                 8 79.425
                                 9 73.888 ; not real
                                 10 68.350
                                 11 65.238
                                 12 62.125}
                           2019 {1 64.013 ; not real
                                 2 65.900
                                 3 67.713
                                 4 69.525
                                 5 74.050
                                 6 77.500}})

(defn parse-int [s]
  (Integer. (re-find #"[0-9]*" s)))

(defn calculate-density-multiplier [density]
  (if density
    (cond
      (> density 50000) 1.4
      (and (<= density 50000) (> density 20000)) 1.3
      (and (<= density 20000) (> density 5000)) 1.2
      (and (<= density 5000) (> density 0)) 1.1
      :else 1.0)
    1.0))

(defn calculate-income-multiplier [income]
  (if income
    (cond
      (> income 100000) 1.4
      (and (<= income 100000) (> income 75000)) 1.3
      (and (<= income 75000) (> income 50000)) 1.2
      (and (<= income 50000) (> income 0)) 1.1
      :else 1.0)
    1.0))

(defn likelihood-calc [year month]
  (let [prices monthly-pork-futures
        last-month (+ 1 (mod (- month 1) 12))
        last-month-year (if (<= (- month 1) 0) (- year 1) year)
        two-months-ago (+ 1 (mod (- month 2) 12))
        two-months-ago-year (if (<= (- month 2) 0) (- year 1) year)
        three-months-ago (+ 1 (mod (- month 3) 12))
        three-months-ago-year (if (<= (- month 3) 0) (- year 1) year)
        this-month-price (get-in prices [year month])
        last-month-price (get-in prices [last-month-year last-month])
        two-months-ago-price (get-in prices [two-months-ago-year two-months-ago])
        three-months-ago-price (get-in prices [three-months-ago-year three-months-ago])
        prices (filterv #(not (nil? %)) [last-month-price two-months-ago-price three-months-ago-price])
        three-month-average (/ (apply + prices) (count prices))
        months-in-a-row-factor (cond
                          (>= this-month-price last-month-price) 0.05
                          (>= last-month-price two-months-ago-price) 0.30
                          (>= two-months-ago-price three-months-ago-price) 0.70)]

    (if (> three-month-average this-month-price)
      (+ 0.25 months-in-a-row-factor)
      months-in-a-row-factor)))

(defn baseline-likelihood-by-month [current-year current-month]
  (loop [month (+ 1 (mod current-month 12))
         year current-year
         counter 0
         v []]
    (if (>= counter 5)
      v
      (recur
        (+ 1 (mod month 12))
        (if (= month 12) (+ 1 year) year)
        (+ 1 counter)
        (conj v [(month->text month) (likelihood-calc year month)])))))

(defn number-crunch [{:keys [zipcode population land-sq-mi density income] :as zipcode-data}]
  (println zipcode-data)
  (let [current-year (t/year (t/now))
        current-month (t/month (t/now))
        income (when income (parse-int income))
        density (when density (parse-int density))
        density-multiplier (calculate-density-multiplier density)
        per-capita-multiplier (calculate-income-multiplier income)
        baseline (baseline-likelihood-by-month current-year current-month)
        baseline-and-multipliers (mapv (fn [[first second]]
                                         [first (* second density-multiplier per-capita-multiplier)])
                                   baseline)
        _ (println baseline-and-multipliers)]
    baseline-and-multipliers))

(defn zipcode-crunch [req]
  (let [zipcode (:zipcode (get req :params))
        zipcode-data (get processed-zipcode-data zipcode)
        percent-likelihood (number-crunch zipcode-data)]
    {:status 200
     :body percent-likelihood}))

(defroutes home-routes
  (GET "/" []
    (home-page))
  (GET "/zipcodes/:zipcode" [zipcode] zipcode-crunch)
  (GET "/docs" []
    (-> (response/ok (-> "docs/docs.md" io/resource slurp))
      (response/header "Content-Type" "text/plain; charset=utf-8"))))

