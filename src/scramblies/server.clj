(ns scramblies.server
  (:require
    [ring.adapter.jetty :as jetty]
    [integrant.core :as ig]
    [muuntaja.core :as m]
    [reitit.interceptor.sieppari :as sieppari]
    [reitit.ring :as ring]
    [reitit.http :as http]
    [reitit.coercion.malli]
    [reitit.http.coercion :as coercion]
    [reitit.http.interceptors.parameters :as parameters]
    [reitit.http.interceptors.muuntaja :as muuntaja]
    [reitit.http.interceptors.exception :as exception]
    [simple-cors.reitit.interceptor :as cors]
    [ring.util.http-response :as response]
    [ring.util.http-status :as status]
    [scramblies.api :as api]
    [scramblies.specs :as specs])
  (:import [org.eclipse.jetty.server Server]))

(def system-config
  {:scramblies/jetty   {:port    3000
                        :join?   false
                        :handler (ig/ref :scramblies/handler)}
   :scramblies/handler {}})

(defmethod ig/init-key :scramblies/jetty [_ {:keys [port join? handler]}]
  (println "starting server on port" port)
  (jetty/run-jetty handler {:port port :join? join?}))

(defmethod ig/halt-key! :scramblies/jetty [_ server]
  (.stop ^Server server))

(defmethod ig/init-key :scramblies/handler [_ _]
  (let [cors-config {:cors-config {:allowed-request-methods [:get]
                                   :allowed-request-headers ["Authorization" "Content-Type"]
                                   :allow-credentials?      true
                                   :origins                 "*"
                                   :max-age                 300}}]
    (http/ring-handler
      (http/router

        ["/scramble"
         {:get {:parameters {:query [:map [:s1 specs/ScrambleString] [:s2 specs/ScrambleString]]}
                :responses  {status/ok {:body [:map [:result boolean?]]}}
                :handler    (fn [{{{:keys [s1 s2]} :query} :parameters}]
                              (response/ok {:result (api/scramble? s1 s2)}))}}]

        {::http/default-options-endpoint (cors/make-default-options-endpoint cors-config)
         :data                           {:coercion     (reitit.coercion.malli/create
                                                          {:encode-error (fn [e] (:humanized e))})
                                          :muuntaja     m/instance
                                          :interceptors [;; query-params & form-params
                                                         (parameters/parameters-interceptor)
                                                         ;; content-negotiation
                                                         (muuntaja/format-negotiate-interceptor)
                                                         ;; encoding response body
                                                         (muuntaja/format-response-interceptor)
                                                         ;; exception handling
                                                         (exception/exception-interceptor)
                                                         ;; decoding request body
                                                         (muuntaja/format-request-interceptor)
                                                         ;; coercing response bodys
                                                         (coercion/coerce-response-interceptor)
                                                         ;; coercing request parameters
                                                         (coercion/coerce-request-interceptor)]}})

      (ring/create-default-handler)

      {:executor     sieppari/executor
       :interceptors [(cors/cors-interceptor cors-config)]})))

(defn -main []
  (ig/init system-config))