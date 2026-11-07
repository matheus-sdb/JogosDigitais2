package io.github.triotiles.game;

/**
 * Uma sessão jogada, como fica salva no histórico (JSON). Campos públicos para o JSON do libGDX.
 * Métricas sem dados ficam null (JSON não tem NaN).
 */
public class SessionRecord {
    /** Data e hora no formato 2026-11-04T15:30. */
    public String date;
    public String patient;
    public int level;

    public int totalNotes;
    public int hits;
    public int misses;
    public int strays;
    public int points;
    public int maxCombo;
    /** Precisão de 0 a 1. */
    public double accuracy;
    /** Velocidade: média de |Δt| em ms. */
    public Double speedMs;
    /** Consistência: desvio padrão de Δt em ms. */
    public Double consistencyMs;
    /** Tendência: média de Δt em ms (negativo = adianta). */
    public Double tendencyMs;
    /** Sustentação das notas longas, de 0 a 1. */
    public Double sustain;
    public int spinHits;
    public int spinNotes;
    /** Precisão de cada tecla (0 a 1), na ordem 1, 2, 3. */
    public Double[] laneAccuracy;
    /** Dedo em cada tecla, conforme a mão usada. */
    public String[] fingers;

    /** Configurações usadas nesta sessão. */
    public String hand;
    public String tempo;
    public String window;
    public int latencyMs;

    /** Para o JSON. */
    public SessionRecord() {
    }

    public static SessionRecord of(SessionStats s, LevelData level, Settings settings, String date) {
        SessionRecord r = new SessionRecord();
        r.date = date;
        r.patient = settings.patient();
        r.level = level.number;
        r.totalNotes = s.totalNotes;
        r.hits = s.hits;
        r.misses = s.misses;
        r.strays = s.strays;
        r.points = s.points;
        r.maxCombo = s.maxCombo;
        r.accuracy = s.accuracy;
        r.speedMs = orNull(s.meanAbsOffsetMs);
        r.consistencyMs = orNull(s.stdDevOffsetMs);
        r.tendencyMs = orNull(s.meanOffsetMs);
        r.sustain = orNull(s.sustain);
        r.spinHits = s.spinHits;
        r.spinNotes = s.spinNotes;
        r.laneAccuracy = new Double[Playfield.LANES];
        r.fingers = new String[Playfield.LANES];
        for (int lane = 0; lane < Playfield.LANES; lane++) {
            r.laneAccuracy[lane] = orNull(s.laneAccuracy(lane));
            r.fingers[lane] = settings.hand().finger(lane);
        }
        r.hand = settings.hand().label;
        r.tempo = settings.tempo().label;
        r.window = settings.window().label;
        r.latencyMs = settings.latencyMs();
        return r;
    }

    private static Double orNull(double v) {
        return Double.isNaN(v) ? null : v;
    }
}
