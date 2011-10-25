(ns brickhit.paddle
  (:import (org.lwjgl.opengl Display DisplayMode GL11))
  (:require [brickhit.util :as putil]))

(defn move [this xspeed max-x dt]
  (assoc this :x (putil/clamp 0 (+ (:x this) (* xspeed dt)) max-x)))