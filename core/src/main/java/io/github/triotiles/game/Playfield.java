package io.github.triotiles.game;

import java.util.List;
import java.util.function.Consumer;

/**
 * Regras da fase, sem nada de desenho: decide se um toque acertou uma nota (e quão bem),
 * marca como perdidas as notas que passaram da linha sem toque e cuida da pontuação.
 * Tempos em segundos desde o início da fase.
 */
public class Playfield {
    public static final int LANES = 3;

    private final Chart chart;
    private final Judge judge;
    private final Score score = new Score();
    /** Todas as notas antes deste índice já foram resolvidas (acertadas ou perdidas). */
    private int cursor;

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
        double window = judge.hitWindow();
        List<Note> notes = chart.notes();
        for (int i = cursor; i < notes.size(); i++) {
            Note n = notes.get(i);
            if (n.time - time > window) break; // daqui em diante, todas cedo demais
            if (n.lane == lane && n.isPending() && Math.abs(time - n.time) <= window) {
                n.state = Note.State.HIT;
                n.offset = time - n.time;
                n.judgement = judge.judge(n.offset);
                hits++;
                score.hit(n.judgement);
                advanceCursor();
                return n;
            }
        }
        straysByLane[lane]++;
        score.breakCombo();
        return null;
    }

    /** Marca como perdidas as notas cuja janela de acerto já passou. Chamar a cada frame. */
    public void update(double time) {
        update(time, null);
    }

    /** Igual a {@link #update(double)}, avisando {@code onMiss} de cada nota perdida agora. */
    public void update(double time, Consumer<Note> onMiss) {
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
                if (onMiss != null) onMiss.accept(n);
            }
        }
        advanceCursor();
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

    /** Todas as notas já foram acertadas ou perdidas. */
    public boolean isFinished() {
        return hits + misses == chart.size();
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
