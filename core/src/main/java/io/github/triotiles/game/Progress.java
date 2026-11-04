package io.github.triotiles.game;

import java.util.Arrays;

/**
 * Até onde o paciente chegou: fases liberadas e melhor precisão em cada uma.
 * A fase seguinte libera ao atingir a meta da anterior (docs/PROPOSTA.md, seção 5.3).
 * Quem salva e carrega é o {@code ProgressStore}.
 */
public class Progress {
    private int unlocked = 1;
    private final double[] best = new double[LevelData.values().length];

    public Progress() {
        Arrays.fill(best, Double.NaN);
    }

    /** Recria um progresso salvo. Valores fora do intervalo são corrigidos. */
    public static Progress restore(int unlocked, double[] best) {
        Progress p = new Progress();
        p.unlocked = Math.max(1, Math.min(LevelData.values().length, unlocked));
        for (int i = 0; i < p.best.length && i < best.length; i++) p.best[i] = best[i];
        return p;
    }

    public boolean isUnlocked(LevelData level) {
        return level.number <= unlocked;
    }

    /** Número da última fase liberada (1 a 3). */
    public int unlockedCount() {
        return unlocked;
    }

    /** Melhor precisão já feita na fase (0 a 1), ou NaN se nunca jogou. */
    public double best(LevelData level) {
        return best[level.ordinal()];
    }

    /**
     * Registra o resultado de uma fase concluída.
     *
     * @return a fase liberada agora por este resultado, ou null se nada novo foi liberado
     */
    public LevelData record(LevelData level, double accuracy) {
        int i = level.ordinal();
        if (Double.isNaN(best[i]) || accuracy > best[i]) best[i] = accuracy;

        LevelData next = level.next();
        if (next != null && level.goalReached(accuracy) && !isUnlocked(next)) {
            unlocked = next.number;
            return next;
        }
        return null;
    }
}
