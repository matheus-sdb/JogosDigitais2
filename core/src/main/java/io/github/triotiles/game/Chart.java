package io.github.triotiles.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

/** A sequência de notas de uma fase, em ordem de tempo. */
public class Chart {
    /** Segundos até a primeira nota: tempo para o paciente se preparar. */
    public static final double FIRST_NOTE = 3.0;
    /** As primeiras notas de toda fase são simples, para aquecer. */
    static final int WARMUP_NOTES = 4;

    private final List<Note> notes;

    public Chart(List<Note> notes) {
        List<Note> sorted = new ArrayList<>(notes);
        sorted.sort(Comparator.comparingDouble((Note n) -> n.time).thenComparingInt(n -> n.lane));
        this.notes = Collections.unmodifiableList(sorted);
    }

    /**
     * Notas simples, uma por vez, em ritmo constante e trilhas sorteadas.
     *
     * @param seed      mesma semente = mesma sequência (útil para comparar sessões)
     * @param firstTime momento da primeira nota, em segundos
     * @param interval  tempo entre notas, em segundos
     */
    public static Chart randomTaps(long seed, int count, double firstTime, double interval) {
        Random random = new Random(seed);
        LanePicker picker = new LanePicker(random);
        List<Note> notes = new ArrayList<>(count);
        for (int i = 0; i < count; i++) notes.add(Note.tap(picker.next(), firstTime + i * interval));
        return new Chart(notes);
    }

    /**
     * Sequência de uma fase: em ritmo constante, cada evento pode ser nota simples, longa,
     * acorde (duas teclas juntas) ou giro do knob, conforme as chances da fase.
     * Depois de uma nota longa vem uma pausa, para o dedo soltar antes da próxima.
     */
    public static Chart generate(LevelData level, long seed) {
        return generate(level, seed, 1.0);
    }

    /**
     * Igual a {@link #generate(LevelData, long)}, com o ritmo ajustado: {@code tempo} 0.8 deixa
     * as notas 25% mais espaçadas, 1.2 deixa mais juntas. A duração da fase não muda.
     */
    public static Chart generate(LevelData level, long seed, double tempo) {
        Random random = new Random(seed);
        LanePicker picker = new LanePicker(random);
        List<Note> notes = new ArrayList<>();
        double interval = level.interval / tempo;
        double t = FIRST_NOTE;
        double end = FIRST_NOTE + level.durationSeconds;
        int events = 0;
        while (t <= end) {
            double roll = events < WARMUP_NOTES ? 1 : random.nextDouble();
            events++;
            if ((roll -= level.spinChance) < 0) {
                notes.add(Note.spin(random.nextBoolean() ? Note.SPIN_LEFT : Note.SPIN_RIGHT, t));
                picker.reset();
                t += interval;
            } else if ((roll -= level.chordChance) < 0) {
                int a = random.nextInt(Playfield.LANES);
                int b = (a + 1 + random.nextInt(Playfield.LANES - 1)) % Playfield.LANES;
                notes.add(Note.tap(a, t));
                notes.add(Note.tap(b, t));
                picker.reset();
                t += interval;
            } else if ((roll -= level.holdChance) < 0) {
                double duration = interval * (1 + random.nextInt(2));
                notes.add(Note.hold(picker.next(), t, duration));
                t += duration + interval;
            } else {
                notes.add(Note.tap(picker.next(), t));
                t += interval;
            }
        }
        return new Chart(notes);
    }

    /** Sorteia trilhas evitando 3 seguidas na mesma, para todos os dedos trabalharem. */
    private static final class LanePicker {
        private final Random random;
        private int previous = -1;
        private int repeats;

        LanePicker(Random random) {
            this.random = random;
        }

        int next() {
            int lane = random.nextInt(Playfield.LANES);
            if (lane == previous && (repeats >= 1 || random.nextBoolean())) {
                lane = (lane + 1 + random.nextInt(Playfield.LANES - 1)) % Playfield.LANES;
            }
            repeats = lane == previous ? repeats + 1 : 0;
            previous = lane;
            return lane;
        }

        void reset() {
            previous = -1;
            repeats = 0;
        }
    }

    public List<Note> notes() {
        return notes;
    }

    public int size() {
        return notes.size();
    }

    /** Quando a última nota termina (0 se não houver notas). */
    public double endTime() {
        double end = 0;
        for (Note n : notes) end = Math.max(end, n.endTime());
        return end;
    }

    /** Quantas notas desse tipo a sequência tem. */
    public int count(Note.Type type) {
        int c = 0;
        for (Note n : notes) if (n.type == type) c++;
        return c;
    }
}
