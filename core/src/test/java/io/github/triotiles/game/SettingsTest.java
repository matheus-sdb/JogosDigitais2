package io.github.triotiles.game;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SettingsTest {
    @Test
    void handChangesWhichFingerIsOnEachKey() {
        assertEquals("indicador", Settings.Hand.RIGHT.finger(0));
        assertEquals("anelar", Settings.Hand.RIGHT.finger(2));
        assertEquals("anelar", Settings.Hand.LEFT.finger(0));
        assertEquals("indicador", Settings.Hand.LEFT.finger(2));
        assertEquals("médio", Settings.Hand.LEFT.finger(1));
    }

    @Test
    void patientNameIsCleanedAndNeverEmpty() {
        Settings s = new Settings();
        s.setPatient("  Ana  ");
        assertEquals("Ana", s.patient());
        s.setPatient("   ");
        assertEquals(Settings.DEFAULT_PATIENT, s.patient());
        s.setPatient("Um nome muito comprido demais");
        assertTrue(s.patient().length() <= Settings.MAX_NAME);
    }

    @Test
    void latencyAndVolumeStayInRange() {
        Settings s = new Settings();
        s.setLatencyMs(999);
        assertEquals(Settings.LATENCY_MAX_MS, s.latencyMs());
        s.setLatencyMs(-999);
        assertEquals(Settings.LATENCY_MIN_MS, s.latencyMs());
        s.setLatencyMs(64);
        assertEquals(60, s.latencyMs(), "arredonda para passos de 10 ms");
        s.setVolumePercent(150);
        assertEquals(100, s.volumePercent());
        s.setVolumePercent(-5);
        assertEquals(0, s.volumePercent());
    }

    @Test
    void windowSettingScalesTheJudge() {
        Settings s = new Settings();
        s.setWindow(Settings.Window.WIDE);
        assertEquals(0.25 * 1.25, s.judgeFor(LevelData.RITMO).hitWindow(), 1e-9);
        s.setWindow(Settings.Window.NARROW);
        assertEquals(Judgement.MISS, s.judgeFor(LevelData.RITMO).judge(0.21));
    }

    @Test
    void slowTempoSpacesTheNotesAndKeepsTheDuration() {
        Settings slow = new Settings();
        slow.setTempo(Settings.Tempo.SLOW);
        Chart normal = new Settings().chartFor(LevelData.AQUECIMENTO, 5);
        Chart slower = slow.chartFor(LevelData.AQUECIMENTO, 5);

        assertTrue(slower.size() < normal.size(), "menos notas no mesmo tempo");
        double gap = slower.notes().get(1).time - slower.notes().get(0).time;
        assertEquals(LevelData.AQUECIMENTO.interval / 0.8, gap, 1e-9);
        assertEquals(normal.endTime(), slower.endTime(), 2.0);
        assertTrue(slow.speedFor(LevelData.AQUECIMENTO) < LevelData.AQUECIMENTO.speed);
    }
}
