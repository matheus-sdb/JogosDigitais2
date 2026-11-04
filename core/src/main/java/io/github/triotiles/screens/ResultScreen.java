package io.github.triotiles.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import io.github.triotiles.TrioTilesGame;
import io.github.triotiles.game.Judgement;
import io.github.triotiles.game.LevelData;
import io.github.triotiles.game.Pipo;
import io.github.triotiles.game.Playfield;
import io.github.triotiles.game.SessionStats;
import io.github.triotiles.input.DeviceAction;

import java.util.ArrayList;
import java.util.List;

/**
 * Fim da fase: meta, pontuação e as métricas de reabilitação (velocidade, consistência,
 * precisão por dedo, sustentação e giros). Girar o knob escolhe, clicar confirma.
 */
public class ResultScreen extends BaseScreen {
    /** Segundos ignorando o aparelho, para um toque do fim da fase não escolher nada sem querer. */
    private static final float INPUT_DELAY = 0.6f;
    /** Diferença de precisão (pontos percentuais) para apontar um dedo como mais fraco. */
    private static final double WEAK_MARGIN = 0.10;
    /** Abaixo disso (ms), a tendência conta como "no tempo". */
    private static final double TENDENCY_MS = 15;
    /** A partir desta precisão o Pipo comemora dançando. */
    private static final double CELEBRATE_ACCURACY = 0.70;

    private static final float LEFT_X = 70;
    private static final float RIGHT_X = 700;
    private static final float BAR_X = RIGHT_X + 120;
    private static final float BAR_W = 300;
    private static final float BAR_H = 28;
    private static final float[] LANE_ROW_Y = {520, 430, 340};
    private static final float OPTION_Y = 92;
    private static final float OPTION_W = 290;
    private static final float OPTION_H = 58;
    private static final float OPTION_GAP = 20;
    private static final Color BAR_BG = new Color(0.16f, 0.17f, 0.23f, 1f);

    private enum Option {
        NEXT("Próxima fase"), AGAIN("Jogar de novo"), CHOOSE("Escolher fase");

        final String label;

        Option(String label) {
            this.label = label;
        }
    }

    private final LevelData level;
    private final SessionStats stats;
    private final LevelData unlocked;
    private final List<Option> options = new ArrayList<>();
    private final Pipo pipo = new Pipo();
    private int selected;
    private float elapsed;

    /** @param unlocked fase liberada agora por este resultado, ou null */
    public ResultScreen(TrioTilesGame game, LevelData level, SessionStats stats, LevelData unlocked) {
        super(game);
        this.level = level;
        this.stats = stats;
        this.unlocked = unlocked;
        LevelData next = level.next();
        if (next != null && game.progress.isUnlocked(next)) options.add(Option.NEXT);
        options.add(Option.AGAIN);
        options.add(Option.CHOOSE);
        if (stats.accuracy >= CELEBRATE_ACCURACY) pipo.celebrate();
    }

    @Override
    public void actionDown(DeviceAction action, long timeNanos) {
        if (elapsed < INPUT_DELAY) return;
        switch (action) {
            case KNOB_LEFT -> {
                selected = (selected + options.size() - 1) % options.size();
                game.sfx.menuMove();
            }
            case KNOB_RIGHT -> {
                selected = (selected + 1) % options.size();
                game.sfx.menuMove();
            }
            case KNOB_PRESS -> {
                game.sfx.menuConfirm();
                switch (options.get(selected)) {
                    case NEXT -> game.setScreen(new GameScreen(game, level.next()));
                    case AGAIN -> game.setScreen(new GameScreen(game, level));
                    case CHOOSE -> game.setScreen(new LevelSelectScreen(game, level));
                }
            }
            default -> { }
        }
    }

    @Override
    protected void update(float delta) {
        elapsed += delta;
        pipo.update(delta);
    }

    private float optionX(int i) {
        float total = options.size() * OPTION_W + (options.size() - 1) * OPTION_GAP;
        return (TrioTilesGame.WORLD_WIDTH - total) / 2f + 40 + i * (OPTION_W + OPTION_GAP);
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
        for (int i = 0; i < options.size(); i++) {
            s.setColor(i == selected ? HIGHLIGHT_COLOR : PANEL_COLOR);
            s.rect(optionX(i), OPTION_Y - OPTION_H / 2f, OPTION_W, OPTION_H);
        }
        PipoRenderer.draw(s, pipo, 85, 32, 0.7f, LANE_COLORS); // pequeno, para as notinhas não cobrirem o texto
        s.end();

        game.batch.begin();
        float cx = TrioTilesGame.WORLD_WIDTH / 2f;
        drawCentered(game.fontBig, level.fullTitle(), cx, 705, Color.WHITE);
        drawGoal(cx);
        drawSummary();
        drawFingers();
        for (int i = 0; i < options.size(); i++) {
            drawCentered(game.font, options.get(i).label, optionX(i) + OPTION_W / 2f, OPTION_Y + 12, Color.WHITE);
        }
        drawCentered(game.font, "Gire o knob para escolher, clique para confirmar", cx, 38, Color.LIGHT_GRAY);
        game.batch.end();
    }

