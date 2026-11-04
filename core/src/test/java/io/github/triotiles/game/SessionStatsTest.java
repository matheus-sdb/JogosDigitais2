package io.github.triotiles.game;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SessionStatsTest {
    private static Playfield field(Note... notes) {
        return new Playfield(new Chart(List.of(notes)), Judge.standard());
    }

    @Test
    void speedConsistencyAndTendency() {
        Playfield f = field(new Note(0, 1.0), new Note(1, 2.0), new Note(2, 3.0), new Note(0, 4.0));
        f.press(0, 1.0 - 0.100); // 100 ms adiantado
        f.press(1, 2.0 + 0.100); // 100 ms atrasado
        f.press(2, 3.0 - 0.050); //  50 ms adiantado
        f.press(0, 4.0 + 0.050); //  50 ms atrasado

        SessionStats s = SessionStats.from(f);
        assertEquals(75, s.meanAbsOffsetMs, 1e-6);   // velocidade
        assertEquals(0, s.meanOffsetMs, 1e-6);       // tendência: equilibrado
        // desvios 100, 100, 50, 50 → variância (10000+10000+2500+2500)/4 = 6250
        assertEquals(Math.sqrt(6250), s.stdDevOffsetMs, 1e-6);
        assertEquals(1.0, s.accuracy, 1e-9);
        assertEquals(2, s.count(Judgement.GOOD));
        assertEquals(2, s.count(Judgement.PERFECT));
    }

    @Test
    void perFingerAccuracyAndStrays() {
        Playfield f = field(
            new Note(0, 1.0), new Note(0, 2.0),   // dedo 1: acerta as 2
            new Note(1, 3.0), new Note(1, 4.0),   // dedo 2: acerta 1 de 2
            new Note(2, 5.0));                     // dedo 3: perde
        f.press(0, 1.0);
        f.press(0, 2.0);
        f.press(1, 3.0);
        f.press(2, 3.5); // tecla errada no dedo 3
        f.update(10.0);

        SessionStats s = SessionStats.from(f);
        assertEquals(1.0, s.laneAccuracy(0), 1e-9);
        assertEquals(0.5, s.laneAccuracy(1), 1e-9);
        assertEquals(0.0, s.laneAccuracy(2), 1e-9);
        assertEquals(1, s.laneStrays(2));
        assertEquals(1, s.strays);
        assertEquals(2, s.count(Judgement.MISS));
        assertEquals(2, s.weakestLane(0.10));
        assertEquals(0.6, s.accuracy, 1e-9);
    }

    @Test
    void fingersCloseTogetherHaveNoWeakest() {
        Playfield f = field(new Note(0, 1.0), new Note(1, 2.0), new Note(2, 3.0));
        f.press(0, 1.0);
        f.press(1, 2.0);
        f.press(2, 3.0);
        assertEquals(-1, SessionStats.from(f).weakestLane(0.10));
    }

    @Test
    void withoutEnoughHitsMetricsAreNaN() {
        Playfield none = field(new Note(0, 1.0));
        none.update(5.0);
        SessionStats s = SessionStats.from(none);
        assertTrue(Double.isNaN(s.meanAbsOffsetMs));
        assertTrue(Double.isNaN(s.stdDevOffsetMs));

        Playfield one = field(new Note(0, 1.0));
        one.press(0, 1.03);
        SessionStats t = SessionStats.from(one);
        assertEquals(30, t.meanAbsOffsetMs, 1e-6);
        assertTrue(Double.isNaN(t.stdDevOffsetMs), "consistência precisa de 2 acertos");
    }
}
