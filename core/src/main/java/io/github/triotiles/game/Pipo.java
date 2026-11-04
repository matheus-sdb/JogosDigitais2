package io.github.triotiles.game;

/**
 * Pipo, o pequeno músico: reage ao desempenho do paciente (docs/PROPOSTA.md, seção 5.2).
 * Só a lógica do humor; o desenho fica em screens/PipoRenderer.
 */
public class Pipo {
    public enum Mood {
        /** Parado, balançando de leve. */
        IDLE,
        /** Acertou: pula e toca uma nota. */
        HAPPY,
        /** Errou: tropeça, sem punir demais. */
        OOPS,
        /** Combo alto: dança sem parar. */
        DANCING
    }

    /** A partir deste combo o Pipo dança. */
    public static final int DANCE_COMBO = 10;
    static final float HAPPY_TIME = 0.45f;
    static final float OOPS_TIME = 0.6f;

    private Mood mood = Mood.IDLE;
    private float moodTime;    // segundos desde que o humor começou
    private float moodLength;  // quanto dura (0 = até mudar)
    private float clock;       // relógio contínuo, para animações que não reiniciam
    private int lastLane = -1;

    /** Acertou uma nota da trilha {@code lane}, ficando com {@code combo} acertos seguidos. */
    public void onHit(int lane, int combo) {
        lastLane = lane;
        if (combo >= DANCE_COMBO) set(Mood.DANCING, 0);
        else set(Mood.HAPPY, HAPPY_TIME);
    }

    /** Nota perdida ou tecla errada. */
    public void onMiss() {
        set(Mood.OOPS, OOPS_TIME);
    }

    /** Comemoração sem fim (ex.: tela final quando foi bem). */
    public void celebrate() {
        set(Mood.DANCING, 0);
    }

    public void update(float delta) {
        clock += delta;
        moodTime += delta;
        if (moodLength > 0 && moodTime >= moodLength) set(Mood.IDLE, 0);
    }

    private void set(Mood mood, float length) {
        if (this.mood != mood || length > 0) moodTime = 0; // um novo acerto reinicia o pulo
        this.mood = mood;
        this.moodLength = length;
    }

    public Mood mood() {
        return mood;
    }

    /** Segundos desde o início do humor atual. */
    public float moodTime() {
        return moodTime;
    }

    /** De 0 a 1: quanto do humor atual já passou (0 para humores sem fim). */
    public float moodProgress() {
        return moodLength <= 0 ? 0 : Math.min(1f, moodTime / moodLength);
    }

    /** Relógio contínuo, em segundos. */
    public float clock() {
        return clock;
    }

    /** Trilha do último acerto (0 a 2), ou -1. Usado para acender a tecla que ele "toca". */
    public int lastLane() {
        return lastLane;
    }
}
