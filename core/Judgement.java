package io.github.triotiles.game;

/** Avaliação de uma nota (docs/PROPOSTA.md, seção 5.5). */
public enum Judgement {
    PERFECT("Perfeito", 100),
    GOOD("Bom", 70),
    OK("Ok", 40),
    /** Nota que passou sem toque. */
    MISS("Errou", 0);

    public final String label;
    /** Pontos antes do multiplicador de combo. */
    public final int points;

    Judgement(String label, int points) {
        this.label = label;
        this.points = points;
    }
}
