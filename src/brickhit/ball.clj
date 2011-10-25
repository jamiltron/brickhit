(ns brickhit.ball
  (:import (org.lwjgl.opengl Display DisplayMode GL11))
  (:require [brickhit.util :as putil]))

(defn update [this dt & others]
  (let [col (apply putil/collision-list (cons this others))
        xd (if (and (not (empty? col))
                    (or (= :left (putil/colliding-edge this (first col)))
                        (= :right (putil/colliding-edge this (first col))))) (* -1 (:xdir this))
                        (:xdir this))
        yd (if (and (not (empty? col))
                    (or (= :top (putil/colliding-edge this (first col)))
                        (= :bottom (putil/colliding-edge this (first col))))) (* -1 (:ydir this))
                        (:ydir this))]
      (assoc this :x (+ (:x this) (* xd (* (:xspeed this) dt)))
             :y (+ (:y this) (* yd (* (:yspeed this) dt)))
             :xdir xd
             :ydir yd)))

