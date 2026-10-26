package io.github.triotiles.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.TimeUtils;
import io.github.triotiles.TrioTilesGame;
import io.github.triotiles.game.Chart;
import io.github.triotiles.game.Note;
import io.github.triotiles.game.Playfield;
import io.github.triotiles.input.DeviceAction;

/**
 * Fase crua da Sprint 1: notas simples caem nas 3 trilhas e a tecla certa na hora certa acerta a nota.
 * Knob clique pausa; na pausa, girar o knob volta ao menu.
 */
public class GameScreen extends BaseScreen {
    // ---- regras (valores iniciais, ajustar nos testes) ----
    private static final int NOTE_COUNT = 40;
    private static final double FIRST_NOTE = 3.0;   // s até a primeira nota: tempo para se preparar
    private static final double INTERVAL = 0.85;    // s entre notas (~70 por minuto)
    private static final double HIT_WINDOW = 0.25;  // s antes ou depois do momento ideal
    private static final double END_DELAY = 1.0;    // s depois da última nota até mostrar o resultado

    // ---- desenho ----
    private static final float LANE_W = 170;
    private static final float LANE_GAP = 10;
    private static final float LANES_W = Playfield.LANES * LANE_W + (Playfield.LANES - 1) * LANE_GAP;
    private static final float LANES_X = (TrioTilesGame.WORLD_WIDTH - LANES_W) / 2f;
    private static final float PAD_Y = 25;
    private static final float PAD_H = 75;
    private static final float HIT_Y = 150;          // linha de acerto
    private static final float NOTE_H = 40;
    private static final float SPEED = 400;          // px por segundo
    private static final float FEEDBACK_TIME = 0.4f;

    private static final Color LANE_BG = new Color(0.11f, 0.12f, 0.17f, 1f);
    private static final Color MISSED_NOTE = new Color(0.30f, 0.30f, 0.35f, 1f);
    private static final Color GOOD = new Color(0.35f, 0.85f, 0.45f, 1f);
    private static final Color BAD = new Color(1f, 0.35f, 0.35f, 1f);
    private static final Color PANEL = new Color(0.13f, 0.14f, 0.20f, 1f);

    private final Playfield field;
    private long startNanos;
    private long pausedNanos;
    private long pauseStart = -1; // -1 = jogando
    private boolean finished;

    private final float[] feedbackTimer = new float[Playfield.LANES];
    private final String[] feedbackText = new String[Playfield.LANES];
    private final Color[] feedbackColor = new Color[Playfield.LANES];
    private final Color tmp = new Color();

    public GameScreen(TrioTilesGame game) {
        super(game);
        Chart chart = Chart.randomTaps(TimeUtils.millis(), NOTE_COUNT, FIRST_NOTE, INTERVAL);
        field = new Playfield(chart, HIT_WINDOW);
    }

    @Override
    public void show() {
        super.show();
        startNanos = TimeUtils.nanoTime();
    }

    // ---------- tempo ----------

    private boolean isPaused() {
        return pauseStart >= 0;
    }

    /** Tempo da fase em segundos no instante dado. Fica parado durante a pausa. */
    private double phaseTime(long nanos) {
        long t = isPaused() ? pauseStart : nanos;
        return (t - startNanos - pausedNanos) / 1e9;
    }

    private void setPaused(boolean pause) {
        long now = TimeUtils.nanoTime();
        if (pause && !isPaused()) {
            pauseStart = now;
        } else if (!pause && isPaused()) {
            pausedNanos += now - pauseStart;
            pauseStart = -1;
        }
    }

    /** Janela minimizada ou sem foco: pausa sozinho. */
    @Override
    public void pause() {
        if (!finished) setPaused(true);
    }

    // ---------- aparelho ----------

    @Override
    public void actionDown(DeviceAction action, long timeNanos) {
        if (action == DeviceAction.KNOB_PRESS) {
            if (finished) backToMenu();
            else setPaused(!isPaused());
            return;
        }
        if (!action.lane) { // girar o knob: só serve na pausa e no fim
            if (isPaused() || finished) backToMenu();
            return;
        }
        if (isPaused() || finished) return;

        int lane = action.laneIndex();
        long when = timeNanos > 0 ? timeNanos : TimeUtils.nanoTime();
        Note hit = field.press(lane, phaseTime(when));
        if (hit != null) showFeedback(lane, "Acertou!", GOOD);
        else showFeedback(lane, "Errou", BAD);
    }

    private void showFeedback(int lane, String text, Color color) {
        feedbackText[lane] = text;
        feedbackColor[lane] = color;
        feedbackTimer[lane] = FEEDBACK_TIME;
    }

    private void backToMenu() {
        game.setScreen(new MenuScreen(game));
    }

    // ---------- lógica por frame ----------

    @Override
    protected void update(float delta) {
        for (int i = 0; i < Playfield.LANES; i++) feedbackTimer[i] = Math.max(0, feedbackTimer[i] - delta);
        if (isPaused() || finished) return;

        double t = phaseTime(TimeUtils.nanoTime());
        field.update(t, n -> showFeedback(n.lane, "Perdeu", BAD));
        if (field.isFinished() && t > field.chart().endTime() + END_DELAY) finished = true;
    }

