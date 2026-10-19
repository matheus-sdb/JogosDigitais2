package io.github.triotiles.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;

import java.util.Arrays;
import java.util.function.LongSupplier;

/**
 * Ponte entre o teclado de 3 teclas + knob e o jogo.
 * Recebe teclas do libGDX, traduz pelo {@link DeviceMapping} e avisa o {@link DeviceListener} atual.
 */
public class DeviceInput extends InputAdapter {
    private static final long UP = -1;

    private final DeviceMapping mapping;
    private final LongSupplier clock;
    private final long[] downTime = new long[DeviceAction.values().length];

    private DeviceListener listener;
    private RawKeyListener rawListener;

    public DeviceInput(DeviceMapping mapping) {
        // Momento em que o sistema entregou o evento, não o momento em que o frame o processou.
        this(mapping, () -> Gdx.input.getCurrentEventTime());
    }

    public DeviceInput(DeviceMapping mapping, LongSupplier clock) {
        this.mapping = mapping;
        this.clock = clock;
        Arrays.fill(downTime, UP);
    }

    public DeviceMapping getMapping() {
        return mapping;
    }

    public void setListener(DeviceListener listener) {
        this.listener = listener;
    }

    public void setRawListener(RawKeyListener rawListener) {
        this.rawListener = rawListener;
    }

    public boolean isPressed(DeviceAction action) {
        return downTime[action.ordinal()] != UP;
    }

    /** Quantas teclas de trilha estão pressionadas agora. */
    public int pressedLaneCount() {
        int count = 0;
        for (DeviceAction a : DeviceAction.values()) {
            if (a.lane && isPressed(a)) count++;
        }
        return count;
    }

    /** Esquece teclas pressionadas, ex.: ao trocar de tela. */
    public void releaseAll() {
        Arrays.fill(downTime, UP);
    }

    @Override
    public boolean keyDown(int keycode) {
        DeviceAction action = mapping.actionFor(keycode);
        if (rawListener != null) rawListener.rawKey(keycode, true, action);
        if (action == null) return false;
        if (isPressed(action)) return true; // ignora repetição automática do sistema

        long now = clock.getAsLong();
        downTime[action.ordinal()] = now;
        if (listener != null) listener.actionDown(action, now);
        return true;
    }

    @Override
    public boolean keyUp(int keycode) {
        DeviceAction action = mapping.actionFor(keycode);
        if (rawListener != null) rawListener.rawKey(keycode, false, action);
        if (action == null) return false;
        if (!isPressed(action)) return true;

        long now = clock.getAsLong();
        long held = now - downTime[action.ordinal()];
        downTime[action.ordinal()] = UP;
        if (listener != null) listener.actionUp(action, now, held);
        return true;
    }
}
