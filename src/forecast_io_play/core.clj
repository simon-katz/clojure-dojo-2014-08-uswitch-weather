(ns forecast-io-play.core
  (:use [clojure.string :only [join]]
        [environ.core]
        [incanter core stats charts datasets])
  (:require [cheshire.core :as json]
            [clj-http.client :as client]))

(defn forecast
  "Retrieve the forecast for a given latitude and longitude"
  [lat lon & {:keys [params time]}]
  (let [base-url "https://api.forecast.io/forecast"
        api-key "PUT YOUR API KEY HERE" ; (env :forecast-key)
        url (join "/" [base-url api-key (join "," (filter #(not-empty %) (map str [lat lon time])))])
        response (client/get url {:query-params params :throw-exceptions false})]
    (cond (= 200 (:status response))
          (json/parse-string (:body response) true))))

(comment
  (forecast "37.8267" "-122.423")

  (forecast "51.5072" "0.1275")

  (forecast "37.8267" "-122.423" :time 1362974400)
  (forecast "37.8267" "-122.423" :params {:units "si" :exclude "daily,alerts" :callback "..."}))

(def london-forecast (forecast "51.5072" "0.1275"))
(def london-forecast-si (forecast "51.5072" "0.1275" :params {:units "si"}))

(def london-temps (map :temperature
                       (get-in london-forecast-si [:hourly :data])))

(view (histogram london-temps)) ; frequencies of each temperature

(view (bar-chart (range (count london-temps))
                 london-temps
                 :legend false
                 :x-label (str "Next " (count london-temps) " hours")
                 :y-label "Temperature (degrees C)"
                 :title "London Forecast"))
