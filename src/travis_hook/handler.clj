(ns travis-hook.handler
  (:use compojure.core)
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [ring.middleware.json :as middleware]
            [ring.adapter.jetty :refer (run-jetty)])
  (:gen-class))

(defroutes app-routes
  (POST "/" {{payload :payload} :params}
        (println payload)
        {:body {:message (str "Hello World")}})

  (GET "/messages/:name" [name] {:body {:message (str "Hello World" " " name)}})

  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (-> (handler/api app-routes)

      (middleware/wrap-json-response)))

(defn -main [& args]
  (run-jetty app {:port 3000 :join? false }))