    // ---------- desenho ----------

    private static float laneX(int lane) {
        return LANES_X + lane * (LANE_W + LANE_GAP);
    }

    @Override
    protected void draw() {
        double t = phaseTime(TimeUtils.nanoTime());
        ShapeRenderer s = game.shapes;
        s.begin(ShapeRenderer.ShapeType.Filled);

        for (int lane = 0; lane < Playfield.LANES; lane++) {
            float x = laneX(lane);
            s.setColor(LANE_BG);
            s.rect(x, 0, LANE_W, TrioTilesGame.WORLD_HEIGHT);

            Color c = LANE_COLORS[lane];
            float k = game.device.isPressed(DeviceAction.forLane(lane)) ? 1f : 0.35f;
            s.setColor(c.r * k, c.g * k, c.b * k, 1f);
            s.rect(x, PAD_Y, LANE_W, PAD_H);
        }

        for (Note n : field.chart().notes()) {
            if (n.state() == Note.State.HIT) continue;
            float y = (float) (HIT_Y + (n.time - t) * SPEED) - NOTE_H / 2f;
            if (y > TrioTilesGame.WORLD_HEIGHT) break; // as próximas estão ainda mais acima
            if (y + NOTE_H < 0) continue;
            s.setColor(n.state() == Note.State.MISSED ? MISSED_NOTE : LANE_COLORS[n.lane]);
            s.rect(laneX(n.lane) + 8, y, LANE_W - 16, NOTE_H);
        }

        s.setColor(Color.WHITE);
        s.rect(LANES_X - 12, HIT_Y - 2, LANES_W + 24, 4);

        if (isPaused() || finished) {
            s.setColor(PANEL);
            s.rect(TrioTilesGame.WORLD_WIDTH / 2f - 330, 220, 660, 300);
        }
        s.end();

        game.batch.begin();
        drawHud(t);
        drawFeedback();
        if (finished) drawResult();
        else if (isPaused()) drawPause();
        game.batch.end();
    }

    private void drawHud(double t) {
        float x = 40;
        game.font.setColor(Color.WHITE);
        game.font.draw(game.batch, "Fase 1 (provisória)", x, 690);
        game.font.setColor(GOOD);
        game.font.draw(game.batch, "Acertos: " + field.hits(), x, 620);
        game.font.setColor(BAD);
        game.font.draw(game.batch, "Perdidas: " + field.misses(), x, 580);
        game.font.draw(game.batch, "Tecla errada: " + field.strays(), x, 540);
        game.font.setColor(Color.LIGHT_GRAY);
        int done = field.hits() + field.misses();
        game.font.draw(game.batch, "Notas: " + done + " / " + field.chart().size(), x, 480);

        float rx = LANES_X + LANES_W + 40;
        game.font.draw(game.batch, "Clique o knob:", rx, 620);
        game.font.draw(game.batch, "pausar", rx, 585);

        for (int lane = 0; lane < Playfield.LANES; lane++) {
            drawCentered(game.font, String.valueOf(lane + 1), laneX(lane) + LANE_W / 2f, PAD_Y + PAD_H / 2f + 12, Color.WHITE);
        }

        if (!isPaused() && t < FIRST_NOTE - 1.0) {
            drawCentered(game.font, "Prepare um dedo em cada tecla...", TrioTilesGame.WORLD_WIDTH / 2f, 420, Color.WHITE);
        }
    }

    private void drawFeedback() {
        for (int lane = 0; lane < Playfield.LANES; lane++) {
            if (feedbackTimer[lane] <= 0) continue;
            Color c = feedbackColor[lane];
            tmp.set(c.r, c.g, c.b, Math.min(1f, feedbackTimer[lane] / FEEDBACK_TIME * 1.5f));
            drawCentered(game.font, feedbackText[lane], laneX(lane) + LANE_W / 2f, HIT_Y + 60, tmp);
        }
    }

    private void drawPause() {
        float cx = TrioTilesGame.WORLD_WIDTH / 2f;
        drawCentered(game.fontBig, "Pausado", cx, 470, Color.WHITE);
        drawCentered(game.font, "Clique o knob: continuar", cx, 350, Color.WHITE);
        drawCentered(game.font, "Gire o knob: voltar ao menu", cx, 300, Color.LIGHT_GRAY);
    }

    private void drawResult() {
        float cx = TrioTilesGame.WORLD_WIDTH / 2f;
        int percent = (int) Math.round(field.accuracy() * 100);
        drawCentered(game.fontBig, "Fim da fase!", cx, 480, Color.WHITE);
        drawCentered(game.font, "Acertos: " + field.hits() + " de " + field.chart().size() + "  (" + percent + "%)",
                cx, 380, GOOD);
        drawCentered(game.font, "Perdidas: " + field.misses() + "   Tecla errada: " + field.strays(), cx, 335, BAD);
        drawCentered(game.font, "Clique o knob para voltar ao menu", cx, 260, Color.LIGHT_GRAY);
    }
}
