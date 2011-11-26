(ns brickhit.timer
  (:import (org.lwjgl Sys)))

(def ticks-per-second (Sys/getTimerResolution))

(defn milliseconds->seconds
  "Convert from milliseconds to seconds"
  [x]  
  (* x 1000))

(defn get-time
  "Return the system time in ticks."
  []
  (/ (milliseconds->seconds (Sys/getTime)) ticks-per-second))

(def timer (atom (get-time)))

(defn update-timer!
  "Update the timer with the current time, and return the delta since last timer-update."
  []
  (let [t  (get-time)
        dt (- t @timer)
        _ (reset! timer t)]
        dt))

(defn reset-timer! []
  (reset! timer (get-time)))