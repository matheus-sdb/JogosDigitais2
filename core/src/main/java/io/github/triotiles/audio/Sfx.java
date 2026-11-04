package io.github.triotiles.audio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.Disposable;

/**
 * Efeitos sonoros (gerados por tools/gerar_sons.py, em assets/sounds/).
 * Cada trilha toca uma nota: Dó, Mi, Sol. Se o som não carregar (PC sem áudio),
 * o jogo segue em silêncio.
 */
public class Sfx implements Disposable {
    /** Volume geral de 0 a 1 (vai para as Configurações na Sprint 5). */
    public float volume = 0.8f;

    private final Sound[] lanes = new Sound[3];
    private final Sound miss;
    private final Sound move;
    private final Sound confirm;
    private final Sound spin;

    public Sfx() {
        for (int i = 0; i < lanes.length; i++) lanes[i] = load("sounds/trilha" + (i + 1) + ".wav");
        miss = load("sounds/erro.wav");
        move = load("sounds/menu_mover.wav");
        confirm = load("sounds/menu_confirmar.wav");
        spin = load("sounds/giro.wav");
    }

    private static Sound load(String path) {
        try {
            return Gdx.audio.newSound(Gdx.files.internal(path));
        } catch (RuntimeException e) {
            Gdx.app.error("Sfx", "som não carregou: " + path, e);
            return null;
        }
    }

    private void play(Sound sound, float relativeVolume) {
        if (sound != null && volume > 0) sound.play(volume * relativeVolume);
    }

    /** Acertou uma nota da trilha 0, 1 ou 2. */
    public void hit(int lane) {
        play(lanes[lane], 1f);
    }

    /** Nota perdida ou tecla errada: som suave, para não punir demais. */
    public void miss() {
        play(miss, 0.6f);
    }

    /** Nota de giro acertada. */
    public void spin() {
        play(spin, 0.9f);
    }

    /** Nota longa segurada até o fim: a nota da trilha de novo, mais baixinho. */
    public void holdCompleted(int lane) {
        play(lanes[lane], 0.5f);
    }

    public void menuMove() {
        play(move, 0.7f);
    }

    public void menuConfirm() {
        play(confirm, 0.8f);
    }

    @Override
    public void dispose() {
        for (Sound s : new Sound[]{lanes[0], lanes[1], lanes[2], miss, move, confirm, spin}) {
            if (s != null) s.dispose();
        }
    }
}