    private void drawGoal(float cx) {
        String done = "você fez " + percent(stats.accuracy);
        if (!level.hasGoal()) {
            drawCentered(game.font, "Última fase concluída!  ·  " + done, cx, 632, PERFECT_COLOR);
        } else if (unlocked != null) {
            drawCentered(game.font, "Meta " + percent(level.goalAccuracy) + "  ·  " + done + "  ·  "
                + unlocked.fullTitle() + " liberada!", cx, 632, GOOD_COLOR);
        } else if (level.goalReached(stats.accuracy)) {
            drawCentered(game.font, "Meta " + percent(level.goalAccuracy) + " atingida  ·  " + done, cx, 632, GOOD_COLOR);
        } else {
            drawCentered(game.font, "Meta " + percent(level.goalAccuracy) + "  ·  " + done
                + "  ·  continue praticando!", cx, 632, PERFECT_COLOR);
        }
    }

    private void drawSummary() {
        float x = LEFT_X;
        text("Pontos: " + stats.points, x, 580, Color.WHITE);
        text("Maior combo: " + stats.maxCombo, x + 300, 580, Color.WHITE);
        text("Precisão: " + percent(stats.accuracy) + "  (" + stats.hits + " de " + stats.totalNotes + ")",
            x, 535, GOOD_COLOR);

        float jx = x;
        for (Judgement j : Judgement.values()) {
            String label = j.label + " " + stats.count(j);
            text(label, jx, 492, colorOf(j));
            jx += 30 + label.length() * 14;
        }

        text("Velocidade: " + ms(stats.meanAbsOffsetMs), x, 432, Color.WHITE);
        text("distância média do momento certo", x, 401, Color.LIGHT_GRAY);
        text("Consistência: " + ms(stats.stdDevOffsetMs), x, 350, Color.WHITE);
        text("variação entre os toques (menor = melhor)", x, 319, Color.LIGHT_GRAY);
        text("Tendência: " + tendency(), x, 268, Color.WHITE);
        text("Teclas erradas: " + stats.strays, x, 225, stats.strays == 0 ? GOOD_COLOR : BAD_COLOR);
    }

    private void drawFingers() {
        text("Precisão por dedo", RIGHT_X, 580, Color.WHITE);
        for (int lane = 0; lane < Playfield.LANES; lane++) {
            float y = LANE_ROW_Y[lane];
            text("Tecla " + (lane + 1), RIGHT_X, y, LANE_COLORS[lane]);
            text(percent(stats.laneAccuracy(lane)), BAR_X + BAR_W + 15, y, Color.WHITE);
            int strays = stats.laneStrays(lane);
            String detail = stats.laneHits(lane) + " de " + stats.laneNotes(lane)
                + "  ·  " + game.settings.hand().finger(lane)
                + (strays == 0 ? "" : strays == 1 ? "  ·  1 errada" : "  ·  " + strays + " erradas");
            text(detail, BAR_X, y - 36, Color.LIGHT_GRAY);
        }
        int weak = stats.weakestLane(WEAK_MARGIN);
        if (weak >= 0) {
            text("Dedo para treinar mais: " + game.settings.hand().finger(weak) + " (tecla " + (weak + 1) + ")",
                RIGHT_X, 262, PERFECT_COLOR);
        }
        else if (stats.hits > 0) text("Os três dedos estão equilibrados", RIGHT_X, 262, GOOD_COLOR);

        float y = 222;
        if (stats.holdNotes > 0) {
            text("Notas longas: " + stats.holdsCompleted + " de " + stats.holdNotes
                + "  ·  sustentação " + percent(stats.sustain), RIGHT_X, y, Color.WHITE);
            y -= 38;
        }
        if (stats.spinNotes > 0) {
            text("Giros do knob: " + stats.spinHits + " de " + stats.spinNotes, RIGHT_X, y, Color.WHITE);
        }
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
