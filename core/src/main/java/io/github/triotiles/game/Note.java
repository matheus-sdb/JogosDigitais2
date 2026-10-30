package io.github.triotiles.game;

/** Uma nota que cai numa trilha. Tempos em segundos desde o início da fase. */
public class Note {
    public enum State { PENDING, HIT, MISSED }

    /** Trilha 0, 1 ou 2 (tecla 1, 2 ou 3). */
    public final int lane;
    /** Momento ideal do toque: quando a nota cruza a linha de acerto. */
    public final double time;

    State state = State.PENDING;
    double offset;
    Judgement judgement;

    public Note(int lane, double time) {
        if (lane < 0 || lane >= Playfield.LANES) throw new IllegalArgumentException("trilha inválida: " + lane);
        this.lane = lane;
        this.time = time;
    }

    public State state() {
        return state;
    }

    public boolean isPending() {
        return state == State.PENDING;
    }

    /** Toque − momento ideal, em segundos (positivo = atrasado). Só vale para notas acertadas. */
    public double offset() {
        return offset;
    }

    /** Perfeito/Bom/Ok se acertada, Errou se perdida, null se ainda não resolvida. */
    public Judgement judgement() {
        return judgement;
    }
}
