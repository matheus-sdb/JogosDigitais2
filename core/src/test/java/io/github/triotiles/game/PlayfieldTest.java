package io.github.triotiles.game;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PlayfieldTest {
    private static final double WINDOW = 0.25;

    private static Playfield field(Note... notes) {
        return new Playfield(new Chart(List.of(notes)), WINDOW);
    }

    @Test
    void pressingTheRightLaneInsideTheWindowHitsTheNote() {
        Note note = new Note(1, 2.0);
        Playfield f = field(note);

        assertSame(note, f.press(1, 2.1));
        assertEquals(Note.State.HIT, note.state());
        assertEquals(0.1, note.offset(), 1e-9);
        assertEquals(1, f.hits());
        assertTrue(f.isFinished());
    }

    @Test
    void earlyPressIsNegativeOffset() {
        Note note = new Note(0, 2.0);
        field(note).press(0, 1.8);
        assertEquals(-0.2, note.offset(), 1e-9);
    }

    @Test
    void wrongLaneIsAStrayAndTheNoteStaysPending() {
        Note note = new Note(0, 2.0);
        Playfield f = field(note);

        assertNull(f.press(2, 2.0));
        assertEquals(1, f.strays());
        assertTrue(note.isPending());
    }

    @Test
    void pressingTooEarlyIsAStray() {
        Note note = new Note(0, 2.0);
        Playfield f = field(note);

        assertNull(f.press(0, 2.0 - WINDOW - 0.01));
        assertEquals(1, f.strays());
        assertTrue(note.isPending());
    }

    @Test
    void noteIsMissedOnlyAfterTheWindowPasses() {
        Note note = new Note(0, 2.0);
        Playfield f = field(note);

        f.update(2.0 + WINDOW);
        assertTrue(note.isPending(), "ainda dentro da janela");

        f.update(2.0 + WINDOW + 0.01);
        assertEquals(Note.State.MISSED, note.state());
        assertEquals(1, f.misses());
        assertTrue(f.isFinished());
    }

    @Test
    void aNoteCannotBeHitTwice() {
        Playfield f = field(new Note(0, 2.0));
        assertNotNull(f.press(0, 2.0));
        assertNull(f.press(0, 2.05));
        assertEquals(1, f.hits());
        assertEquals(1, f.strays());
    }

    @Test
    void hitsTheEarliestPendingNoteOfThatLane() {
        Note first = new Note(0, 2.0);
        Note second = new Note(0, 2.2);
        Playfield f = field(second, first); // fora de ordem de propósito

        assertSame(first, f.press(0, 2.1));
        assertSame(second, f.press(0, 2.15));
    }

    @Test
    void otherLanesAreIndependent() {
        Note a = new Note(0, 2.0);
        Note b = new Note(2, 2.0);
        Playfield f = field(a, b);

        assertSame(b, f.press(2, 2.0));
        f.update(3.0);
        assertEquals(Note.State.MISSED, a.state());
        assertEquals(1, f.hits());
        assertEquals(1, f.misses());
        assertEquals(0.5, f.accuracy(), 1e-9);
    }
}
