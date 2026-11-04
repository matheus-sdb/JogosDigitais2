package io.github.triotiles.input;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntMap;

import java.util.EnumMap;

/** Diz qual tecla (enviada pelo aparelho) corresponde a qual ação. Uma ação pode ter várias teclas. */
public class DeviceMapping {
    private final IntMap<DeviceAction> keyToAction = new IntMap<>();
    private final EnumMap<DeviceAction, Array<String>> keyNames = new EnumMap<>(DeviceAction.class);

    /**
     * Mapeamento padrão (ver docs/PROPOSTA.md, seção 2):
     * teclas 1-3 = A/S/D (ou F13/F14/F15), knob = setas cima/baixo e Enter.
     * Assim o teclado do PC também funciona para testar sem o aparelho.
     */
    public static DeviceMapping standard() {
        DeviceMapping m = new DeviceMapping();
        m.bind(DeviceAction.LANE_1, Keys.A, Keys.F13);
        m.bind(DeviceAction.LANE_2, Keys.S, Keys.F14);
        m.bind(DeviceAction.LANE_3, Keys.D, Keys.F15);
        m.bind(DeviceAction.KNOB_LEFT, Keys.UP);
        m.bind(DeviceAction.KNOB_RIGHT, Keys.DOWN);
        m.bind(DeviceAction.KNOB_PRESS, Keys.ENTER, Keys.NUMPAD_ENTER);
        return m;
    }

    public void bind(DeviceAction action, int... keycodes) {
        for (int keycode : keycodes) {
            keyToAction.put(keycode, action);
            keyNames.computeIfAbsent(action, a -> new Array<>()).add(Keys.toString(keycode));
        }
    }

    /** Ação ligada à tecla, ou null se a tecla não faz parte do aparelho. */
    public DeviceAction actionFor(int keycode) {
        return keyToAction.get(keycode);
    }

    /** Nomes das teclas ligadas à ação, ex.: "A / F13". */
    public String describeKeys(DeviceAction action) {
        Array<String> names = keyNames.get(action);
        return names == null ? "-" : names.toString(" / ");
    }
}
