(ns travis-hook.handler
  (:use compojure.core)
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [ring.middleware.json :as middleware]
            [ring.adapter.jetty :refer (run-jetty)]
            [clojure.data.json :as json]
            [clj-http.client :as client])
  (:gen-class))

(def device-mapping {"pjg@touk.pl" "53ff6c065067544860381287", "bartek.zdanowski@gmail.com" "50ff76065067545641150387"})
(def status-mapping {1 "on", 0 "off"})

(def access-token "d193fc0b39482f9ed9513c139a6b0bbb7609563f")

(defn send-update [email status]
  (client/post (str "https://api.spark.io/v1/devices/" (get device-mapping email) "/led")
               {:headers {"Authorization" (str "Bearer " access-token)}
                :form-params {:args status}}))

(defn process-event [event]
  (let [status (:status event)
        email (:committer_email event)]
    (println (str "email " email))
    (println (str "status " status))
    (send-update (get status-mapping status))))

(defroutes app-routes
  (POST "/" {{payload :payload} :params}
        (println payload)
        (process-event (json/read-str payload :key-fn keyword))
        {:body {:message "OK"}})

  (GET "/jagiel/:status" [status]
       (send-update "pjg@touk.pl" status)
       {:body {:message "OK"}})

  (GET "/zdanek/:status" [status]
       (send-update "bartek.zdanowski@gmail.com" status)
       {:body {:message "OK"}})

  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (-> (handler/api app-routes)
      (middleware/wrap-json-response)))

(defn -main [& args]
  (run-jetty app {:port 3000 :join? false }))
