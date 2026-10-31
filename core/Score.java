package io.github.triotiles.game;

/**
 * Pontos e combo. Cada acerto vale os pontos da avaliação × multiplicador do combo.
 * Nota perdida ou tecla errada zera o combo.
 */
public class Score {
    /** A cada quantos acertos seguidos o multiplicador sobe. */
    public static final int COMBO_STEP = 10;
    public static final int MAX_MULTIPLIER = 4;

    private int points;
    private int combo;
    private int maxCombo;

    void hit(Judgement judgement) {
        combo++;
        maxCombo = Math.max(maxCombo, combo);
        points += judgement.points * multiplier();
    }

    void breakCombo() {
        combo = 0;
    }

    public int points() {
        return points;
    }

    /** Acertos seguidos agora. */
    public int combo() {
        return combo;
    }

    public int maxCombo() {
        return maxCombo;
    }

    /** 1x até 9 acertos seguidos, 2x a partir de 10, 3x a partir de 20, 4x a partir de 30. */
    public int multiplier() {
        return Math.min(MAX_MULTIPLIER, 1 + combo / COMBO_STEP);
    }
}
