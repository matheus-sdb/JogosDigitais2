package io.github.triotiles.game;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ScoreTest {
    @Test
    void multiplierGoesUpEveryTenHitsUpToFour() {
        Score s = new Score();
        assertEquals(1, s.multiplier());
        for (int i = 0; i < 9; i++) s.hit(Judgement.PERFECT);
        assertEquals(1, s.multiplier());
        assertEquals(900, s.points());

        s.hit(Judgement.PERFECT); // 10º seguido: já vale 2x
        assertEquals(2, s.multiplier());
        assertEquals(1100, s.points());

        for (int i = 0; i < 40; i++) s.hit(Judgement.OK);
        assertEquals(4, s.multiplier());
        assertEquals(50, s.maxCombo());
    }

    @Test
    void breakingTheComboResetsTheMultiplierButKeepsPoints() {
        Score s = new Score();
        for (int i = 0; i < 12; i++) s.hit(Judgement.GOOD);
        int before = s.points();

        s.breakCombo();
        assertEquals(0, s.combo());
        assertEquals(1, s.multiplier());
        assertEquals(before, s.points());
        assertEquals(12, s.maxCombo());
    }
}
