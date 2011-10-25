(ns brickhit.core
  (:import (org.lwjgl.opengl Display DisplayMode GL11)
           (org.lwjgl.input Keyboard)
           (org.lwjgl Sys))
  (:require [brickhit.paddle :as paddle]
            [brickhit.ball :as ball]
            [brickhit.util :as putil]))

(def ticks-per-second (Sys/getTimerResolution))
(defn get-time []
  (/ (* (Sys/getTime) 1000) ticks-per-second))
(def WIDTH 800)
(def HEIGHT 600)
(def player (ref {:x (/ 672 2) :y 536}))
(def ball (ref {:x (/ 784 2) :y 518 :xdir -1 :ydir -1 :xspeed 0.35 :yspeed 0.35 :collided false}))
(def l-wall {:x -32 :y 0 :w 32 :h HEIGHT})
(def r-wall {:x WIDTH :y 0 :w 32 :h HEIGHT})
(def t-wall {:x 0 :y -64 :w WIDTH :h 64})
(def timer (atom (get-time)))

(defn init-gl [width height]
  (do
    (Display/setDisplayMode (new DisplayMode width height))
    (Display/setTitle "Brickhit: Clojure + LWJGL")
    (Display/setFullscreen false)
    (Display/create)
    (Display/setVSyncEnabled true)

    (GL11/glEnable GL11/GL_TEXTURE_2D)
    (GL11/glShadeModel GL11/GL_SMOOTH)
    (GL11/glDisable GL11/GL_DEPTH_TEST)
    (GL11/glDisable GL11/GL_LIGHTING)
    (GL11/glClearColor 0.0 0.0 0.0 0.0)
    (GL11/glClearDepth 1)
    (GL11/glMatrixMode GL11/GL_PROJECTION)
    (GL11/glLoadIdentity)
    (GL11/glOrtho 0 width height 0 1 -1)
    (GL11/glMatrixMode GL11/GL_MODELVIEW)
    (GL11/glViewport 0 0 width height)))

(defn update-ref [r f]
  (dosync
   (ref-set r f)))

(defn render []
  (putil/draw @player)
  (putil/draw @ball))

(defn clean-up []
  (Display/destroy)
  (System/exit 0))

(defn handle-input [dt]
  (when (Keyboard/isKeyDown Keyboard/KEY_LEFT)
    (update-ref player (paddle/move @player -0.35 (- WIDTH 128) dt)))
  (when (Keyboard/isKeyDown Keyboard/KEY_RIGHT)
    (update-ref player (paddle/move @player 0.35 (- WIDTH 128) dt)))
  (when (Keyboard/isKeyDown Keyboard/KEY_ESCAPE)
    (clean-up)))

(defn update-timer! []
  (let [t (get-time)
        dt (- t @timer)]
    (do (reset! timer t)
        dt)))

(defn update-game []
  (let [dt (update-timer!)]
    (handle-input dt)
    (update-ref ball (ball/update @ball dt @player l-wall r-wall t-wall))))

(defn init-textures []
  (update-ref player (putil/load-image @player "res/paddle.png"))
  (update-ref ball (putil/load-image @ball "res/ball.png")))

(defn main-loop []
  (if (. Display isCloseRequested) (clean-up)
      (do
        (GL11/glClear (or GL11/GL_COLOR_BUFFER_BIT GL11/GL_DEPTH_BUFFER_BIT))
        (update-game)
        (render)
        (. Display update)
        (Display/sync 60)
        (recur))))


(defn -main []
  (do
    (init-gl WIDTH HEIGHT)
    (init-textures)
    (main-loop)))