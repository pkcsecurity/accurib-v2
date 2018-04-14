(ns accurib.new-sighting
  (:require [reagent.core :as r]))

(defn experience-box [img text responded?]
  (let [hover? (r/atom false)]
    (fn []
      [:div.col-6.m1.flex.flex-column.justify-between
       {:style (merge
                 {:cursor :pointer
                  :width "150px"
                  :height "150px"
                  :border "1px solid"
                  :border-color "#d8d8d8"
                  :box-shadow "6px 6px 18px -6px rgba(61,61,61,0.85)"
                  :transition "all 0.5s"}
                 (when @hover? {:box-shadow "12px 12px 18px -6px rgba(61,61,61,0.85)"}))
        :on-mouse-enter #(swap! hover? not)
        :on-mouse-leave #(swap! hover? not)
        :on-click #(swap! responded? not)}
       [:div {:style {:height "85px"}}
        [:img.fit {:src (str "/img/" img)}]]
       [:div.center.pb2.h5 text]])))

(defn new-sighting-page []
  (let [responded? (r/atom false)]
    (fn []
      [:div.container
       [:div.flex.flex-column.items-center
        [:div.h1.center.pt4 {:style {:font-weight 100}}
         "Where did you experience the McRib?"]
        (if @responded?
          [:div.p4.mt4.flex.flex-column.items-center.center
           [:div.h1 {:style {:font-weight 700}}
            "Woot! Thanks for responding!"]
           [:div.h6 "Your information is now in the custody of Carls A Jr"]]
          [:div.mt3
           [:div.flex.flex-row
            [experience-box "mcrib.png" "I ate delicious McRib" responded?]
            [experience-box "billboard.png" "I saw a billboard!" responded?]]
           [:div.flex.flex-row
            [experience-box "party2.png" "At an event" responded?]
            [experience-box "ground.png" "It was on the ground" responded?]]])]])))