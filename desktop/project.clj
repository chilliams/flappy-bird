(defproject flappy-bird "0.0.1-SNAPSHOT"
  :description "FIXME: write description"

  :dependencies [[com.badlogicgames.gdx/gdx "1.8.0"]
                 [com.badlogicgames.gdx/gdx-backend-lwjgl "1.8.0"]
                 [com.badlogicgames.gdx/gdx-box2d "1.8.0"]
                 [com.badlogicgames.gdx/gdx-box2d-platform "1.8.0"
                  :classifier "natives-desktop"]
                 [com.badlogicgames.gdx/gdx-bullet "1.8.0"]
                 [com.badlogicgames.gdx/gdx-bullet-platform "1.8.0"
                  :classifier "natives-desktop"]
                 [com.badlogicgames.gdx/gdx-platform "1.8.0"
                  :classifier "natives-desktop"]
                 [org.clojure/clojure "1.9.0"]
                 [play-clj "1.0.0"]]

  :source-paths ["src" "src-common"]
  :javac-options ["-target" "1.8"
                  "-source" "1.8"
                  "-Xlint:-options"
                  "-XgcPrio:deterministic"
                  "-Xms:1g"
                  "-Xmx:1g" ]
  :aot [flappy-bird.core.desktop-launcher]
  :main flappy-bird.core.desktop-launcher)
