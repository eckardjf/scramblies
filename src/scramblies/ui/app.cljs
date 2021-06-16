(ns scramblies.ui.app
  (:require [reagent.core :as r]
            [reagent.dom :as rd]
            [fork.reagent :as fork]
            [ajax.core :refer [GET]]
            [malli.core :as m]
            [malli.error :as me]
            [scramblies.specs :as specs]))

(def app-db (r/atom {}))

(defn success-icon []
  [:svg.w-20.h-20.text-green-400.inline-block
   {:fill "currentColor" :viewBox "0 0 20 20" :xmlns "http://www.w3.org/2000/svg"}
   [:path {:fill-rule "evenodd" :d "M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" :clip-rule "evenodd"}]])

(defn failure-icon []
  [:svg.w-20.h-20.text-red-400.inline-block
   {:fill "currentColor" :viewBox "0 0 20 20" :xmlns "http://www.w3.org/2000/svg"}
   [:path {:fill-rule "evenodd" :d "M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" :clip-rule "evenodd"}]])

(defn error-icon []
  [:svg.w-20.h-20.text-yellow-400.inline-block
   {:fill "currentColor" :viewBox "0 0 20 20" :xmlns "http://www.w3.org/2000/svg"}
   [:path {:fill-rule "evenodd" :d "M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-8-3a1 1 0 00-.867.5 1 1 0 11-1.731-1A3 3 0 0113 8a3.001 3.001 0 01-2 2.83V11a1 1 0 11-2 0v-1a1 1 0 011-1 1 1 0 100-2zm0 8a1 1 0 100-2 1 1 0 000 2z" :clip-rule "evenodd"}]])

(defn set-errors-from-response
  "use the set-error helper to convert api errors to fork server errors"
  [state path response]
  (reduce-kv (fn [m k v]
               (fork/set-error m path (name k) v))
             state
             response))

(defn submit [{:keys [state path values]}]
  (swap! state #(fork/set-submitting % path true))
  (GET "http://localhost:3000/scramble"
       {:params        values
        :handler       (fn [response]
                         (swap! state #(-> (fork/set-submitting % path false)
                                           (assoc :response response))))
        :error-handler (fn [{:keys [response] :as error-response}]
                         (swap! state #(-> (fork/set-submitting % path false)
                                           (set-errors-from-response path response)
                                           (assoc :response error-response))))}))

(defn validate [values]
  (reduce (fn [errors field-name]
            (let [schema specs/ScrambleString
                  value (get values field-name)]
              (if-not (m/validate schema value)
                (assoc errors field-name (me/humanize (m/explain schema value))))))
          {}
          ["s1" "s2"]))

(defn clear-server-errors [state path input-name]
  (swap! state update-in (conj path :server) dissoc input-name))

(defn clear-response [state]
  (swap! state dissoc :response))

(defn string-input
  [{:keys [handle-change handle-blur state path values touched errors server-errors]}
   {:keys [name placeholder]}]
  (let [field-errors (when (touched name)
                       (or (get errors name)
                           (get server-errors name)))]
    [:div
     [:input.field.w-full
      {:name        name
       :type        "text"
       :placeholder placeholder
       :value       (get values name)
       :class       [(when field-errors "border-red-600")]
       :on-blur     handle-blur
       :on-change   (fn [e]
                      (handle-change e)
                      (clear-server-errors state path name)
                      (clear-response state))}]
     [:div.text-sm.text-red-600
      (for [fe field-errors]
        ^{:key fe} [:div fe])]]))

(defn app [db]
  (let [response (r/cursor db [:response])]
    [:div.w-96.mx-auto.my-32
     [fork/form {:state            db
                 :path             [:scramble]
                 :prevent-default? true
                 :validation       validate
                 :on-submit        submit}
      (fn [{:keys [submitting?
                   handle-submit
                   reset] :as props}]
        [:form {:on-submit handle-submit}
         [:fieldset.space-y-6 {:disabled submitting?}
          [string-input props {:name "s1" :placeholder "String 1"}]
          [string-input props {:name "s2" :placeholder "String 2"}]
          [:div.flex.space-x-6
           [:button.btn.btn-blue.flex-1 {:type "reset" :on-click #(reset)} "Clear"]
           [:button.btn.btn-purple.flex-1 {:type "submit"} "Scramble"]]
          [:div.text-center.w-full
           (cond
             (contains? @response :result)
             (if (:result @response)
               [success-icon]
               [failure-icon])

             (contains? @response :failure)
             [:<>
              [error-icon]
              [:div.text-yellow-500 (:status-text @response)]])]]])]]))

(defn ^:dev/after-load start []
  (rd/render [app app-db] (.getElementById js/document "app")))

(defn ^:export init []
  (start))