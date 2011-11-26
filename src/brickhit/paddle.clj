(ns brickhit.paddle
  (:import (org.lwjgl.opengl Display DisplayMode GL11)
           (org.lwjgl.input Keyboard)
           (org.lwjgl Sys))
  (:require [brickhit.sprite :as sprite]))

(def DIRS {:left -1 :right 1 :none 0})

(defn move! [this dir]
  (dosync
   (ref-set this
    (assoc @this :xdir (dir DIRS)))))

(defn update! [this dt]
  (dosync
   (ref-set this
            (assoc @this :x
                   (sprite/clamp 0
                          (+ (:x @this) (* (:xdir @this) (:xspeed @this) dt))
                          (- 800 (:w @this)))))))
