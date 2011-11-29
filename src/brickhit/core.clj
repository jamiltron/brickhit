(ns brickhit.core
  (:import (org.lwjgl.opengl Display DisplayMode GL11)
           (org.lwjgl.input Keyboard)
           (org.lwjgl Sys)
           (org.newdawn.slick Color TrueTypeFont)
           (java.awt Font)
           (java.io InputStream))
  (:require   [brickhit.sprite :as sprite]
              [brickhit.timer :as timer]
              [brickhit.paddle :as paddle]
              [brickhit.ball :as ball]))

;; game constants
(def FPS 60)
(def WIDTH 800)
(def HEIGHT 600)

;; game entities
(def playing? (atom :paused))
(def awtFont (new Font "Courier New" (Font/BOLD) 24))
(def awtFontSmaller (new Font "Courier New" (Font/BOLD) 14))

(def player (ref {:x 336
                  :y 536
                  :xdir 0
                  :xspeed 0.40
                  :yspeed 0
                  :type :paddle}))

(def ball (ref {:x 384
                :y 520
                :xdir 1
                :ydir 1
                :xspeed 0.30
                :yspeed 0.30
                :type ball}))

(def a-brick (ref {}))

(def bricks (ref []))

;; walls
(def l-wall (ref {:x -32 :y -32 :w 32 :h (+ HEIGHT 32) :type :wall}))
(def r-wall (ref {:x WIDTH :y -32 :w 32 :h (+ HEIGHT 32) :type :wall}))
(def t-wall (ref {:x -32 :y -32 :w (+ WIDTH 32) :h 32 :type :wall}))
(def b-wall (ref {:x -32 :y HEIGHT :w (+ WIDTH 32) :h 32 :type :wall}))


(defn init-gl [width height]
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
  (GL11/glEnable  GL11/GL_BLEND)
  (GL11/glBlendFunc GL11/GL_SRC_ALPHA GL11/GL_ONE_MINUS_SRC_ALPHA)
  (GL11/glClearDepth 1)
  (GL11/glMatrixMode GL11/GL_PROJECTION)
  (GL11/glLoadIdentity)
  (GL11/glOrtho 0 width height 0 1 -1)
  (GL11/glMatrixMode GL11/GL_MODELVIEW)
  (GL11/glViewport 0 0 width height)
  (def font (new TrueTypeFont awtFont true))
  (def font2 (new TrueTypeFont awtFontSmaller true)))

(defn init-textures []
  (dosync
   (ref-set player (sprite/load-image @player "res/paddle.png"))
   (ref-set ball (sprite/load-image @ball "res/ball.png"))
   (ref-set a-brick (sprite/load-image @a-brick "res/brick.png"))))

(defn init-level []
  (dosync
   (ref-set bricks
            (into [] (for [x (range 16 736 (:w @a-brick))
                           y (range 32 200 (:h @a-brick))]
                       (ref {:x x, :y y, :texture (:texture @a-brick),
                             :w (:w @a-brick), :h (:h @a-brick),
                             :type :brick, :living true}))))))

(defn render []
  (sprite/draw @player)
  (sprite/draw @ball)
  (doseq [brick @bricks]
    (sprite/draw @brick))
  (.drawString font2 0 0 "P to pause, ESC to quit" (Color/white)))

(defn clean-up []
  (do
    (Display/destroy)
    (System/exit 0)))

(declare paused-state)

(defn handle-quit []
  (when (Keyboard/isKeyDown Keyboard/KEY_ESCAPE) (clean-up)))

(defn handle-pause []
  (when (Keyboard/isKeyDown Keyboard/KEY_P) (reset! playing? :paused)))

(defn handle-unpause []
  (when (Keyboard/isKeyDown Keyboard/KEY_SPACE) (do
                                               (reset! playing? :unpaused)
                                               (timer/reset-timer!))))

(defn handle-input []
  (when (Keyboard/isKeyDown Keyboard/KEY_LEFT) (paddle/move! player :left))
  (when (Keyboard/isKeyDown Keyboard/KEY_RIGHT) (paddle/move! player :right))
  (when (not (or (Keyboard/isKeyDown Keyboard/KEY_LEFT)
                 (Keyboard/isKeyDown Keyboard/KEY_RIGHT) (paddle/move! player :none))))
  (handle-pause)
  (handle-quit))

(defn update-bricks [col]
  (dosync
   (ref-set bricks (remove #(= @col (deref %)) @bricks))))
  
(defn update-game []
  (let [dt (timer/update-timer!)
        _ (paddle/update! player dt)
        col (ball/update!  ball (concat [player r-wall l-wall t-wall b-wall] @bricks) dt)]
    (when-not (nil? col) (update-bricks col))
    (when (<= (count @bricks) 0) (reset! playing? :won))))

(defn won-state []
  (GL11/glClear GL11/GL_COLOR_BUFFER_BIT)
  (handle-quit)
  (render)
  (.drawString font 270.0 300.0 "YOU WIN! THANKS FOR PLAYING!" (Color/white))
  (. Display update)
  (recur))

(defn main-loop []
  (cond (. Display isCloseRequested) (clean-up)
        (= @playing? :paused) (paused-state)
        (= @playing? :won) (won-state)
        :else (do
                (GL11/glClear GL11/GL_COLOR_BUFFER_BIT)
                (handle-input)
                (update-game)
                (render)
                (. Display update)
                (Display/sync FPS)
                (recur))))

(defn paused-state []
  (if (= @playing? :unpaused) main-loop
      (do
        (GL11/glClear GL11/GL_COLOR_BUFFER_BIT)
        (handle-unpause)
        (handle-quit)
        (render)
        (.drawString font 270.0 300.0 "HIT SPACE TO CONTINUE" (Color/white))
        (. Display update)
        (recur))))
                
(defn -main []
  (init-gl WIDTH HEIGHT)
  (init-textures)
  (init-level)
  (trampoline paused-state))