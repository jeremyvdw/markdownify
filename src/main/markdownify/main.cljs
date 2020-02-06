(ns markdownify.main
  (:require [reagent.core :as reagent]
            ["showdown" :as showdown]))

(defonce showdown-converter 
  (showdown/Converter.))

(defn md->html [md]
  (.makeHtml showdown-converter md))

(defn html->md [html]
  (.makeMarkdown showdown-converter html))

(defonce text-state (reagent/atom {:format :md 
                                   :value ""}))

(defn ->md [{:keys [format value]}]
  (case format
    :md value
    :html (html->md value)))

(defn ->html [{:keys [format value]}]
  (case format
    :md (md->html value)
    :html value))

;; https://hackernoon.com/copying-text-to-clipboard-with-javascript-df4d4988697f
(defn copy-to-clipboard [s]
  (let [el (.createElement js/document "textarea")
        selected (when (pos? (-> js/document .getSelection .-rangeCount))
                   (-> js/document .getSelection (.getRangeAt 0)))]          
    (set! (.-value el) s)
    (.setAttribute el "readonly" "")
    (set! (-> el .-style .-position) "absolute")
    (set! (-> el .-style .-left) "-9999px")
    (-> js/document .-body (.appendChild el))
    (.select el)
    (.execCommand js/document "copy")
    (-> js/document .-body (.removeChild el))
    (when selected
      (-> js/document .getSelection .removeAllRanges)
      (-> js/document .getSelection (.addRange selected)))))

(defn app []
  [:div 
   [:h1 "Markdownify"]
   [:div 
    {:style {:display :flex}}
    [:div
     {:style {:flex "1"}}
     [:h2 "Markdown"]
     [:textarea 
      {:on-change (fn [e]
                    (reset! text-state {:format :md 
                                        :value (-> e .-target .-value)}))
       :value (->md @text-state)
       :style {:resize "none"
               :width "100%"
               :height "500px"}}]
     [:button
      {:on-click #(copy-to-clipboard (->md @text-state))}
      "Copy Markdown"]]

    [:div
     {:style {:flex "2"}}
     [:h2 "HTML"]
     [:textarea 
      {:on-change (fn [e]
                    (reset! text-state {:format :html 
                                        :value (-> e .-target .-value)}))
       :value (->html @text-state)
       :style {:resize "none"
               :width "100%"
               :height "500px"}}]
     [:button
      {:on-click #(copy-to-clipboard (->html @text-state))}
      "Copy HTML"]]

    [:div
     {:style {:flex "3"}}
     [:h2 "HTML preview"]
     [:div 
      {:style {:height "500px"}
       :dangerouslySetInnerHTML {:__html (->html @text-state)}}]]]])

(defn mount! []
  (reagent/render [app]
                  (.getElementById js/document "app")))

(defn main! []
  (mount!))

(defn reload! []
  (mount!))
