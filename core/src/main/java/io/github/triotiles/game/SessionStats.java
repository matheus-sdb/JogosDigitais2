package io.github.triotiles.game;

import java.util.EnumMap;

/**
 * Resumo de uma fase jogada: as métricas de reabilitação da proposta (seção 5.5).
 * Tempos em milissegundos. Métricas sem dados suficientes valem {@link Double#NaN}.
 */
public final class SessionStats {
    public final int totalNotes;
    public final int hits;
    public final int misses;
    public final int strays;
    public final int points;
    public final int maxCombo;
    private final EnumMap<Judgement, Integer> counts = new EnumMap<>(Judgement.class);

    /** Precisão: acertos ÷ total de notas (0 a 1). */
    public final double accuracy;
    /** Velocidade: média de |Δt|, o quanto cada toque se afasta do momento certo. */
    public final double meanAbsOffsetMs;
    /** Consistência: desvio padrão de Δt. Quanto menor, mais regular o movimento. Precisa de 2+ acertos. */
    public final double stdDevOffsetMs;
    /** Tendência: média de Δt. Negativo = costuma adiantar; positivo = costuma atrasar. */
    public final double meanOffsetMs;

    private final int[] laneNotes = new int[Playfield.LANES];
    private final int[] laneHits = new int[Playfield.LANES];
    private final int[] laneStrays = new int[Playfield.LANES];

    private SessionStats(Playfield field) {
        for (Judgement j : Judgement.values()) counts.put(j, 0);

        double sum = 0, sumAbs = 0;
        for (Note n : field.chart().notes()) {
            laneNotes[n.lane]++;
            if (n.judgement() != null) counts.merge(n.judgement(), 1, Integer::sum);
            if (n.state() == Note.State.HIT) {
                laneHits[n.lane]++;
                double ms = n.offset() * 1000;
                sum += ms;
                sumAbs += Math.abs(ms);
            }
        }
        for (int lane = 0; lane < Playfield.LANES; lane++) laneStrays[lane] = field.strays(lane);

        totalNotes = field.chart().size();
        hits = field.hits();
        misses = field.misses();
        strays = field.strays();
        points = field.score().points();
        maxCombo = field.score().maxCombo();
        accuracy = field.accuracy();

        meanOffsetMs = hits == 0 ? Double.NaN : sum / hits;
        meanAbsOffsetMs = hits == 0 ? Double.NaN : sumAbs / hits;
        if (hits < 2) {
            stdDevOffsetMs = Double.NaN;
        } else {
            double squares = 0;
            for (Note n : field.chart().notes()) {
                if (n.state() == Note.State.HIT) {
                    double d = n.offset() * 1000 - meanOffsetMs;
                    squares += d * d;
                }
            }
            stdDevOffsetMs = Math.sqrt(squares / hits);
        }
    }

    /** Calcula as métricas a partir do estado atual da fase. */
    public static SessionStats from(Playfield field) {
        return new SessionStats(field);
    }

    /** Quantas notas tiveram essa avaliação (MISS = perdidas). */
    public int count(Judgement judgement) {
        return counts.get(judgement);
    }

    public int laneNotes(int lane) {
        return laneNotes[lane];
    }

    public int laneHits(int lane) {
        return laneHits[lane];
    }

    /** Teclas erradas nessa trilha. */
    public int laneStrays(int lane) {
        return laneStrays[lane];
    }

    /** Precisão de um dedo (0 a 1), ou NaN se a trilha não teve notas. */
    public double laneAccuracy(int lane) {
        return laneNotes[lane] == 0 ? Double.NaN : (double) laneHits[lane] / laneNotes[lane];
    }

    /**
     * O dedo com menor precisão, se estiver pelo menos {@code margin} abaixo do melhor
     * (ex.: 0.10 = 10 pontos percentuais). Senão -1: os dedos estão parecidos.
     */
    public int weakestLane(double margin) {
        int worst = -1, best = -1;
        for (int lane = 0; lane < Playfield.LANES; lane++) {
            double a = laneAccuracy(lane);
            if (Double.isNaN(a)) continue;
            if (worst < 0 || a < laneAccuracy(worst)) worst = lane;
            if (best < 0 || a > laneAccuracy(best)) best = lane;
        }
        if (worst < 0 || laneAccuracy(best) - laneAccuracy(worst) < margin) return -1;
        return worst;
    }
}
