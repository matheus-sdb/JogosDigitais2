package io.github.triotiles.input;

import com.badlogic.gdx.Input.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DeviceInputTest {
    private long now;
    private DeviceInput input;
    private final List<String> events = new ArrayList<>();

    @BeforeEach
    void setUp() {
        now = 0;
        input = new DeviceInput(DeviceMapping.standard(), () -> now);
        input.setListener(new DeviceListener() {
            @Override
            public void actionDown(DeviceAction action, long timeNanos) {
                events.add("down " + action + " @" + timeNanos);
            }

            @Override
            public void actionUp(DeviceAction action, long timeNanos, long heldNanos) {
                events.add("up " + action + " held " + heldNanos);
            }
        });
    }

    @Test
    void standardMappingCoversAllSixActions() {
        DeviceMapping m = DeviceMapping.standard();
        assertEquals(DeviceAction.LANE_1, m.actionFor(Keys.A));
        assertEquals(DeviceAction.LANE_1, m.actionFor(Keys.F13));
        assertEquals(DeviceAction.LANE_2, m.actionFor(Keys.S));
        assertEquals(DeviceAction.LANE_3, m.actionFor(Keys.D));
        assertEquals(DeviceAction.KNOB_LEFT, m.actionFor(Keys.UP));
        assertEquals(DeviceAction.KNOB_RIGHT, m.actionFor(Keys.DOWN));
        assertEquals(DeviceAction.KNOB_PRESS, m.actionFor(Keys.ENTER));
        assertNull(m.actionFor(Keys.Q));
        assertEquals("S / F14", m.describeKeys(DeviceAction.LANE_2));
    }

    @Test
    void measuresHowLongAKeyWasHeld() {
        now = 1_000;
        input.keyDown(Keys.S);
        assertTrue(input.isPressed(DeviceAction.LANE_2));
        now = 501_000;
        input.keyUp(Keys.S);
        assertFalse(input.isPressed(DeviceAction.LANE_2));
        assertEquals(List.of("down LANE_2 @1000", "up LANE_2 held 500000"), events);
    }

    @Test
    void ignoresAutoRepeatWhileHeld() {
        input.keyDown(Keys.A);
        input.keyDown(Keys.A);
        assertEquals(1, events.size());
    }

    @Test
    void countsSimultaneousLanes() {
        input.keyDown(Keys.A);
        input.keyDown(Keys.D);
        input.keyDown(Keys.ENTER); // knob não é trilha
        assertEquals(2, input.pressedLaneCount());
    }

    @Test
    void unmappedKeysAreNotConsumedButReachRawListener() {
        List<String> raw = new ArrayList<>();
        input.setRawListener((keycode, down, action) -> raw.add(keycode + ":" + down + ":" + action));
        assertFalse(input.keyDown(Keys.Q));
        assertTrue(events.isEmpty());
        assertEquals(List.of(Keys.Q + ":true:null"), raw);
    }

    @Test
    void releaseAllForgetsPressedKeys() {
        input.keyDown(Keys.A);
        input.releaseAll();
        input.keyUp(Keys.A);
        assertEquals(1, events.size()); // não gera "up" para uma tecla esquecida
    }
}
