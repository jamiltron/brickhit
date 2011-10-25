(ns brickhit.display
  (:import (org.lwjgl.util Display)))

(defn get-display-mode [width height]
  (let [dm (Display/getAvailableDisplayModes width height width height 16 16 60 60)]
    (do (println (count dm))
      (Display/setDisplayMode dm (into-array String [(str "width=" width) (str "height=" height) (str "freq=" 60) (str "bpp="  24)])))))