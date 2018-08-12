(ns flappy-bird.core
  (:require [play-clj.core :refer :all]
            [play-clj.g2d :refer :all]
            [play-clj.math :refer :all]
            [play-clj.ui :refer :all])
  (:import [com.badlogic.gdx.graphics Texture Texture$TextureFilter]
           [com.badlogic.gdx.math Rectangle Vector2]))

(set! *warn-on-reflection* true)

(defonce manager (asset-manager))
(set-asset-manager! manager)

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
  (touch-down [this])
  (get-x [this])
  (get-y [this]))

(defrecord Bird
    [width
     height
     ^Vector2 position
     ^Vector2 velocity
     ^Vector2 acceleration
     rotation]
  Updatable
  (touch-down [_]
    (let [new-velocity (vector-2! velocity :cpy)]
      (y! new-velocity -140)
      (->Bird width height position new-velocity acceleration rotation)))
  (get-x [_]
    (x position))
  (get-y [_]
    (y position))
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
  (->Bird width height (vector-2 x y) (vector-2 0 0) (vector-2 0
                                                               460
                                                               ;; 0
                                                               ) 0))

(defn load-assets []
  (let [tex (Texture. "texture.png")
        _ (.setFilter tex
                      Texture$TextureFilter/Linear
                      Texture$TextureFilter/Linear)
        bird-down (texture tex
                           :set-region 136 0 17 12
                           :flip false true)
        bird (texture tex
                      :set-region 153 0 17 12
                      :flip false true)
        bird-up (texture tex
                         :set-region 170 0 17 12
                         :flip false true)
        birds [bird-down bird bird-up]]
    {:bg (texture tex
                  :set-region 0 0 136 43
                  :flip false true)
     :grass (texture tex
                     :set-region 0 43 143 11
                     :flip false true)
     :bird-down bird-down
     :bird bird
     :bird-up bird-up
     :bird-animation (animation 0.06 birds)
     :skull-up (texture tex :set-region 192 0 24 14)
     :skull-down (texture tex
                          :set-region 192 0 24 14
                          :flip false true)
     :bar (texture tex
                   :set-region 136 16 22 3
                   :flip false true)}))

(defn update-world [world delta-time]
  (update world :bird #(next-frame % delta-time)))

(defscreen main-screen
  :on-show
  (fn [screen entities]
    (let [camera (orthographic :set-to-ortho true 136 204)
          {:keys [bg grass] :as assets} (load-assets)]
      (update! screen
               :renderer (stage)
               :camera camera
               :world {:rect (rectangle 0 0 17 12)
                       :bird (new-bird 33 100 17 12)}
               :assets assets)
      [(shape :filled
              :set-color (/ 55 255) (/ 80 255) (/ 100 255) 1
              :rect 0 0 136 98)
       (assoc bg
              :x 0 :y 98
              :width 136 :height 43)
       (assoc grass
              :x 0 :y 141
              :width 136 :height 11)
       (shape :filled
              :set-color (/ 147 255) (/ 80 255) (/ 27 255) 1
              :rect 0 152 136 52)]))
  :on-touch-down
  (fn [{:keys [world] :as screen} entities]
    (update! screen :world (update world :bird touch-down))
    entities)
  :on-render
  (fn [{:keys [assets delta-time world] :as screen} entities]
    (clear!)
    (update! screen :world (update-world world delta-time))
    (let [{:keys [^Rectangle rect bird]} world
          bird-tex (:bird assets)
          x (rectangle! rect :get-x)
          y (rectangle! rect :get-y)
          width (rectangle! rect :get-width)
          height (rectangle! rect :get-height)
          new-x (if (> x 137)
                  -20
                  (inc x))]
      (render! screen entities)
      (render! screen
               [(assoc bird-tex
                       :x (get-x bird) :y (get-y bird)
                       :width (:width bird) :height (:height bird))])
      (rectangle! rect :set-x new-x))
    entities))

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
