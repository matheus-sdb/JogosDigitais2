package io.github.triotiles.game;

import org.junit.jupiter.api.Test;

import static io.github.triotiles.game.LevelData.*;
import static org.junit.jupiter.api.Assertions.*;

class ProgressTest {
    @Test
    void onlyTheFirstLevelStartsUnlocked() {
        Progress p = new Progress();
        assertTrue(p.isUnlocked(AQUECIMENTO));
        assertFalse(p.isUnlocked(RITMO));
        assertFalse(p.isUnlocked(DESTREZA));
        assertTrue(Double.isNaN(p.best(AQUECIMENTO)));
    }

    @Test
    void reachingTheGoalUnlocksTheNextLevelOnce() {
        Progress p = new Progress();
        assertNull(p.record(AQUECIMENTO, 0.60), "abaixo da meta de 70%");
        assertFalse(p.isUnlocked(RITMO));

        assertEquals(RITMO, p.record(AQUECIMENTO, 0.72));
        assertTrue(p.isUnlocked(RITMO));
        assertNull(p.record(AQUECIMENTO, 0.90), "já estava liberada");

        assertNull(p.record(RITMO, 0.74), "meta da fase 2 é 75%");
        assertEquals(DESTREZA, p.record(RITMO, 0.75));
        assertNull(p.record(DESTREZA, 0.10), "não há fase depois da última");
    }

    @Test
    void keepsTheBestAccuracy() {
        Progress p = new Progress();
        p.record(AQUECIMENTO, 0.5);
        p.record(AQUECIMENTO, 0.8);
        p.record(AQUECIMENTO, 0.6);
        assertEquals(0.8, p.best(AQUECIMENTO), 1e-9);
    }

    @Test
    void restoreFixesInvalidValues() {
        Progress p = Progress.restore(9, new double[]{0.9, Double.NaN, Double.NaN});
        assertEquals(3, p.unlockedCount());
        assertEquals(0.9, p.best(AQUECIMENTO), 1e-9);
        assertEquals(1, Progress.restore(0, new double[0]).unlockedCount());
    }
}
