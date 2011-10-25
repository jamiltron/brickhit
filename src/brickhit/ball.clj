(ns brickhit.ball
  (:import (org.lwjgl.opengl Display DisplayMode GL11))
  (:require [brickhit.util :as putil]))

(defn update [this dt & others]
  (let [col (if (false? (:collided this)) (apply putil/collision-list (cons this others))
                [])
        edge (if (not (empty? col)) (putil/colliding-edge this (first col))
                 false)
        xd (cond
            (= :left edge) -1
            (= :right edge) 1
            :else (:xdir this))
        yd (cond
            (= :top edge) -1
            (= :bottom edge) 1
            :else (:ydir this))
        nx (cond
            (= :left edge) (- (:x (first col)) (:w this))
            (= :right edge) (inc (+ (:x (first col)) (:w (first col))))
            :else (:x this))
        ny (cond
            (= :top edge) (- (:y (first col)) (:h this))
            (= :bottom edge) (inc (+ (:y (first col)) (:h (first col))))
            :else (:y this))       
        ncol (if (and (not (empty? col)) (not (:collided this))) true
                 false)]
    (assoc this :x (+ nx (* xd (:xspeed this) dt))
                :y (+ ny (* yd (:yspeed this) dt))
                :xdir xd
                :ydir yd
                :collided ncol)))
  
            
                      
        
       

