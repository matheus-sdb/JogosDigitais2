package io.github.triotiles.screens;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Array;
import io.github.triotiles.TrioTilesGame;
import io.github.triotiles.input.DeviceAction;
import io.github.triotiles.input.RawKeyListener;

/**
 * Tela para testar o aparelho quando ele chegar (docs/TESTE_APARELHO.md).
 * Mostra o que cada botão envia e avalia sozinha os testes de segurar tecla,
 * teclas simultâneas e knob.
 */
public class DeviceTestScreen extends BaseScreen implements RawKeyListener {
    private static final int ACTIONS = DeviceAction.values().length;
    private static final int LOG_SIZE = 9;
    private static final long LONG_HOLD_MS = 1000;   // segurou de verdade
    private static final long INSTANT_MS = 60;       // "apertou e soltou" na hora
    private static final int PRESSES_TO_JUDGE = 6;
    private static final long TRIPLE_CLICK_NS = 1_000_000_000L;

    private static final Color[] LANE_COLORS = {
        new Color(0.30f, 0.60f, 1.00f, 1f),
        new Color(0.35f, 0.85f, 0.45f, 1f),
        new Color(1.00f, 0.60f, 0.25f, 1f),
    };
    private static final Color KNOB_COLOR = new Color(0.80f, 0.50f, 0.95f, 1f);
    private static final Color OK = new Color(0.35f, 0.85f, 0.45f, 1f);
    private static final Color PROBLEM = new Color(1f, 0.35f, 0.35f, 1f);
    private static final Color PENDING = new Color(0.95f, 0.80f, 0.30f, 1f);

    private final int[] presses = new int[ACTIONS];
    private final long[] lastHoldMs = new long[ACTIONS];
    private final int[] lastKeycode = new int[ACTIONS];
    private final float[] flash = new float[ACTIONS];
    private final long[] knobClicks = new long[3];

    private long longestHoldMs;
    private int lanePresses;
    private int maxSimultaneous;
    private int unknownKeys;
    private String lastUnknown = "";
    private final Array<String> log = new Array<>();

    public DeviceTestScreen(TrioTilesGame game) {
        super(game);
    }

    @Override
    public void show() {
        super.show();
        game.device.setRawListener(this);
    }

    // ---------- eventos ----------

    @Override
    public void rawKey(int keycode, boolean down, DeviceAction action) {
        if (action != null) {
            lastKeycode[action.ordinal()] = keycode;
            return;
        }
        if (!down) return;
        if (keycode == Keys.ESCAPE) {
            backToMenu();
            return;
        }
        unknownKeys++;
        lastUnknown = keyName(keycode);
        addLog("+ " + lastUnknown + "  ->  tecla NÃO configurada");
    }

    @Override
    public void actionDown(DeviceAction action, long timeNanos) {
        int i = action.ordinal();
        presses[i]++;
        flash[i] = 0.2f;
        if (action.lane) {
            lanePresses++;
            maxSimultaneous = Math.max(maxSimultaneous, game.device.pressedLaneCount());
        }
        addLog("+ " + keyName(lastKeycode[i]) + "  ->  " + action.label);

        if (action == DeviceAction.KNOB_PRESS && isTripleClick(timeNanos)) {
            backToMenu();
        }
    }

    @Override
    public void actionUp(DeviceAction action, long timeNanos, long heldNanos) {
        long ms = heldNanos / 1_000_000L;
        int i = action.ordinal();
        lastHoldMs[i] = ms;
        if (action.lane) longestHoldMs = Math.max(longestHoldMs, ms);
        addLog("- " + keyName(lastKeycode[i]) + "  ->  " + action.label + "  (segurou " + ms + " ms)");
    }

    private boolean isTripleClick(long now) {
        knobClicks[0] = knobClicks[1];
        knobClicks[1] = knobClicks[2];
        knobClicks[2] = now;
        return knobClicks[0] != 0 && now - knobClicks[0] < TRIPLE_CLICK_NS;
    }

    private void backToMenu() {
        game.setScreen(new MenuScreen(game));
        dispose();
    }

    private void addLog(String line) {
        log.add(line);
        if (log.size > LOG_SIZE) log.removeIndex(0);
    }

    private static String keyName(int keycode) {
        String name = Keys.toString(keycode);
        return (name == null ? "?" : name) + " (" + keycode + ")";
    }

    // ---------- resultado dos testes ----------

    private Color holdStatus() {
        if (longestHoldMs >= LONG_HOLD_MS) return OK;
        if (lanePresses >= PRESSES_TO_JUDGE && longestHoldMs < INSTANT_MS) return PROBLEM;
        return PENDING;
    }

