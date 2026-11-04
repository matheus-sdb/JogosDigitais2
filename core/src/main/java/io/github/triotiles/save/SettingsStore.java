package io.github.triotiles.save;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import io.github.triotiles.game.Settings;

/** Salva as configurações nas Preferences do libGDX (no Windows: %USERPROFILE%\.prefs\TrioTiles-config). */
public class SettingsStore {
    private static final String FILE = "TrioTiles-config";

    private Preferences prefs() {
        return Gdx.app.getPreferences(FILE);
    }

    public Settings load() {
        Settings s = new Settings();
        try {
            Preferences p = prefs();
            s.setPatient(p.getString("paciente", Settings.DEFAULT_PATIENT));
            s.setHand(parse(Settings.Hand.class, p.getString("mao", ""), Settings.Hand.RIGHT));
            s.setTempo(parse(Settings.Tempo.class, p.getString("velocidade", ""), Settings.Tempo.NORMAL));
            s.setWindow(parse(Settings.Window.class, p.getString("janela", ""), Settings.Window.NORMAL));
            s.setVolumePercent(p.getInteger("volume", 80));
            s.setLatencyMs(p.getInteger("atrasoMs", 0));
        } catch (RuntimeException e) {
            Gdx.app.error("SettingsStore", "configurações não carregaram; usando o padrão", e);
        }
        return s;
    }

    public void save(Settings s) {
        try {
            Preferences p = prefs();
            p.putString("paciente", s.patient());
            p.putString("mao", s.hand().name());
            p.putString("velocidade", s.tempo().name());
            p.putString("janela", s.window().name());
            p.putInteger("volume", s.volumePercent());
            p.putInteger("atrasoMs", s.latencyMs());
            p.flush();
        } catch (RuntimeException e) {
            Gdx.app.error("SettingsStore", "configurações não foram salvas", e);
        }
    }

    private static <E extends Enum<E>> E parse(Class<E> type, String value, E fallback) {
        try {
            return Enum.valueOf(type, value);
        } catch (IllegalArgumentException e) {
            return fallback;
        }
    }
}
