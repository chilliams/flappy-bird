(ns flappy-bird.core.desktop-launcher
  (:require [flappy-bird.core :refer :all])
  (:import [com.badlogic.gdx.backends.lwjgl LwjglApplication]
           [org.lwjgl.input Keyboard])
  (:gen-class))

(defn -main
  []
  (LwjglApplication. flappy-bird-game "Flappy Bird" 272 408)
  (Keyboard/enableRepeatEvents true))
