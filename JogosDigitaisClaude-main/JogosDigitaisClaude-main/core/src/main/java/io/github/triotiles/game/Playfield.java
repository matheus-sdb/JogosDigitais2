package io.github.triotiles.game;

import java.util.List;
import java.util.function.Consumer;

/**
 * Regras da fase, sem nada de desenho: decide se um toque acertou uma nota
 * e marca como perdidas as notas que passaram da linha sem toque.
 * Tempos em segundos desde o início da fase.
 */
public class Playfield {
    public static final int LANES = 3;

    private final Chart chart;
    private final double hitWindow;
    /** Todas as notas antes deste índice já foram resolvidas (acertadas ou perdidas). */
    private int cursor;

    private int hits;
    private int misses;
    private int strays;

    /** @param hitWindow quanto antes ou depois do momento ideal o toque ainda vale, em segundos */
    public Playfield(Chart chart, double hitWindow) {
        this.chart = chart;
        this.hitWindow = hitWindow;
    }

    /**
     * Tecla da trilha apertada no momento {@code time}.
     *
     * @return a nota acertada, ou null se não havia nota dessa trilha perto da linha
     *         (conta como "tecla errada": dedo apertou sem precisar)
     */
    public Note press(int lane, double time) {
        List<Note> notes = chart.notes();
        for (int i = cursor; i < notes.size(); i++) {
            Note n = notes.get(i);
            if (n.time - time > hitWindow) break; // daqui em diante, todas cedo demais
            if (n.lane == lane && n.isPending() && Math.abs(time - n.time) <= hitWindow) {
                n.state = Note.State.HIT;
                n.offset = time - n.time;
                hits++;
                advanceCursor();
                return n;
            }
        }
        strays++;
        return null;
    }

    /** Marca como perdidas as notas cuja janela de acerto já passou. Chamar a cada frame. */
    public void update(double time) {
        update(time, null);
    }

    /** Igual a {@link #update(double)}, avisando {@code onMiss} de cada nota perdida agora. */
    public void update(double time, Consumer<Note> onMiss) {
        List<Note> notes = chart.notes();
        for (int i = cursor; i < notes.size(); i++) {
            Note n = notes.get(i);
            if (time - n.time <= hitWindow) break;
            if (n.isPending()) {
                n.state = Note.State.MISSED;
                misses++;
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

    public double hitWindow() {
        return hitWindow;
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

    /** Toques sem nota na trilha (dedo errado ou fora de hora). */
    public int strays() {
        return strays;
    }

    /** Acertos ÷ total de notas, de 0 a 1. */
    public double accuracy() {
        return chart.size() == 0 ? 0 : (double) hits / chart.size();
    }
}
