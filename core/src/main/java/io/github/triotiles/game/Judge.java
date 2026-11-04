package io.github.triotiles.game;

/**
 * Transforma o erro de tempo de um toque (Δt) em Perfeito / Bom / Ok.
 * As janelas podem mudar por paciente (Configurações, Sprint 5).
 */
public class Judge {
    private final double perfect;
    private final double good;
    private final double ok;

    /** Janelas em segundos, para mais ou para menos. Precisa ser perfect ≤ good ≤ ok. */
    public Judge(double perfect, double good, double ok) {
        if (!(0 < perfect && perfect <= good && good <= ok)) {
            throw new IllegalArgumentException("janelas inválidas: " + perfect + ", " + good + ", " + ok);
        }
        this.perfect = perfect;
        this.good = good;
        this.ok = ok;
    }

    /** Valores iniciais da proposta: 80 / 160 / 250 ms. */
    public static Judge standard() {
        return new Judge(0.080, 0.160, 0.250);
    }

    /**
     * @param offset toque − momento ideal, em segundos
     * @return a avaliação, ou {@link Judgement#MISS} se ficou fora da janela de acerto
     */
    public Judgement judge(double offset) {
        double d = Math.abs(offset);
        if (d <= perfect) return Judgement.PERFECT;
        if (d <= good) return Judgement.GOOD;
        if (d <= ok) return Judgement.OK;
        return Judgement.MISS;
    }

    /** Maior distância do momento ideal que ainda conta como acerto, em segundos. */
    public double hitWindow() {
        return ok;
    }
}
