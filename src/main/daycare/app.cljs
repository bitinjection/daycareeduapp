(ns daycare.app
  "Application entry point. Deliberately bare until stage 7 adds Reagent, so a
  failure here can only come from the shadow-cljs / Metro / Expo seam."
  (:require
   ["react" :as react]
   ["react-native" :as rn]
   [shadow.expo :as expo]))

(defn root []
  (react/createElement
   rn/View
   #js {:style #js {:flex 1 :alignItems "center" :justifyContent "center"}}
   (react/createElement rn/Text nil "Hello from ClojureScript")))

(defn ^:dev/after-load start []
  (expo/render-root (root)))

(defn init []
  (start))
