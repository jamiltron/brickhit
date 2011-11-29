(ns brickhit.sprite
  (:import (org.lwjgl.opengl Display DisplayMode GL11)
           (org.lwjgl.util.glu GLU)
           (org.lwjgl.input Keyboard)
           (org.newdawn.slick Color)
           (org.newdawn.slick.opengl Texture TextureLoader)
           (org.newdawn.slick.util ResourceLoader)))

(defn clamp [min-bound test-val max-bound]
  (min (max min-bound test-val) max-bound))


(defn load-image [this image]
  (let [t (TextureLoader/getTexture "PNG" (ResourceLoader/getResourceAsStream image))]
    (assoc this :texture t
                :w (.getImageWidth t)
                :h (.getImageHeight t))))

(defn draw [this]
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
  (GL11/glPopMatrix))

(defn aabb? [x y]
  (let [a @x
        b @y]
    (if (and (not= a b)
             (< (:x a) (+ (:x b) (:w b)))
             (< (:x b) (+ (:x a) (:w a)))
             (< (:y a) (+ (:y b) (:h b)))
             (< (:y b) (+ (:y a) (:h a)))) true
           false)))

(defn collision-list
  "Given a collision function and sprite, and a list of other entities, it
  returns a list of all others who collide with entity."
  [collision-func entity others]
  (filter #(collision-func entity %) others))

(defn one-d [a-min a-max b-min b-max]
  (cond
   (and (< a-min b-min) (< a-max b-max)) (- a-max b-min)
   (and (< b-min a-min) (< b-max a-max)) (- a-min b-max)
   :else (- a-max a-min)))

(defn calc-area
  "Assuming a collision has occured between a & b, calc the area within that collision, return
   a vector of [x-area y-area]"
  [a b]
    (let [a-left (:x a) a-top (:y a) a-right (+ (:x a) (:w a)) a-bottom (+ (:y a) (:h a))
          b-left (:x b) b-top (:y b) b-right (+ (:x b) (:w b)) b-bottom (+ (:y b) (:h b))
          x-area (one-d a-left a-right b-left b-right)
          y-area (one-d a-top a-bottom b-top b-bottom)]
      [x-area y-area]))

(defn colliding-edge [x y]
  (let [a @x
        b @y
        a-left (:x a) a-top (:y a) a-right (+ (:x a) (:w a)) a-bottom (+ (:y a) (:h a))
        b-left (:x b) b-top (:y b) b-right (+ (:x b) (:w b)) b-bottom (+ (:y b) (:h b))
        x-area (first (calc-area a b))
        y-area (second (calc-area a b))]
    (cond
     (= y-area x-area) :corner
     (< y-area x-area) (if (< a-top b-top) :top
                           :bottom)
     :else (if (< a-left b-left) :left
               :right))))

         