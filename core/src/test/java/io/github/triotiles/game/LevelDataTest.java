package io.github.triotiles.game;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LevelDataTest {
    @Test
    void difficultyGrowsFromLevelToLevel() {
        LevelData[] levels = LevelData.values();
        assertEquals(3, levels.length);
        for (int i = 1; i < levels.length; i++) {
            assertTrue(levels[i].interval < levels[i - 1].interval, "notas mais frequentes");
            assertTrue(levels[i].speed > levels[i - 1].speed, "notas mais rápidas");
            assertTrue(levels[i].judge().hitWindow() < levels[i - 1].judge().hitWindow(), "janela mais estreita");
            assertTrue(levels[i].durationSeconds > levels[i - 1].durationSeconds, "fase mais longa");
        }
    }

    @Test
    void level1HasOnlySimpleNotes() {
        Chart c = LevelData.AQUECIMENTO.chart(1);
        assertEquals(c.size(), c.count(Note.Type.TAP));
        assertFalse(hasChord(c));
    }

    @Test
    void level2AddsHoldsButNoChordsOrSpins() {
        Chart c = LevelData.RITMO.chart(2);
        assertTrue(c.count(Note.Type.HOLD) > 0);
        assertEquals(0, c.count(Note.Type.SPIN));
        assertFalse(hasChord(c));
    }

    @Test
    void level3HasEverything() {
        Chart c = LevelData.DESTREZA.chart(3);
        assertTrue(c.count(Note.Type.HOLD) > 0);
        assertTrue(c.count(Note.Type.SPIN) > 0);
        assertTrue(hasChord(c));
    }

    @Test
    void chartsLastAboutTheLevelDurationAndStartWithWarmup() {
        for (LevelData level : LevelData.values()) {
            Chart c = level.chart(42);
            double last = c.notes().get(c.size() - 1).time;
            assertTrue(last <= Chart.FIRST_NOTE + level.durationSeconds, level + " passou da duração");
            assertTrue(c.endTime() > level.durationSeconds * 0.9, level + " curta demais");
            for (int i = 0; i < Chart.WARMUP_NOTES; i++) {
                assertEquals(Note.Type.TAP, c.notes().get(i).type, level + ": aquecimento com notas simples");
            }
        }
    }

    @Test
    void nothingHappensWhileAHoldIsBeingHeld() {
        for (LevelData level : LevelData.values()) {
            List<Note> notes = level.chart(7).notes();
            for (Note hold : notes) {
                if (hold.type != Note.Type.HOLD) continue;
                for (Note other : notes) {
                    if (other == hold) continue;
                    boolean during = other.time > hold.time - 1e-9 && other.time < hold.endTime() + level.interval - 1e-9;
                    assertFalse(during, level + ": nota durante a nota longa em " + hold.time);
                }
            }
        }
    }

    @Test
    void goalsAndNextLevel() {
        assertEquals(LevelData.RITMO, LevelData.AQUECIMENTO.next());
        assertNull(LevelData.DESTREZA.next());
        assertTrue(LevelData.AQUECIMENTO.goalReached(0.70));
        assertFalse(LevelData.AQUECIMENTO.goalReached(0.69));
        assertFalse(LevelData.DESTREZA.hasGoal());
        assertTrue(LevelData.DESTREZA.goalReached(0), "última fase: basta concluir");
        assertEquals(LevelData.RITMO, LevelData.byNumber(2));
        assertEquals("Fase 1: Aquecimento", LevelData.AQUECIMENTO.fullTitle());
    }

    private static boolean hasChord(Chart c) {
        List<Note> n = c.notes();
        for (int i = 1; i < n.size(); i++) {
            if (n.get(i).time == n.get(i - 1).time) return true;
        }
        return false;
    }
}
