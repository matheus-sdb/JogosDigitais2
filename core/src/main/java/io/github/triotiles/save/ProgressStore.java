package io.github.triotiles.save;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import io.github.triotiles.game.LevelData;
import io.github.triotiles.game.Progress;

/**
 * Salva o progresso (fases liberadas e melhores precisões) nas Preferences do libGDX.
 * No Windows fica em %USERPROFILE%\.prefs\TrioTiles.
 */
public class ProgressStore {
    private static final String FILE = "TrioTiles";
    private static final String UNLOCKED = "fasesLiberadas";
    private static final String BEST = "melhorPrecisaoFase";

    private Preferences prefs() {
        return Gdx.app.getPreferences(FILE);
    }

    public Progress load() {
        try {
            Preferences p = prefs();
            double[] best = new double[LevelData.values().length];
            for (LevelData level : LevelData.values()) {
                best[level.ordinal()] = p.getFloat(BEST + level.number, Float.NaN);
            }
            return Progress.restore(p.getInteger(UNLOCKED, 1), best);
        } catch (RuntimeException e) {
            Gdx.app.error("ProgressStore", "progresso não carregou; começando do zero", e);
            return new Progress();
        }
    }

    public void save(Progress progress) {
        try {
            Preferences p = prefs();
            p.putInteger(UNLOCKED, progress.unlockedCount());
            for (LevelData level : LevelData.values()) {
                double best = progress.best(level);
                if (!Double.isNaN(best)) p.putFloat(BEST + level.number, (float) best);
            }
            p.flush();
        } catch (RuntimeException e) {
            Gdx.app.error("ProgressStore", "progresso não foi salvo", e);
        }
    }
}
