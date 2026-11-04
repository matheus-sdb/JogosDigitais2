package io.github.triotiles.game;

/**
 * Uma nota da fase. Tempos em segundos desde o início da fase.
 * <ul>
 *   <li>{@link Type#TAP}: apertar a tecla da trilha (dois TAP no mesmo instante formam um acorde)</li>
 *   <li>{@link Type#HOLD}: apertar e segurar até o fim (força e resistência do dedo)</li>
 *   <li>{@link Type#SPIN}: girar o knob na direção indicada (rotação fina), ocupa as 3 trilhas</li>
 * </ul>
 */
public class Note {
    public enum Type { TAP, HOLD, SPIN }

    public enum State { PENDING, HIT, MISSED }

    /** Situação da parte "segurar" de uma nota longa. */
    public enum HoldState { NONE, HOLDING, COMPLETED, BROKEN }

    /** Direção do giro: anti-horário (knob para a esquerda) ou horário (para a direita). */
    public static final int SPIN_LEFT = -1;
    public static final int SPIN_RIGHT = 1;

    public final Type type;
    /** Trilha 0, 1 ou 2 (tecla 1, 2 ou 3). -1 nas notas de giro. */
    public final int lane;
    /** Momento ideal do toque: quando a nota cruza a linha de acerto. */
    public final double time;
    /** Quanto tempo segurar (só notas longas; 0 nas outras). */
    public final double duration;
    /** {@link #SPIN_LEFT} ou {@link #SPIN_RIGHT} nas notas de giro; 0 nas outras. */
    public final int direction;

    State state = State.PENDING;
    double offset;
    Judgement judgement;
    HoldState holdState = HoldState.NONE;
    double heldSeconds;

    /** Nota simples. */
    public Note(int lane, double time) {
        this(Type.TAP, lane, time, 0, 0);
    }

    private Note(Type type, int lane, double time, double duration, int direction) {
        if (type != Type.SPIN && (lane < 0 || lane >= Playfield.LANES)) {
            throw new IllegalArgumentException("trilha inválida: " + lane);
        }
        if (type == Type.HOLD && duration <= 0) throw new IllegalArgumentException("nota longa sem duração");
        if (type == Type.SPIN && direction != SPIN_LEFT && direction != SPIN_RIGHT) {
            throw new IllegalArgumentException("direção de giro inválida: " + direction);
        }
        this.type = type;
        this.lane = lane;
        this.time = time;
        this.duration = duration;
        this.direction = direction;
    }

    public static Note tap(int lane, double time) {
        return new Note(lane, time);
    }

    public static Note hold(int lane, double time, double duration) {
        return new Note(Type.HOLD, lane, time, duration, 0);
    }

    public static Note spin(int direction, double time) {
        return new Note(Type.SPIN, -1, time, 0, direction);
    }

    /** Momento em que a nota termina (fim da parte segurada, nas notas longas). */
    public double endTime() {
        return time + duration;
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

    public HoldState holdState() {
        return holdState;
    }

    /** Quanto tempo a nota longa foi segurada, em segundos. */
    public double heldSeconds() {
        return heldSeconds;
    }
}
