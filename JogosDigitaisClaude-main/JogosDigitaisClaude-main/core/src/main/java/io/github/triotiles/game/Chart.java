package io.github.triotiles.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

/** A sequência de notas de uma fase, em ordem de tempo. */
public class Chart {
    private final List<Note> notes;

    public Chart(List<Note> notes) {
        List<Note> sorted = new ArrayList<>(notes);
        sorted.sort(Comparator.comparingDouble(n -> n.time));
        this.notes = Collections.unmodifiableList(sorted);
    }

    /**
     * Notas simples, uma por vez, em ritmo constante e trilhas sorteadas.
     * Evita a mesma trilha muitas vezes seguidas, para todos os dedos trabalharem.
     *
     * @param seed      mesma semente = mesma sequência (útil para comparar sessões)
     * @param firstTime momento da primeira nota, em segundos
     * @param interval  tempo entre notas, em segundos
     */
    public static Chart randomTaps(long seed, int count, double firstTime, double interval) {
        Random random = new Random(seed);
        List<Note> notes = new ArrayList<>(count);
        int previous = -1;
        int repeats = 0;
        for (int i = 0; i < count; i++) {
            int lane = random.nextInt(Playfield.LANES);
            if (lane == previous && (repeats >= 1 || random.nextBoolean())) {
                lane = (lane + 1 + random.nextInt(Playfield.LANES - 1)) % Playfield.LANES;
            }
            repeats = lane == previous ? repeats + 1 : 0;
            previous = lane;
            notes.add(new Note(lane, firstTime + i * interval));
        }
        return new Chart(notes);
    }

    public List<Note> notes() {
        return notes;
    }

    public int size() {
        return notes.size();
    }

    /** Momento da última nota (0 se não houver notas). */
    public double endTime() {
        return notes.isEmpty() ? 0 : notes.get(notes.size() - 1).time;
    }
}
