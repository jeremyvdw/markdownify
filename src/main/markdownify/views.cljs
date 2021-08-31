(ns markdownify.views
  (:require [re-frame.core :refer [subscribe dispatch]]
            ["showdown" :as showdown]))

(defn flash-box []
  [:div
   {:id "flash-message"}
   @(subscribe [:flash-message])
   (do (js/setTimeout #(dispatch [:update-flash-message ""]) 3000)
       nil)])

(defn content-symbol->content-text [sym]
  (case sym 
    :md "Markdown"
    :html "HTML"))

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

(defn input-box [format]
  (let [content-text (content-symbol->content-text format)]
    [:div
     {:style {:display :flex}}
     [:div
      {:style {:flex "1"}}
      [:h2 content-text]
      [:textarea
       {:value (->> @(subscribe [:content])
                    format)
        :on-change #(dispatch [:update-content {:format format
                                                :value (-> % .-target .-value)}])
        :style {:resize "none"
                :width "100%"
                :height "500px"}}]
      [:button
       {:on-click (fn []
                    (copy-to-clipboard (->> @(subscribe [:content])
                                            format))
                    (dispatch [:update-flash-message (str content-text " copied to clipboard")]))}
       (str "Copy" content-text)]]]))

(defn footer-box []
  [:div
   {:style {:flex "3"}}
   [:h2 "HTML preview"]
   [:div
    {:style {:height "500px"}
     :dangerouslySetInnerHTML {:__html (:html @(subscribe [:content]))}}]])

(defn markdownify-app []
  [:div
   [flash-box]
   [:h1 "Markdownify"]
   [input-box :md]
   [input-box :html]
   [footer-box]])
