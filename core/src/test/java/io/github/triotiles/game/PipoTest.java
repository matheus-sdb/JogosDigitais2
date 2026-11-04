package io.github.triotiles.game;

import org.junit.jupiter.api.Test;

import static io.github.triotiles.game.Pipo.Mood.*;
import static org.junit.jupiter.api.Assertions.*;

class PipoTest {
    @Test
    void startsIdle() {
        assertEquals(IDLE, new Pipo().mood());
    }

    @Test
    void hitMakesHimHappyForAMoment() {
        Pipo p = new Pipo();
        p.onHit(2, 1);
        assertEquals(HAPPY, p.mood());
        assertEquals(2, p.lastLane());

        p.update(Pipo.HAPPY_TIME / 2);
        assertEquals(HAPPY, p.mood());
        assertEquals(0.5f, p.moodProgress(), 1e-4);

        p.update(Pipo.HAPPY_TIME);
        assertEquals(IDLE, p.mood());
    }

    @Test
    void newHitRestartsTheJump() {
        Pipo p = new Pipo();
        p.onHit(0, 1);
        p.update(0.3f);
        p.onHit(1, 2);
        assertEquals(0, p.moodTime(), 1e-6);
    }

    @Test
    void missMakesHimStumbleThenRecover() {
        Pipo p = new Pipo();
        p.onMiss();
        assertEquals(OOPS, p.mood());
        p.update(Pipo.OOPS_TIME + 0.01f);
        assertEquals(IDLE, p.mood());
    }

    @Test
    void highComboMakesHimDanceUntilAMiss() {
        Pipo p = new Pipo();
        p.onHit(0, Pipo.DANCE_COMBO);
        assertEquals(DANCING, p.mood());
        p.update(10f);
        assertEquals(DANCING, p.mood(), "dança não acaba sozinha");

        p.onMiss();
        assertEquals(OOPS, p.mood());
    }
}
