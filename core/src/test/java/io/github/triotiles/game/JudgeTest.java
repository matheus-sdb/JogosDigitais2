package io.github.triotiles.game;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JudgeTest {
    private final Judge judge = Judge.standard();

    @Test
    void earlyAndLateAreJudgedTheSame() {
        assertEquals(Judgement.PERFECT, judge.judge(0.0));
        assertEquals(Judgement.PERFECT, judge.judge(-0.080));
        assertEquals(Judgement.PERFECT, judge.judge(0.080));
        assertEquals(Judgement.GOOD, judge.judge(-0.081));
        assertEquals(Judgement.GOOD, judge.judge(0.160));
        assertEquals(Judgement.OK, judge.judge(-0.2));
        assertEquals(Judgement.OK, judge.judge(0.250));
        assertEquals(Judgement.MISS, judge.judge(0.251));
    }

    @Test
    void hitWindowIsTheOkWindow() {
        assertEquals(0.25, judge.hitWindow());
    }

    @Test
    void windowsMustBeInOrder() {
        assertThrows(IllegalArgumentException.class, () -> new Judge(0.2, 0.1, 0.3));
        assertThrows(IllegalArgumentException.class, () -> new Judge(0, 0.1, 0.3));
    }
}
