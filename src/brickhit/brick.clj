(ns brickhit.brick
  (:import (org.lwjgl.opengl Display DisplayMode GL11)
           (org.lwjgl.input Keyboard)
           (org.lwjgl Sys))
  (:require [brickhit.sprite :as sprite]))

(defn hit! [this]
  (dosync
   (ref-set this
            {:living false :x -100 :y -100 })))
