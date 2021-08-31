(ns markdownify.core
  (:require [re-frame.core :as rf]
            [markdownify.views :as views]
            ["showdown" :as showdown]))

;; -- Domino 1 - Event Dispatch -----------------------------------------------


;; -- Domino 2 - Event Handlers -----------------------------------------------

(def default-db
  {:content nil
   :flash-message nil}) 

(rf/reg-event-fx
 :initialize
 [(rf/inject-cofx :showdown-converter)]
 (fn [_ _]
   {:db default-db
    :dispatch [:update-content {:format :md
                                :value "# Hello Markdown"}]}))

(rf/reg-event-fx
 :update-content
 [(rf/inject-cofx :showdown-converter)]
 (fn [cofx [_ {:keys [format value]}]]
   (prn format value)
   (prn cofx)
   (let [db (:db cofx)
         showdown-converter (:showdown-converter cofx)
         md (if (= format :md)
              value
              (.makeMarkdown showdown-converter value))
         html (if (= format :html)
                value
                (.makeHtml showdown-converter value))]
     {:db {:content {:md md :html html}}})));; compute and return the new application state

(rf/reg-event-db
  :update-flash-message
  [re-frame.core/debug]
  (fn [db [_ flash-message]]
    (assoc db :flash-message flash-message)))

;; -- cofx
(rf/reg-cofx
  :showdown-converter
  (fn [cofx _]
      ;; put the localstore todos into the coeffect under :local-store-todos
      (assoc cofx :showdown-converter (showdown/Converter.))))


;; -- Domino 4 - Query  -------------------------------------------------------

(rf/reg-sub
 :content
 (fn [db _]     ;; db is current app state. 2nd unused param is query vector
   (:content db))) ;; return a query computation over the application state

(rf/reg-sub
 :flash-message
 (fn [db _]
   (:flash-message db)))


;; -- Domino 5 - View Functions ----------------------------------------------

;; in  views.cljs


;; -- Entry Point -------------------------------------------------------------

(enable-console-print!)   ;; so that println writes to `console.log`

(defn mount! []
  (reagent.dom/render [markdownify.views/markdownify-app]
                      (js/document.getElementById "app")))

(defn ^:dev/after-load clear-cache-and-render!
  []
  ;; The `:dev/after-load` metadata causes this function to be called
  ;; after shadow-cljs hot-reloads code. We force a UI update by clearing
  ;; the Reframe subscription cache.
  (rf/clear-subscription-cache!)
  (mount!))

(defn main!
  []
  (rf/dispatch-sync [:initialize]) ;; put a value into application state
  (mount!))                        ;; mount the application's ui into '<div id="app" />'