    private Color simultaneousStatus() {
        return maxSimultaneous >= 2 ? OK : PENDING;
    }

    private Color knobStatus() {
        boolean all = presses[DeviceAction.KNOB_LEFT.ordinal()] > 0
            && presses[DeviceAction.KNOB_RIGHT.ordinal()] > 0
            && presses[DeviceAction.KNOB_PRESS.ordinal()] > 0;
        return all ? OK : PENDING;
    }

    private Color unknownStatus() {
        return unknownKeys == 0 ? OK : PROBLEM;
    }

    private static String word(Color status) {
        if (status == OK) return "OK";
        if (status == PROBLEM) return "PROBLEMA";
        return "PENDENTE";
    }

    // ---------- desenho ----------

    @Override
    protected void update(float delta) {
        for (int i = 0; i < ACTIONS; i++) flash[i] = Math.max(0, flash[i] - delta);
    }

    @Override
    protected void draw() {
        DeviceAction[] actions = DeviceAction.values();
        float boxW = 190, boxH = 120, gap = 20, left = 40;
        float laneY = 480, knobY = 320;

        ShapeRenderer s = game.shapes;
        s.begin(ShapeRenderer.ShapeType.Filled);
        for (DeviceAction a : actions) {
            int i = a.ordinal();
            int col = a.lane ? i : i - 3;
            float x = left + col * (boxW + gap);
            float y = a.lane ? laneY : knobY;
            Color base = a.lane ? LANE_COLORS[i] : KNOB_COLOR;
            boolean lit = game.device.isPressed(a) || flash[i] > 0;
            float k = lit ? 1f : 0.3f;
            s.setColor(base.r * k, base.g * k, base.b * k, 1f);
            s.rect(x, y, boxW, boxH);
        }
        // painel do log
        s.setColor(0.12f, 0.13f, 0.18f, 1f);
        s.rect(left, 30, 3 * boxW + 2 * gap, 270);
        s.end();

        game.batch.begin();
        game.font.setColor(Color.WHITE);
        game.font.draw(game.batch, "Teste do Aparelho", left, 690);
        game.font.setColor(Color.LIGHT_GRAY);
        game.font.draw(game.batch, "Sair: clique o knob 3x rápido (ou ESC no teclado do PC)", left, 650);

        for (DeviceAction a : actions) {
            int i = a.ordinal();
            int col = a.lane ? i : i - 3;
            float x = left + col * (boxW + gap) + 12;
            float y = (a.lane ? laneY : knobY) + boxH - 12;
            game.font.setColor(Color.WHITE);
            game.font.draw(game.batch, a.label, x, y);
            game.font.draw(game.batch, game.device.getMapping().describeKeys(a), x, y - 30);
            game.font.draw(game.batch, presses[i] + "x", x, y - 60);
            if (a.lane && presses[i] > 0) game.font.draw(game.batch, lastHoldMs[i] + " ms", x + 80, y - 60);
        }

        game.font.setColor(Color.LIGHT_GRAY);
        float logY = 285;
        for (int i = 0; i < log.size; i++) {
            game.font.draw(game.batch, log.get(i), left + 12, logY - i * 28);
        }

        float rx = 720, ry = 600, line = 34;
        game.font.setColor(Color.WHITE);
        game.font.draw(game.batch, "Resultados", rx, ry);
        drawCheck(rx, ry - 50, "1. Segurar tecla", holdStatus(),
            "Segure uma tecla por ~2 s.  Maior tempo: " + longestHoldMs + " ms");
        drawCheck(rx, ry - 50 - 3 * line, "2. Teclas ao mesmo tempo", simultaneousStatus(),
            "Aperte 2 ou 3 juntas.  Máximo juntas: " + maxSimultaneous);
        drawCheck(rx, ry - 50 - 6 * line, "3. Knob", knobStatus(),
            "Gire para os 2 lados e clique.");
        drawCheck(rx, ry - 50 - 9 * line, "4. Teclas desconhecidas", unknownStatus(),
            unknownKeys == 0 ? "Nenhuma recebida." : unknownKeys + " recebida(s). Última: " + lastUnknown);
        game.batch.end();
    }

    private void drawCheck(float x, float y, String title, Color status, String detail) {
        game.font.setColor(status);
        game.font.draw(game.batch, "[" + word(status) + "]", x, y);
        game.font.setColor(Color.WHITE);
        game.font.draw(game.batch, title, x + 170, y);
        game.font.setColor(Color.LIGHT_GRAY);
        game.font.draw(game.batch, detail, x, y - 32);
    }
}
