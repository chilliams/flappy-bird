(ns flappy-bird.core
  (:require [play-clj.core :refer :all]
            [play-clj.math :refer :all]
            [play-clj.ui :refer :all])
  (:import [com.badlogic.gdx.math Rectangle Vector2]))

(set! *warn-on-reflection* true)

(defscreen fps-screen
  :on-show
  (fn [screen entities]
    (update! screen :camera (orthographic) :renderer (stage))
    (assoc (label "0" (color :white))
           :id :fps
           :x 5))

  :on-render
  (fn [screen entities]
    (->> (for [entity entities]
           (case (:id entity)
             :fps (doto entity (label! :set-text (str (game :fps))))
             entity))
         (render! screen)))

  :on-resize
  (fn [screen entities]
    (height! screen 300)))

(defprotocol Updatable
  (next-frame [this delta])
  (touch-down [this]))

(defrecord Bird
    [width
     height
     ^Vector2 position
     ^Vector2 velocity
     ^Vector2 acceleration
     rotation]
  Updatable
  (touch-down [_]
    (let [new-velocity (-> velocity
                           (vector-2! :cpy)
                           (y! -140))]
      (-> Bird width height position new-velocity acceleration rotation)))
  (next-frame [_ delta]
    (let [accel (-> acceleration
                    (vector-2! :cpy)
                    (vector-2! :scl ^float delta))
          v (vector-2! velocity :add accel)
          v (if (> (y v) 200)
              (do (y! v 200) v)
              v)
          move (-> velocity
                   (vector-2! :cpy)
                   (vector-2! :scl ^float delta))]
      (->Bird width
              height
              (-> position
                  (vector-2! :cpy)
                  (vector-2! :add move))
              v
              acceleration
              rotation))))

(defn new-bird [x y width height]
  (->Bird width height (vector-2 x y) (vector-2 0 0) (vector-2 0 460) 0))

(defscreen main-screen
  :on-show
  (fn [screen entities]
    (let [camera (orthographic :set-to-ortho true 136 204)]
      (update! screen
               :renderer (stage)
               :camera camera
               :world {:rect (rectangle 0 0 17 12)})
      (new-bird 33 100 17 12)))
  :on-touch-down
  (fn [screen entities]
    (map touch-down entities))
  :on-render
  (fn [{:keys [delta-time world] :as screen} entities]
    (clear!)
    (let [{^Rectangle rect :rect} world
          x (rectangle! rect :get-x)
          y (rectangle! rect :get-y)
          width (rectangle! rect :get-width)
          height (rectangle! rect :get-height)
          new-x (if (> x 137)
                  -20
                  (inc x))]
      (render!
       screen
       [(shape :filled
               :set-color (/ 87 255) (/ 109 255) (/ 120 255) 1
               :rect x y width height)
        (shape :line
               :set-color (/ 255 255) (/ 109 255) (/ 120 255) 1
               :rect x y width height)])
      (rectangle! rect :set-x new-x))
    (map #(next-frame % delta-time) entities)))

(defgame flappy-bird-game
  :on-create
  (fn [this]
    (set-screen! this main-screen)))


(defscreen blank-screen
  :on-render
  (fn [screen entities]
    (clear!)))

(set-screen-wrapper!
 (fn [screen screen-fn]
   (try (screen-fn)
        (catch Exception e
          (.printStackTrace e)
          (set-screen! flappy-bird-game blank-screen)))))
