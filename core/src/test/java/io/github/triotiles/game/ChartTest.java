package io.github.triotiles.game;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ChartTest {
    @Test
    void randomTapsHasTheRequestedRhythm() {
        Chart c = Chart.randomTaps(42, 10, 3.0, 0.5);
        assertEquals(10, c.size());
        assertEquals(3.0, c.notes().get(0).time, 1e-9);
        assertEquals(7.5, c.endTime(), 1e-9);
    }

    @Test
    void sameSeedGivesSameSequence() {
        List<Note> a = Chart.randomTaps(7, 50, 0, 1).notes();
        List<Note> b = Chart.randomTaps(7, 50, 0, 1).notes();
        for (int i = 0; i < a.size(); i++) assertEquals(a.get(i).lane, b.get(i).lane);
    }

    @Test
    void neverThreeInARowOnTheSameLaneAndUsesAllLanes() {
        List<Note> notes = Chart.randomTaps(123, 300, 0, 1).notes();
        int[] count = new int[Playfield.LANES];
        for (int i = 0; i < notes.size(); i++) {
            count[notes.get(i).lane]++;
            if (i >= 2) {
                boolean three = notes.get(i).lane == notes.get(i - 1).lane
                        && notes.get(i).lane == notes.get(i - 2).lane;
                assertFalse(three, "3 seguidas na trilha " + notes.get(i).lane + " na nota " + i);
            }
        }
        for (int lane = 0; lane < Playfield.LANES; lane++) assertTrue(count[lane] > 60, "trilha " + lane + " pouco usada");
    }

    @Test
    void notesAreSortedByTime() {
        Chart c = new Chart(List.of(new Note(0, 3), new Note(1, 1), new Note(2, 2)));
        assertEquals(1, c.notes().get(0).time);
        assertEquals(3, c.endTime());
    }
}
