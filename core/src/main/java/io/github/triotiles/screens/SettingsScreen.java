package io.github.triotiles.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import io.github.triotiles.TrioTilesGame;
import io.github.triotiles.game.Settings;
import io.github.triotiles.input.DeviceAction;

/**
 * Configurações do paciente, só pelo knob: girar escolhe a linha; clicar entra no ajuste
 * (girar muda o valor) e clicar de novo confirma e salva.
 */
public class SettingsScreen extends BaseScreen {
    private enum Row {
        PATIENT("Paciente"), HAND("Mão usada"), TEMPO("Velocidade"), WINDOW("Janela de acerto"),
        VOLUME("Volume"), LATENCY("Ajuste de atraso"), BACK("Voltar");

        final String label;

        Row(String label) {
            this.label = label;
        }
    }

    private static final Row[] ROWS = Row.values();
    private static final float ROW_X = 240;
    private static final float ROW_W = 800;
    private static final float ROW_H = 54;
    private static final float ROW_GAP = 10;
    private static final float TOP = 605;
    /** Quantas sessões recentes entram na sugestão de ajuste de atraso. */
    private static final int SUGGESTION_SESSIONS = 5;
    private static final Color EDITING = new Color(0.85f, 0.62f, 0.15f, 1f);

    private int selected;
    private boolean editing;

    public SettingsScreen(TrioTilesGame game) {
        this(game, 0);
    }

    SettingsScreen(TrioTilesGame game, int selectedRow) {
        super(game);
        selected = selectedRow;
    }

    @Override
    public void actionDown(DeviceAction action, long timeNanos) {
        if (action.lane) return;
        Row row = ROWS[selected];
        if (editing) {
            switch (action) {
                case KNOB_LEFT -> change(row, -1);
                case KNOB_RIGHT -> change(row, +1);
                case KNOB_PRESS -> {
                    editing = false;
                    game.saveSettings();
                    game.sfx.menuConfirm();
                }
                default -> { }
            }
            return;
        }
        switch (action) {
            case KNOB_LEFT -> {
                selected = (selected + ROWS.length - 1) % ROWS.length;
                game.sfx.menuMove();
            }
            case KNOB_RIGHT -> {
                selected = (selected + 1) % ROWS.length;
                game.sfx.menuMove();
            }
            case KNOB_PRESS -> {
                game.sfx.menuConfirm();
                switch (row) {
                    case PATIENT -> game.setScreen(new NameEditScreen(game));
                    case BACK -> game.setScreen(new MenuScreen(game));
                    default -> editing = true;
                }
            }
            default -> { }
        }
    }

    private void change(Row row, int dir) {
        Settings s = game.settings;
        switch (row) {
            case HAND -> s.setHand(step(Settings.Hand.values(), s.hand(), dir));
            case TEMPO -> s.setTempo(step(Settings.Tempo.values(), s.tempo(), dir));
            case WINDOW -> s.setWindow(step(Settings.Window.values(), s.window(), dir));
            case VOLUME -> {
                s.setVolumePercent(s.volumePercent() + dir * Settings.VOLUME_STEP);
                game.saveSettings(); // aplica na hora, para ouvir o volume novo
            }
            case LATENCY -> s.setLatencyMs(s.latencyMs() + dir * Settings.LATENCY_STEP_MS);
            default -> { }
        }
        game.sfx.menuMove();
    }

    /** O valor anterior/seguinte da lista, sem dar a volta (fica no primeiro/último). */
    private static <E> E step(E[] values, E current, int dir) {
        int i = 0;
        while (values[i] != current) i++;
        return values[Math.max(0, Math.min(values.length - 1, i + dir))];
    }

    private String value(Row row) {
        Settings s = game.settings;
        return switch (row) {
            case PATIENT -> s.patient();
            case HAND -> s.hand().label;
            case TEMPO -> s.tempo().label;
            case WINDOW -> s.window().label;
            case VOLUME -> s.volumePercent() + "%";
            case LATENCY -> signed(s.latencyMs()) + " ms";
            case BACK -> "";
        };
    }

    private String help(Row row) {
        Settings s = game.settings;
        return switch (row) {
            case PATIENT -> "Cada paciente tem seu progresso e seu histórico. Clique para trocar o nome.";
            case HAND -> "Direita: tecla 1 = indicador. Esquerda: tecla 1 = anelar (nome do dedo nos resultados).";
            case TEMPO -> "Lenta espaça as notas e faz elas caírem mais devagar. Rápida, o contrário.";
            case WINDOW -> "Larga aceita toques mais longe do momento certo. Estreita exige mais precisão.";
            case VOLUME -> "Volume dos sons do jogo.";
            case LATENCY -> latencyHelp(s);
            case BACK -> "Voltar ao menu principal.";
        };
    }

    private String latencyHelp(Settings s) {
        double raw = game.history.suggestedLatencyMs(s.patient(), SUGGESTION_SESSIONS);
        if (Double.isNaN(raw)) {
            return "Desconta este tempo de cada toque. Use se o resultado mostrar sempre \"atrasa\".";
        }
        int suggestion = Math.round((float) (raw / Settings.LATENCY_STEP_MS)) * Settings.LATENCY_STEP_MS;
        suggestion = Math.max(Settings.LATENCY_MIN_MS, Math.min(Settings.LATENCY_MAX_MS, suggestion));
        return "Desconta este tempo de cada toque. Sugestão pelas últimas sessões: " + signed(suggestion) + " ms";
    }

    private static String signed(int v) {
        return v > 0 ? "+" + v : String.valueOf(v);
    }

    private static float rowTop(int i) {
        return TOP - i * (ROW_H + ROW_GAP);
    }

    @Override
    protected void draw() {
        ShapeRenderer s = game.shapes;
        s.begin(ShapeRenderer.ShapeType.Filled);
        for (int i = 0; i < ROWS.length; i++) {
            Color c = i != selected ? PANEL_COLOR : editing ? EDITING : HIGHLIGHT_COLOR;
            s.setColor(c);
            s.rect(ROW_X, rowTop(i) - ROW_H, ROW_W, ROW_H);
        }
        s.end();

        game.batch.begin();
        float cx = TrioTilesGame.WORLD_WIDTH / 2f;
        drawCentered(game.fontBig, "Configurações", cx, 705, Color.WHITE);
        for (int i = 0; i < ROWS.length; i++) {
            Row row = ROWS[i];
            float y = rowTop(i) - ROW_H / 2f + 12;
            if (row == Row.BACK) {
                drawCentered(game.font, row.label, cx, y, Color.WHITE);
                continue;
            }
            text(row.label, ROW_X + 25, y, Color.WHITE);
            String v = value(row);
            if (i == selected && editing) v = "<  " + v + "  >";
            text(v, ROW_X + 470, y, i == selected ? Color.WHITE : PERFECT_COLOR);
        }
        drawCentered(game.font, help(ROWS[selected]), cx, 110, Color.LIGHT_GRAY);
        String how = editing ? "Gire o knob para mudar o valor, clique para confirmar"
            : "Gire o knob para escolher, clique para ajustar";
        drawCentered(game.font, how, cx, 55, editing ? EDITING : Color.LIGHT_GRAY);
        game.batch.end();
    }

    private void text(String s, float x, float y, Color color) {
        game.font.setColor(color);
        game.font.draw(game.batch, s, x, y);
    }
}
