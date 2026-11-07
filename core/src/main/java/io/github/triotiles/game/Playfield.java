package io.github.triotiles.game;

import java.util.List;

/**
 * Regras da fase, sem nada de desenho: decide se um toque acertou uma nota (e quão bem),
 * acompanha as notas longas, os giros do knob, as notas perdidas e a pontuação.
 * Tempos em segundos desde o início da fase.
 */
public class Playfield {
    public static final int LANES = 3;
    /** Soltar a nota longa até este tanto antes do fim ainda conta como segurada até o fim. */
    public static final double HOLD_TOLERANCE = 0.15;

    /** Avisos do que aconteceu sozinho com o passar do tempo (sem toque do paciente). */
    public interface Listener {
        /** Nota passou da linha sem toque. */
        default void onMiss(Note note) {
        }

        /** Nota longa segurada até o fim. */
        default void onHoldCompleted(Note note) {
        }
    }

    private final Chart chart;
    private final Judge judge;
    private final Score score = new Score();
    /** Todas as notas antes deste índice já foram resolvidas (acertadas ou perdidas). */
    private int cursor;
    /** Nota longa sendo segurada em cada trilha (ou null). */
    private final Note[] holding = new Note[LANES];

    private int hits;
    private int misses;
    private final int[] straysByLane = new int[LANES];

    public Playfield(Chart chart, Judge judge) {
        this.chart = chart;
        this.judge = judge;
    }

    /**
     * Tecla da trilha apertada no momento {@code time}.
     *
     * @return a nota acertada (com {@link Note#judgement()} preenchido), ou null se não havia nota
     *         dessa trilha perto da linha. Isso conta como "tecla errada" e zera o combo.
     */
    public Note press(int lane, double time) {
        Note n = findPending(time, note -> note.type != Note.Type.SPIN && note.lane == lane);
        if (n == null) {
            straysByLane[lane]++;
            score.breakCombo();
            return null;
        }
        resolveHit(n, time);
        if (n.type == Note.Type.HOLD) {
            n.holdState = Note.HoldState.HOLDING;
            holding[lane] = n;
        }
        return n;
    }

    /**
     * Tecla da trilha solta. Só importa se ela estava segurando uma nota longa.
     *
     * @return a nota longa solta (veja {@link Note#holdState()}: COMPLETED ou BROKEN), ou null
     */
    public Note release(int lane, double time) {
        Note n = holding[lane];
        if (n == null) return null;
        holding[lane] = null;
        if (time >= n.endTime() - HOLD_TOLERANCE) {
            completeHold(n);
        } else {
            n.holdState = Note.HoldState.BROKEN;
            n.heldSeconds = Math.max(0, Math.min(n.duration, time - n.time));
            score.breakCombo();
        }
        return n;
    }

    /**
     * Knob girado. Acerta uma nota de giro dessa direção perto da linha.
     * Giros sem nota não contam como erro: o knob manda vários passos por volta.
     *
     * @param direction {@link Note#SPIN_LEFT} ou {@link Note#SPIN_RIGHT}
     * @return a nota de giro acertada, ou null
     */
    public Note spin(int direction, double time) {
        Note n = findPending(time, note -> note.type == Note.Type.SPIN && note.direction == direction);
        if (n != null) resolveHit(n, time);
        return n;
    }

    private interface Match {
        boolean test(Note n);
    }

    /** A primeira nota pendente que combina e está dentro da janela de acerto. */
    private Note findPending(double time, Match match) {
        double window = judge.hitWindow();
        List<Note> notes = chart.notes();
        for (int i = cursor; i < notes.size(); i++) {
            Note n = notes.get(i);
            if (n.time - time > window) break; // daqui em diante, todas cedo demais
            if (n.isPending() && match.test(n) && Math.abs(time - n.time) <= window) return n;
        }
        return null;
    }

    private void resolveHit(Note n, double time) {
        n.state = Note.State.HIT;
        n.offset = time - n.time;
        n.judgement = judge.judge(n.offset);
        hits++;
        score.hit(n.judgement);
        advanceCursor();
    }

    private void completeHold(Note n) {
        n.holdState = Note.HoldState.COMPLETED;
        n.heldSeconds = n.duration;
        score.holdCompleted();
    }

    /** Marca notas perdidas e completa notas longas seguradas até o fim. Chamar a cada frame. */
    public void update(double time, Listener listener) {
        double window = judge.hitWindow();
        List<Note> notes = chart.notes();
        for (int i = cursor; i < notes.size(); i++) {
            Note n = notes.get(i);
            if (time - n.time <= window) break;
            if (n.isPending()) {
                n.state = Note.State.MISSED;
                n.judgement = Judgement.MISS;
                misses++;
                score.breakCombo();
                listener.onMiss(n);
            }
        }
        advanceCursor();

        for (int lane = 0; lane < LANES; lane++) {
            Note n = holding[lane];
            if (n != null && time >= n.endTime()) {
                holding[lane] = null;
                completeHold(n);
                listener.onHoldCompleted(n);
            }
        }
    }

    private void advanceCursor() {
        List<Note> notes = chart.notes();
        while (cursor < notes.size() && !notes.get(cursor).isPending()) cursor++;
    }

    public Chart chart() {
        return chart;
    }

    public Judge judge() {
        return judge;
    }

    public Score score() {
        return score;
    }

    /** Todas as notas já foram resolvidas e nenhuma nota longa está sendo segurada. */
    public boolean isFinished() {
        if (hits + misses != chart.size()) return false;
        for (Note n : holding) if (n != null) return false;
        return true;
    }

    public int hits() {
        return hits;
    }

    /** Notas que passaram sem toque. */
    public int misses() {
        return misses;
    }

    /** Toques sem nota na trilha (dedo errado ou fora de hora), em todas as trilhas. */
    public int strays() {
        int total = 0;
        for (int s : straysByLane) total += s;
        return total;
    }

    /** Toques sem nota numa trilha: mostra qual dedo "escapa". */
    public int strays(int lane) {
        return straysByLane[lane];
    }

    /** Acertos ÷ total de notas, de 0 a 1. */
    public double accuracy() {
        return chart.size() == 0 ? 0 : (double) hits / chart.size();
    }
}
