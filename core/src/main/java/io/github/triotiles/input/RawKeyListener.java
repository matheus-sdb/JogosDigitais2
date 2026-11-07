package io.github.triotiles.input;

/** Recebe toda tecla, inclusive as que não pertencem ao aparelho. Usado na tela de teste. */
public interface RawKeyListener {
    /** @param action ação mapeada, ou null se a tecla não é do aparelho */
    void rawKey(int keycode, boolean down, DeviceAction action);
}
