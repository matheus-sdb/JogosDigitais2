package io.github.triotiles.lwjgl3;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import io.github.triotiles.TrioTilesGame;

/** Abre o jogo no desktop (Windows, Linux, macOS). */
public class Lwjgl3Launcher {
    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("TrioTiles");
        config.setWindowedMode(1280, 720);
        config.useVsync(true);
        // Alguns monitores (e telas virtuais) informam 0 Hz; garante no mínimo 60 FPS.
        config.setForegroundFPS(Math.max(60, Lwjgl3ApplicationConfiguration.getDisplayMode().refreshRate + 1));
        new Lwjgl3Application(new TrioTilesGame(), config);
    }
}
