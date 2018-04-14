(ns accurib.forecast
  (:require [reagent.core :as r]))

(def forecast-data (r/atom []))

(defn month-card [[month likelihood]]
  [:div.flex.flex-row.items-center.col-12.p1 {:style {:border-bottom "1px solid"
                                                      :border-color "#ccc"}}
   [:div.col-8
    [:div {:style {:font-size "2em"
                   :font-weight "100"
                   :color "#333"}}
     month]
    [:div.h6.italic {:style {:color "#777"}}
     (cond
       (>= likelihood 0.75) (str "IT'S RAINING MCRIBS! " (Math/round (* likelihood 100)) "% chance it'll happen. That's like guaranteed.")
       (and (< likelihood 0.75) (>= likelihood 0.50)) (str (Math/round (* likelihood 100)) "% chance for the McRib. This could be good.")
       (and (< likelihood 0.50) (>= likelihood 0.25)) (str (Math/round (* likelihood 100)) "% chance for the McRib. Not looking good.")
       (< likelihood 0.25) (str "Nagganna, Naggana Happen. " (Math/round (* likelihood 100)) "% chance for the McRib.")
       :else (str (Math/round (* likelihood 100)) "% chance for the McRib"))]]
   [:img.fit.col-4 {:src (cond
                           (>= likelihood 0.75) "/img/75-100.png"
                           (and (< likelihood 0.75) (>= likelihood 0.50)) "/img/50-75.png"
                           (and (< likelihood 0.50) (>= likelihood 0.25)) "/img/25-50.png"
                           (< likelihood 0.25) "/img/0-25.png"
                           :else "/img/mcrib.png")}]])

(defn button-go-back [button-clicked?]
  (let [hover? (r/atom false)]
    (fn []
      [:div.my4.p2.center.caps.h5
       {:style (merge {:cursor "pointer"
                       :border "1px solid"
                       :box-shadow "6px 6px 18px -6px rgba(61,61,61,0.85)"
                       :transition "all 0.5s"}
                 (when @hover? {:box-shadow "12px 12px 18px -6px rgba(61,61,61,0.85)"}))
        :class (when @hover? "pri")
        :on-mouse-enter #(swap! hover? not)
        :on-mouse-leave #(swap! hover? not)
        :on-click #(swap! button-clicked? not)}
       "Go back"])))

(defn forecast [button-clicked? zip-code]
  [:div.flex.flex-column.pt4.col-12
   [:div {:style {:border "1px solid"
                  :border-color "#d8d8d8"
                  :box-shadow "8px 20px 24px -2px rgba(61,61,61,0.54)"}}
    (for [month @forecast-data]
      ^{:key month} [month-card month])]
   [button-go-back button-clicked?]])