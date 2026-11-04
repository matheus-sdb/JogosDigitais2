package io.github.triotiles.game;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Notas longas, giros do knob e acordes. */
class SpecialNotesTest {
    private static Playfield field(Note... notes) {
        return new Playfield(new Chart(List.of(notes)), Judge.standard());
    }

    @Test
    void holdHeldUntilTheEndCompletesByItself() {
        Note hold = Note.hold(1, 2.0, 1.0);
        Playfield f = field(hold);
        List<Note> completed = new ArrayList<>();

        assertSame(hold, f.press(1, 2.0));
        assertEquals(Note.HoldState.HOLDING, hold.holdState());
        assertSame(hold, f.holding(1));
        assertFalse(f.isFinished(), "ainda segurando");

        f.update(2.5, new Playfield.Listener() {
            @Override
            public void onHoldCompleted(Note n) {
                completed.add(n);
            }
        });
        assertTrue(completed.isEmpty());

        f.update(3.0, new Playfield.Listener() {
            @Override
            public void onHoldCompleted(Note n) {
                completed.add(n);
            }
        });
        assertEquals(List.of(hold), completed);
        assertEquals(Note.HoldState.COMPLETED, hold.holdState());
        assertEquals(1.0, hold.heldSeconds(), 1e-9);
        assertTrue(f.isFinished());
        assertEquals(100 + Score.HOLD_BONUS, f.score().points());
    }

    @Test
    void releasingAlmostAtTheEndStillCounts() {
        Note hold = Note.hold(0, 2.0, 1.0);
        Playfield f = field(hold);
        f.press(0, 2.0);
        f.release(0, 3.0 - Playfield.HOLD_TOLERANCE);
        assertEquals(Note.HoldState.COMPLETED, hold.holdState());
    }

    @Test
    void releasingEarlyBreaksTheHoldAndTheCombo() {
        Note hold = Note.hold(2, 2.0, 1.0);
        Playfield f = field(hold);
        f.press(2, 2.0);
        assertEquals(1, f.score().combo());

        assertSame(hold, f.release(2, 2.4));
        assertEquals(Note.HoldState.BROKEN, hold.holdState());
        assertEquals(0.4, hold.heldSeconds(), 1e-9);
        assertEquals(0, f.score().combo());
        assertEquals(Note.State.HIT, hold.state(), "o início foi acertado");
        assertNull(f.holding(2));
    }

    @Test
    void releasingAKeyWithoutHoldDoesNothing() {
        Playfield f = field(new Note(0, 2.0));
        f.press(0, 2.0);
        assertNull(f.release(0, 2.1));
        assertEquals(1, f.score().combo());
    }

    @Test
    void missedHoldHeadMissesTheWholeNote() {
        Note hold = Note.hold(0, 2.0, 1.0);
        Playfield f = field(hold);
        f.update(3.5);
        assertEquals(Note.State.MISSED, hold.state());
        assertEquals(0, hold.heldSeconds());
        assertTrue(f.isFinished());
    }

    @Test
    void spinNeedsTheRightDirection() {
        Note spin = Note.spin(Note.SPIN_RIGHT, 2.0);
        Playfield f = field(spin);

        assertNull(f.spin(Note.SPIN_LEFT, 2.0), "lado errado não acerta");
        assertEquals(0, f.strays(), "giro do knob nunca conta como tecla errada");
        assertTrue(spin.isPending());

        assertSame(spin, f.spin(Note.SPIN_RIGHT, 2.05));
        assertEquals(Judgement.PERFECT, spin.judgement());
    }

    @Test
    void keysDoNotHitSpinNotes() {
        Note spin = Note.spin(Note.SPIN_LEFT, 2.0);
        Playfield f = field(spin);
        assertNull(f.press(0, 2.0));
        assertTrue(spin.isPending());
    }

    @Test
    void chordNeedsBothKeys() {
        Note a = Note.tap(0, 2.0);
        Note b = Note.tap(2, 2.0);
        Playfield f = field(a, b);

        assertSame(a, f.press(0, 2.02));
        assertSame(b, f.press(2, 2.03));
        assertEquals(2, f.hits());
        assertEquals(2, f.score().combo());
    }

    @Test
    void sustainAndSpinsAppearInTheStats() {
        Note full = Note.hold(0, 2.0, 1.0);
        Note half = Note.hold(1, 4.0, 1.0);
        Note spinHit = Note.spin(Note.SPIN_LEFT, 6.0);
        Note spinMissed = Note.spin(Note.SPIN_RIGHT, 7.0);
        Playfield f = field(full, half, spinHit, spinMissed);

        f.press(0, 2.0);
        f.update(3.0);
        f.press(1, 4.0);
        f.release(1, 4.5);
        f.spin(Note.SPIN_LEFT, 6.0);
        f.update(9.0);

        SessionStats s = SessionStats.from(f);
        assertEquals(0.75, s.sustain, 1e-9);   // 1,5 s segurados de 2 s
        assertEquals(1, s.holdsCompleted);
        assertEquals(2, s.holdNotes);
        assertEquals(1, s.spinHits);
        assertEquals(2, s.spinNotes);
        assertEquals(1, s.laneNotes(0));
        assertEquals(1, s.laneNotes(1));
        assertEquals(0, s.laneNotes(2), "giros não entram na precisão por dedo");
    }
}
