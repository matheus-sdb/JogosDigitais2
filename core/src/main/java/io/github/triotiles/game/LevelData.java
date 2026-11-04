package io.github.triotiles.game;

/**
 * As 3 fases, com dificuldade progressiva (docs/PROPOSTA.md, seção 5.4).
 * Os números são valores iniciais, para ajustar nos testes com pacientes.
 */
public enum LevelData {
    AQUECIMENTO(1, "Aquecimento", Stage.QUARTO, "Ativar cada dedo, reação simples",
        60, 1.00, 320, new Judge(0.100, 0.200, 0.300), 0, 0, 0, 0.70),
    RITMO(2, "Ritmo", Stage.PRACA, "Ritmo constante e resistência do dedo",
        90, 0.75, 400, Judge.standard(), 0.22, 0, 0, 0.75),
    DESTREZA(3, "Destreza", Stage.TEATRO, "Coordenação entre dedos e controle fino",
        120, 0.62, 470, new Judge(0.070, 0.130, 0.200), 0.15, 0.15, 0.10, 0);

    /** Onde o Pipo toca em cada fase: a "carreira" dele acompanha a evolução do paciente. */
    public enum Stage {
        QUARTO("Quarto"), PRACA("Praça"), TEATRO("Teatro");

        public final String label;

        Stage(String label) {
            this.label = label;
        }
    }

    public final int number;
    public final String title;
    public final Stage stage;
    /** Objetivo terapêutico da fase, mostrado na seleção de fase. */
    public final String purpose;
    /** Duração aproximada, em segundos. */
    public final double durationSeconds;
    /** Tempo entre notas, em segundos. */
    public final double interval;
    /** Velocidade de queda das notas, em pixels por segundo. */
    public final float speed;
    private final Judge judge;
    /** Chance de cada evento ser nota longa / acorde / giro do knob. */
    public final double holdChance;
    public final double chordChance;
    public final double spinChance;
    /** Precisão para liberar a próxima fase (0 = basta concluir). */
    public final double goalAccuracy;

    LevelData(int number, String title, Stage stage, String purpose, double durationSeconds, double interval,
              float speed, Judge judge, double holdChance, double chordChance, double spinChance,
              double goalAccuracy) {
        this.number = number;
        this.title = title;
        this.stage = stage;
        this.purpose = purpose;
        this.durationSeconds = durationSeconds;
        this.interval = interval;
        this.speed = speed;
        this.judge = judge;
        this.holdChance = holdChance;
        this.chordChance = chordChance;
        this.spinChance = spinChance;
        this.goalAccuracy = goalAccuracy;
    }

    public Judge judge() {
        return judge;
    }

    /** "Fase 1: Aquecimento". */
    public String fullTitle() {
        return "Fase " + number + ": " + title;
    }

    public boolean hasGoal() {
        return goalAccuracy > 0;
    }

    /** A precisão atinge a meta (ou a fase não tem meta). */
    public boolean goalReached(double accuracy) {
        return accuracy >= goalAccuracy;
    }

    /** A fase seguinte, ou null na última. */
    public LevelData next() {
        int i = ordinal() + 1;
        return i < values().length ? values()[i] : null;
    }

    /** Fase pelo número (1 a 3). */
    public static LevelData byNumber(int number) {
        return values()[number - 1];
    }

    /** Sequência de notas nova para esta fase. Mesma semente = mesma sequência. */
    public Chart chart(long seed) {
        return Chart.generate(this, seed);
    }
}
