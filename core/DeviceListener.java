package io.github.triotiles.input;

/** Recebe as ações do aparelho. Os tempos estão em nanossegundos (System.nanoTime). */
public interface DeviceListener {
    void actionDown(DeviceAction action, long timeNanos);

    /** @param heldNanos quanto tempo a tecla ficou pressionada */
    void actionUp(DeviceAction action, long timeNanos, long heldNanos);
}
