(ns brickhit.util
  (:import (org.lwjgl.opengl Display DisplayMode GL11)
           (org.lwjgl.util.glu GLU)
           (org.lwjgl.input Keyboard)
           (org.newdawn.slick Color)
           (org.newdawn.slick.opengl Texture TextureLoader)
           (org.newdawn.slick.util ResourceLoader)))

(defn load-image [this image]
  (let [l (TextureLoader/getTexture "PNG" (ResourceLoader/getResourceAsStream image))]
    (assoc this :texture l
                :w (.getImageWidth l)
                :h (.getImageHeight l))))

(defn draw [this]
  (do
    (GL11/glPushMatrix)
    (.bind Color/white)
    (.bind (:texture this))
    (GL11/glTranslatef (:x this) (:y this) 0)
    (GL11/glBegin GL11/GL_QUADS)
    (GL11/glTexCoord2f 0 0)
    (GL11/glVertex2i 0 0)
    (GL11/glTexCoord2f 1 0)
    (GL11/glVertex2i  (:w this) 0)
    (GL11/glTexCoord2f 1 1)
    (GL11/glVertex2i (:w this) (:h this))
    (GL11/glTexCoord2f 0 1)
    (GL11/glVertex2i 0 (:h this))
    (GL11/glEnd)
    (GL11/glPopMatrix)))

(defn clamp [min-bound test-val max-bound]
  (min (max min-bound test-val) max-bound))

(defn collided?  [e1 e2]
  (if (and (not= e1 e2)
           (< (:x e1) (+ (:x e2) (:w e2)))
           (< (:x e2) (+ (:x e1) (:w e1)))
           (< (:y e1) (+ (:y e2) (:h e2)))
           (< (:y e2) (+ (:y e1) (:w e1)))) true
       false))

(defn collision-list [e1 & others]
  (filter #(collided? e1 %) others))

(defn calc-area [a-min a-max b-min b-max]
  (cond
   (and (< a-max b-max) (> a-min b-min)) 0
   (< a-max b-max) (- a-max b-min)
   :else (- b-max a-min)))

(defn colliding-edge [e1 e2]
  (let [e1-x1 (:x e1) e1-x2 (+ (:x e1) (:w e1))
        e2-x1 (:x e2) e2-x2 (+ (:x e2) (:w e2))
        e1-y1 (:y e1) e1-y2 (+ (:y e1) (:h e1))
        e2-y1 (:y e2) e2-y2 (+ (:y e2) (:h e2))     
        x-area (calc-area e1-x1 e1-x2 e2-x1 e2-x2)
        y-area (calc-area e1-y1 e1-y2 e2-y1 e2-y2)]
    (if (< y-area x-area) (if (< e1-x1 e2-x1) :left
                              :right)
        (if (< e1-y1 e2-y1) :top
            :bottom))))
               
               
           