package io.github.triotiles.save;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import io.github.triotiles.game.LevelData;
import io.github.triotiles.game.Progress;
import io.github.triotiles.game.Settings;

import java.util.Locale;

/**
 * Salva o progresso de cada paciente (fases liberadas e melhores precisões) nas Preferences do libGDX.
 * No Windows fica em %USERPROFILE%\.prefs\TrioTiles.
 */
public class ProgressStore {
    private static final String FILE = "TrioTiles";
    private static final String UNLOCKED = "fasesLiberadas";
    private static final String BEST = "melhorPrecisaoFase";

    private Preferences prefs() {
        return Gdx.app.getPreferences(FILE);
    }

    /** Sufixo das chaves de um paciente. O paciente padrão usa as chaves antigas (sem sufixo). */
    private static String suffix(String patient) {
        if (patient == null || patient.equalsIgnoreCase(Settings.DEFAULT_PATIENT)) return "";
        return "." + patient.trim().toLowerCase(Locale.ROOT);
    }

    public Progress load(String patient) {
        try {
            Preferences p = prefs();
            String sx = suffix(patient);
            double[] best = new double[LevelData.values().length];
            for (LevelData level : LevelData.values()) {
                best[level.ordinal()] = p.getFloat(BEST + level.number + sx, Float.NaN);
            }
            return Progress.restore(p.getInteger(UNLOCKED + sx, 1), best);
        } catch (RuntimeException e) {
            Gdx.app.error("ProgressStore", "progresso não carregou; começando do zero", e);
            return new Progress();
        }
    }

    public void save(String patient, Progress progress) {
        try {
            Preferences p = prefs();
            String sx = suffix(patient);
            p.putInteger(UNLOCKED + sx, progress.unlockedCount());
            for (LevelData level : LevelData.values()) {
                double best = progress.best(level);
                if (!Double.isNaN(best)) p.putFloat(BEST + level.number + sx, (float) best);
            }
            p.flush();
        } catch (RuntimeException e) {
            Gdx.app.error("ProgressStore", "progresso não foi salvo", e);
        }
    }
}
