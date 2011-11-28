(ns brickhit.ball
  (:import (org.lwjgl.opengl Display DisplayMode GL11)
           (org.lwjgl.input Keyboard)
           (org.lwjgl Sys))
  (:require [brickhit.sprite :as sprite]))

(defn set-position [this other edge]
  (let [x (cond
           (= edge :left) (- (:x @other) (:w @this))
           (= edge :right) (+ (:x @other) (:w @other))
           :else (:x @this))
        y (cond
           (= edge :top) (- (:y @other) (:h @this))
           (= edge :bottom) (+ (:y @other) (:h @other))
           :else (:y @this))]
    {:x x :y y}))

(defn reflect [this other edge]
  (let [ydir (if (or (= edge :top)
                     (= edge :bottom)
                     (= edge :corner)) (* (:ydir @this) -1)
                  (:ydir @this))
        xdir (cond
               (= edge :corner) (* (:xdir @this) -1)
               (= edge :left) -1
               (= edge :right) 1
               (= (:type @other) :paddle) (if (not= (:xdir @other) 0) (:xdir @other)
                                              (:xdir @this))
               :else (:xdir @this))]
    {:xdir xdir :ydir ydir}))
    

(defn update! [this others dt]
  (let [col (sprite/collision-list sprite/aabb? this others)]
    (if (not (empty? col))
      (let
          [colmap (first col)
           edge (sprite/colliding-edge this colmap)
           pos (set-position this colmap edge)
           dir (reflect this colmap edge)]
        (dosync
         ;; I should write a function to handle this DRY
         (ref-set this
                  (assoc @this
                    :x (+ (:x pos) (* (:xdir dir) (:xspeed @this) dt))
                    :y (+ (:y pos) (* (:ydir dir) (:yspeed @this) dt))
                    :xdir (:xdir dir)
                    :ydir (:ydir dir))))
        colmap)

      ;; else
      (do
        (dosync
         (ref-set this
                  (assoc @this
                    :x (+ (:x @this) (* (:xdir @this) (:xspeed @this) dt))
                    :y (+ (:y @this) (* (:ydir @this) (:yspeed @this) dt)))))
        nil))))
