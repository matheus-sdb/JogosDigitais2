package io.github.triotiles.game;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SessionHistoryTest {
    /** Uma sessão de verdade: 2 acertos (100 ms adiantado e 50 ms atrasado) e 1 nota perdida. */
    private static SessionRecord played(String patient, String date) {
        Playfield f = new Playfield(new Chart(List.of(
            Note.tap(0, 1.0), Note.tap(1, 2.0), Note.tap(2, 3.0))), Judge.standard());
        f.press(0, 0.9);
        f.press(1, 2.05);
        f.update(10);
        Settings s = new Settings();
        s.setPatient(patient);
        s.setHand(Settings.Hand.LEFT);
        s.setLatencyMs(40);
        return SessionRecord.of(SessionStats.from(f), LevelData.AQUECIMENTO, s, date);
    }

    @Test
    void recordKeepsTheMetricsAndSettings() {
        SessionRecord r = played("Ana", "2026-11-04T10:00");
        assertEquals("Ana", r.patient);
        assertEquals(1, r.level);
        assertEquals(2.0 / 3, r.accuracy, 1e-9);
        assertEquals(75, r.speedMs, 1e-6);
        assertEquals(-25, r.tendencyMs, 1e-6);
        assertNull(r.sustain, "fase sem notas longas");
        assertEquals("anelar", r.fingers[0]);
        assertEquals(1.0, r.laneAccuracy[0], 1e-9);
        assertEquals(0.0, r.laneAccuracy[2], 1e-9);
        assertEquals("Esquerda", r.hand);
        assertEquals(40, r.latencyMs);
    }

    @Test
    void jsonRoundTrip() {
        SessionHistory h = new SessionHistory();
        h.add(played("Ana", "2026-11-04T10:00"));
        h.add(played("Bruno", "2026-11-04T11:00"));

        String json = h.toJson();
        assertFalse(json.contains("NaN"), "JSON válido não tem NaN");
        SessionHistory back = SessionHistory.fromJson(json);

        assertEquals(2, back.all().size());
        SessionRecord r = back.all().get(0);
        assertEquals("Ana", r.patient);
        assertEquals("2026-11-04T10:00", r.date);
        assertEquals(75, r.speedMs, 1e-6);
        assertNull(r.sustain);
        assertEquals(3, r.laneAccuracy.length);
        assertEquals("anelar", r.fingers[0]);
    }

    @Test
    void emptyOrMissingFileGivesEmptyHistory() {
        assertTrue(SessionHistory.fromJson("").all().isEmpty());
        assertTrue(SessionHistory.fromJson(null).all().isEmpty());
        assertTrue(SessionHistory.fromJson(new SessionHistory().toJson()).all().isEmpty());
    }

    @Test
    void filtersByPatientIgnoringCase() {
        SessionHistory h = new SessionHistory();
        h.add(played("Ana", "d1"));
        h.add(played("Bruno", "d2"));
        h.add(played("ana", "d3"));
        List<SessionRecord> ana = h.forPatient("ANA");
        assertEquals(2, ana.size());
        assertEquals("d3", ana.get(1).date);
    }

    @Test
    void latencySuggestionUndoesTheAdjustmentUsedInEachSession() {
        SessionHistory h = new SessionHistory();
        h.add(played("Ana", "d1")); // tendência -25 ms jogando com ajuste de +40 ms
        h.add(played("Ana", "d2"));
        assertTrue(Double.isNaN(h.suggestedLatencyMs("Ana", 3)), "poucas sessões");
        h.add(played("Ana", "d3"));
        assertEquals(15, h.suggestedLatencyMs("Ana", 3), 1e-6); // sem ajuste, atrasaria -25 + 40 = 15 ms
    }

    @Test
    void csvUsesSemicolonsAndDecimalComma() {
        SessionHistory h = new SessionHistory();
        h.add(played("Ana", "2026-11-04T10:00"));
        String[] lines = h.toCsv().split("\n");
        assertEquals(2, lines.length);
        assertTrue(lines[0].startsWith("data;paciente;fase;precisao_%"));
        assertTrue(lines[1].startsWith("2026-11-04T10:00;Ana;1;66,7;75,0;"), lines[1]);
        assertEquals(lines[0].split(";").length, lines[1].split(";", -1).length, "mesmo número de colunas");
    }
}
