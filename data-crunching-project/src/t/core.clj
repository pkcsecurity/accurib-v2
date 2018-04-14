(ns t.core
  (:require [cheshire.core :refer :all]
            [clojure-csv.core :as csv])
  (:gen-class))

(defn sighting-write [{:keys [latitude longitude time]}]
  (spit "resources/mcribs.csv" (str "\n" "1," latitude "," longitude, "," time) :append true))

; mcribs.json data is November-December 2017 ONLY.
; source: http://mcriblocator.com/map.html, then clicking "Search McRib Sitings".
; notes: the API will only return 500 entries.
(defn mcrib-json->csv []
  (let [s (slurp "resources/mcribs.json")
        sightings (parse-string s true)]
    (mapv sighting-write sightings)))

(defn return-csv-data [filename]
  (let [data (slurp filename)]
    (csv/parse-csv data)))

(defonce household-income-data (return-csv-data "resources/zcta_household_income_2016.csv"))
(defonce zip->zcta (return-csv-data "resources/zip_to_zcta_2017.csv"))

; source: https://www.udsmapper.org/zcta-crosswalk.cfm
; notes: many zipcodes to one zcta
(defn zcta->householdincome []
  (let [cleaned-list (filterv seq (mapv (fn [[zcta5-dirty median-household-income-dirty]]
                                          (let [zcta5-clean (subs zcta5-dirty 6)
                                                median-household-income-clean (if (= "-" median-household-income-dirty)
                                                                                nil
                                                                                (if (re-find #"\+" median-household-income-dirty)
                                                                                  250000
                                                                                  median-household-income-dirty))]
                                            (when median-household-income-clean
                                              [zcta5-clean median-household-income-clean])))
                                    (rest household-income-data)))]
    (reduce
      (fn [v [zcta5 household-income]]
        (assoc v zcta5 household-income))
      {}
      cleaned-list)))

(defn zips []
  (mapv (fn [row]
          (let [zip (first row)
                zcta (nth row 4)]
            [zip zcta]))
    (rest zip->zcta)))

(defn -main []
  #_(mcrib-json->csv)
  (let [zcta5->income-map (zcta->householdincome)
        zip->zcta-list (zips)]
    (spit "resources/zip_to_income.csv" "ZIP,INCOME\n" :append true)
    (doseq [[zip zcta] zip->zcta-list]
      (spit "resources/zip_to_income.csv" (str zip "," (get zcta5->income-map zcta) "\n") :append true))))