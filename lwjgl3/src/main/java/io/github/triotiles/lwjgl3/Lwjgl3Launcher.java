package io.github.triotiles.lwjgl3;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import io.github.triotiles.TrioTilesGame;

/** Abre o jogo no desktop (Windows, Linux, macOS). */
public class Lwjgl3Launcher {
    public static void main(String[] args) {
        // Tem que vir primeiro: no macOS reabre o Java com -XstartOnFirstThread (senão o .jar não abre)
        // e no Windows evita falha quando o nome do usuário tem caracteres especiais.
        if (StartupHelper.startNewJvmIfRequired()) return;

        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("TrioTiles");
        config.setWindowedMode(1280, 720);
        config.useVsync(true);
        // Alguns monitores (e telas virtuais) informam 0 Hz; garante no mínimo 60 FPS.
        config.setForegroundFPS(Math.max(60, Lwjgl3ApplicationConfiguration.getDisplayMode().refreshRate + 1));
        // Ícone da janela e da barra de tarefas (lwjgl3/src/main/resources/).
        config.setWindowIcon("icon128.png", "icon64.png", "icon32.png", "icon16.png");
        new Lwjgl3Application(new TrioTilesGame(), config);
    }
}
