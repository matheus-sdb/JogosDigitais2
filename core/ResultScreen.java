package io.github.triotiles.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import io.github.triotiles.TrioTilesGame;
import io.github.triotiles.game.Judgement;
import io.github.triotiles.game.Playfield;
import io.github.triotiles.game.SessionStats;
import io.github.triotiles.input.DeviceAction;

/**
 * Fim da fase: pontuação e as métricas de reabilitação (velocidade, consistência,
 * precisão por dedo). Girar o knob escolhe, clicar confirma.
 */
public class ResultScreen extends BaseScreen {
    private static final String[] OPTIONS = {"Jogar de novo", "Menu principal"};
    /** Segundos ignorando o aparelho, para um toque do fim da fase não escolher nada sem querer. */
    private static final float INPUT_DELAY = 0.6f;
    /** Diferença de precisão (pontos percentuais) para apontar um dedo como mais fraco. */
    private static final double WEAK_MARGIN = 0.10;
    /** Abaixo disso (ms), a tendência conta como "no tempo". */
    private static final double TENDENCY_MS = 15;

    private static final float LEFT_X = 70;
    private static final float RIGHT_X = 700;
    private static final float BAR_X = RIGHT_X + 120;
    private static final float BAR_W = 300;
    private static final float BAR_H = 28;
    private static final float[] LANE_ROW_Y = {520, 430, 340};
    private static final float OPTION_Y = 95;
    private static final float OPTION_W = 320;
    private static final float OPTION_H = 60;
    private static final Color BAR_BG = new Color(0.16f, 0.17f, 0.23f, 1f);

    private final SessionStats stats;
    private int selected;
    private float elapsed;

    public ResultScreen(TrioTilesGame game, SessionStats stats) {
        super(game);
        this.stats = stats;
    }

    @Override
    public void actionDown(DeviceAction action, long timeNanos) {
        if (elapsed < INPUT_DELAY) return;
        switch (action) {
            case KNOB_LEFT -> selected = (selected + OPTIONS.length - 1) % OPTIONS.length;
            case KNOB_RIGHT -> selected = (selected + 1) % OPTIONS.length;
            case KNOB_PRESS -> {
                if (selected == 0) game.setScreen(new GameScreen(game));
                else game.setScreen(new MenuScreen(game));
            }
            default -> { }
        }
    }

    @Override
    protected void update(float delta) {
        elapsed += delta;
    }

    private static float optionX(int i) {
        return TrioTilesGame.WORLD_WIDTH / 2f + (i == 0 ? -OPTION_W - 20 : 20);
    }

    @Override
    protected void draw() {
        ShapeRenderer s = game.shapes;
        s.begin(ShapeRenderer.ShapeType.Filled);
        for (int lane = 0; lane < Playfield.LANES; lane++) {
            float y = LANE_ROW_Y[lane];
            s.setColor(BAR_BG);
            s.rect(BAR_X, y - BAR_H + 4, BAR_W, BAR_H);
            double a = stats.laneAccuracy(lane);
            if (!Double.isNaN(a) && a > 0) {
                s.setColor(LANE_COLORS[lane]);
                s.rect(BAR_X, y - BAR_H + 4, (float) (BAR_W * a), BAR_H);
            }
        }
        for (int i = 0; i < OPTIONS.length; i++) {
            s.setColor(i == selected ? HIGHLIGHT_COLOR : PANEL_COLOR);
            s.rect(optionX(i), OPTION_Y - OPTION_H / 2f, OPTION_W, OPTION_H);
        }
        s.end();

        game.batch.begin();
        float cx = TrioTilesGame.WORLD_WIDTH / 2f;
        drawCentered(game.fontBig, "Fim da fase!", cx, 695, Color.WHITE);
        drawSummary();
        drawFingers();
        for (int i = 0; i < OPTIONS.length; i++) {
            drawCentered(game.font, OPTIONS[i], optionX(i) + OPTION_W / 2f, OPTION_Y + 12, Color.WHITE);
        }
        drawCentered(game.font, "Gire o knob para escolher, clique para confirmar", cx, 38, Color.LIGHT_GRAY);
        game.batch.end();
    }

    private void drawSummary() {
        float x = LEFT_X;
        text("Pontos: " + stats.points, x, 590, Color.WHITE);
        text("Maior combo: " + stats.maxCombo, x + 300, 590, Color.WHITE);
        text("Precisão: " + percent(stats.accuracy) + "  (" + stats.hits + " de " + stats.totalNotes + ")",
            x, 540, GOOD_COLOR);

        float jx = x;
        for (Judgement j : Judgement.values()) {
            String label = j.label + " " + stats.count(j);
            text(label, jx, 495, colorOf(j));
            jx += 30 + label.length() * 14;
        }

        text("Velocidade: " + ms(stats.meanAbsOffsetMs), x, 430, Color.WHITE);
        text("distância média do momento certo", x, 398, Color.LIGHT_GRAY);
        text("Consistência: " + ms(stats.stdDevOffsetMs), x, 345, Color.WHITE);
        text("variação entre os toques (menor = melhor)", x, 313, Color.LIGHT_GRAY);
        text("Tendência: " + tendency(), x, 260, Color.WHITE);
        text("Teclas erradas: " + stats.strays, x, 210, stats.strays == 0 ? GOOD_COLOR : BAD_COLOR);
    }

    private void drawFingers() {
        text("Precisão por dedo", RIGHT_X, 590, Color.WHITE);
        for (int lane = 0; lane < Playfield.LANES; lane++) {
            float y = LANE_ROW_Y[lane];
            text("Tecla " + (lane + 1), RIGHT_X, y, LANE_COLORS[lane]);
            text(percent(stats.laneAccuracy(lane)), BAR_X + BAR_W + 15, y, Color.WHITE);
            int strays = stats.laneStrays(lane);
            String detail = stats.laneHits(lane) + " de " + stats.laneNotes(lane)
                + (strays == 0 ? "" : strays == 1 ? "  ·  1 tecla errada" : "  ·  " + strays + " teclas erradas");
            text(detail, BAR_X, y - 36, Color.LIGHT_GRAY);
        }
        int weak = stats.weakestLane(WEAK_MARGIN);
        if (weak >= 0) text("Dedo para treinar mais: Tecla " + (weak + 1), RIGHT_X, 225, PERFECT_COLOR);
        else if (stats.hits > 0) text("Os três dedos estão equilibrados", RIGHT_X, 225, GOOD_COLOR);
    }

    private String tendency() {
        double m = stats.meanOffsetMs;
        if (Double.isNaN(m)) return "-";
        if (Math.abs(m) < TENDENCY_MS) return "no tempo";
        return (m < 0 ? "adianta " : "atrasa ") + Math.round(Math.abs(m)) + " ms";
    }

    private void text(String s, float x, float y, Color color) {
        game.font.setColor(color);
        game.font.draw(game.batch, s, x, y);
    }

    private static String ms(double value) {
        return Double.isNaN(value) ? "-" : Math.round(value) + " ms";
    }

    private static String percent(double value) {
        return Double.isNaN(value) ? "-" : Math.round(value * 100) + "%";
    }
}
