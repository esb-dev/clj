(ns clj.life
  (:require [quil.core :as q]))

(def size "size of the world" 100)
(def scale 5)
(def sleep-length "time in ms between steps" 500)

; world is a 2 dim vector of refs
(def world
  (vec
    (map vec (partition size
               (repeatedly (* size size) #(ref nil))))))

(defn setup []
  (q/color-mode :hsb)
  (q/smooth)
  (q/frame-rate 10))

(defn draw []
  (q/background 0)
  (dosync 
    (doseq [x (range 0 size)
            y (range 0 size)]
      (when (= :on @(get-in world [x y]))
        (q/fill (q/color 100 255 255))
        (q/rect (* scale x) (* scale y) scale scale)))))

(q/defsketch game
  :title "Game of Life"
  :setup setup
  :draw draw
  :size [(* scale size) (* scale size)])

