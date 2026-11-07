package io.github.triotiles.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import io.github.triotiles.TrioTilesGame;
import io.github.triotiles.game.LevelData;
import io.github.triotiles.game.Progress;
import io.github.triotiles.input.DeviceAction;

/**
 * Escolha da fase pelo knob. A fase seguinte libera ao atingir a meta da anterior.
 * Mostra o objetivo terapêutico de cada fase e a melhor precisão já feita.
 */
public class LevelSelectScreen extends BaseScreen {
    private static final LevelData[] LEVELS = LevelData.values();
    private static final float CARD_X = 190;
    private static final float CARD_W = 900;
    private static final float CARD_H = 110;
    private static final float CARD_GAP = 18;
    private static final float TOP = 590;
    private static final float BACK_H = 60;
    private static final Color LOCKED_TEXT = new Color(0.55f, 0.55f, 0.60f, 1f);
    private static final float MESSAGE_TIME = 2.2f;

    /** As 3 fases + "Voltar". */
    private final OptionList options = new OptionList("1", "2", "3", "Voltar");
    private String message;
    private float messageTimer;

    public LevelSelectScreen(TrioTilesGame game) {
        this(game, null);
    }

    /** @param focus fase que começa selecionada (null = a mais avançada liberada) */
    public LevelSelectScreen(TrioTilesGame game, LevelData focus) {
        super(game);
        options.select(focus != null ? focus.ordinal() : game.progress.unlockedCount() - 1);
    }

    @Override
    public void actionDown(DeviceAction action, long timeNanos) {
        int chosen = options.handle(action, game.sfx);
        if (chosen < 0) return;
        if (chosen == LEVELS.length) {
            game.setScreen(new MenuScreen(game));
            return;
        }
        LevelData level = LEVELS[chosen];
        if (game.progress.isUnlocked(level)) {
            game.setScreen(new GameScreen(game, level));
        } else {
            game.sfx.miss();
            LevelData before = LEVELS[chosen - 1];
            message = "Faça " + percent(before.goalAccuracy) + " na " + before.fullTitle() + " para liberar";
            messageTimer = MESSAGE_TIME;
        }
    }

    @Override
    protected void update(float delta) {
        messageTimer = Math.max(0, messageTimer - delta);
    }

    private static float cardTop(int i) {
        return TOP - i * (CARD_H + CARD_GAP);
    }

    @Override
    protected void draw() {
        Progress progress = game.progress;
        ShapeRenderer s = game.shapes;
        s.begin(ShapeRenderer.ShapeType.Filled);
        for (int i = 0; i <= LEVELS.length; i++) {
            float h = i < LEVELS.length ? CARD_H : BACK_H;
            s.setColor(i == options.selected() ? HIGHLIGHT_COLOR : PANEL_COLOR);
            s.rect(CARD_X, cardTop(i) - h, CARD_W, h);
            if (i < LEVELS.length) { // faixa com as cores das trilhas, marcando a fase
                boolean open = progress.isUnlocked(LEVELS[i]);
                for (int lane = 0; lane < 3; lane++) {
                    Color c = LANE_COLORS[lane];
                    float k = open ? 1f : 0.3f;
                    s.setColor(c.r * k, c.g * k, c.b * k, 1f);
                    s.rect(CARD_X + 18 + lane * 22, cardTop(i) - h + 20, 16, h - 40);
                }
            }
        }
        s.end();

        game.batch.begin();
        float cx = TrioTilesGame.WORLD_WIDTH / 2f;
        drawCentered(game.fontBig, "Escolha a fase", cx, 700, Color.WHITE);
        for (int i = 0; i < LEVELS.length; i++) {
            LevelData level = LEVELS[i];
            boolean open = progress.isUnlocked(level);
            float x = CARD_X + 110, top = cardTop(i);
            text(level.fullTitle() + "  ·  " + level.stage.label, x, top - 22, open ? Color.WHITE : LOCKED_TEXT);
            text(level.purpose, x, top - 62, open ? Color.LIGHT_GRAY : LOCKED_TEXT);
            String right;
            if (!open) {
                right = "Bloqueada";
            } else if (Double.isNaN(progress.best(level))) {
                right = level.hasGoal() ? "Meta: " + percent(level.goalAccuracy) : "Última fase";
            } else {
                right = "Melhor: " + percent(progress.best(level));
            }
            text(right, CARD_X + CARD_W - 230, top - 42, open ? PERFECT_COLOR : LOCKED_TEXT);
        }
        drawCentered(game.font, "Voltar", cx, cardTop(LEVELS.length) - BACK_H / 2f + 12, Color.WHITE);

        if (messageTimer > 0) drawCentered(game.font, message, cx, 70, BAD_COLOR);
        else drawCentered(game.font, "Gire o knob para escolher, clique para confirmar", cx, 70, Color.LIGHT_GRAY);
        game.batch.end();
    }

    private void text(String s, float x, float y, Color color) {
        game.font.setColor(color);
        game.font.draw(game.batch, s, x, y);
    }

    private static String percent(double value) {
        return Math.round(value * 100) + "%";
    }
}
