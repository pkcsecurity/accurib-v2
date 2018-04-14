(ns accurib.home
  (:require [reagent.core :as r]
            [accurib.forecast :as forecast]
            [ajax.core :refer [GET]]))

(defn handler [res]
  (reset! forecast/forecast-data res))

(defn get-data [zipcode]
  (GET (str "/zipcodes/" zipcode) {:handler handler}))

(defn disabled? [zip-code]
  (not= 5 (count zip-code)))

(defn button [button-clicked? zip-code]
  (let [hover? (r/atom false)]
    (fn []
      [:div.p2.center.caps.h5
       {:style (merge {:color (if (disabled? @zip-code) "#ccc" (when-not @hover? "#777"))
                       :cursor "pointer"
                       :border "1px solid"
                       :box-shadow (when-not (disabled? @zip-code) "6px 6px 18px -6px rgba(61,61,61,0.85)")
                       :transition "all 0.5s"}
                 (when @hover? {:box-shadow "12px 12px 18px -6px rgba(61,61,61,0.85)"}))
        :class (when @hover? "pri")
        :on-mouse-enter #(when-not (disabled? @zip-code) (swap! hover? not))
        :on-mouse-leave #(when-not (disabled? @zip-code) (swap! hover? not))
        :on-click #(when-not (disabled? @zip-code)
                     (swap! button-clicked? not)
                     (get-data @zip-code)
                     (reset! zip-code ""))}
       "Click here to find out!"])))

(defn home-page []
  (let [button-clicked? (r/atom false)
        zip-code (r/atom "")]
    (fn []
      [:div.container
       [:div.flex.flex-column.items-center
        (if @button-clicked?
          [:div
           [:div.h1.center.pt4 {:style {:font-weight 100}}
            "Your Accurib forecast"]
           [forecast/forecast button-clicked? zip-code]]
          [:div
           [:div.h1.center.pt4 {:style {:font-weight 100}}
            "What's the chance of McRib?"]
           [:div.flex.flex-column.items-stretch
            [:input.p2.mt4.h3 {:on-change #(reset! zip-code (.. % -target -value))
                               :style {:border "1px solid"
                                       :border-color "#ccc"
                                       :font-weight 100
                                       :letter-spacing ".2em"}
                               :placeholder "Enter 5-digit zip code"
                               :class (when-not (clojure.string/blank? @zip-code) "center")}]
            [:div.mt4
             [button button-clicked? zip-code]]]])]])))