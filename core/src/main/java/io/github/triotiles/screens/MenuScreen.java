package io.github.triotiles.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import io.github.triotiles.TrioTilesGame;
import io.github.triotiles.input.DeviceAction;

/** Menu principal provisório (o menu completo é da Sprint 3). Navegação só pelo knob. */
public class MenuScreen extends BaseScreen {
    private static final String[] ITEMS = {"Jogar", "Teste do Aparelho", "Sair"};
    private static final boolean[] ENABLED = {true, true, true};
    private static final Color HIGHLIGHT = new Color(0.25f, 0.55f, 0.95f, 1f);

    private int selected = 0;

    public MenuScreen(TrioTilesGame game) {
        super(game);
    }

    @Override
    public void actionDown(DeviceAction action, long timeNanos) {
        switch (action) {
            case KNOB_LEFT -> selected = (selected + ITEMS.length - 1) % ITEMS.length;
            case KNOB_RIGHT -> selected = (selected + 1) % ITEMS.length;
            case KNOB_PRESS -> choose();
            default -> { }
        }
    }

    private void choose() {
        if (!ENABLED[selected]) return;
        switch (selected) {
            case 0 -> game.setScreen(new GameScreen(game));
            case 1 -> game.setScreen(new DeviceTestScreen(game));
            case 2 -> Gdx.app.exit();
            default -> { }
        }
    }

    @Override
    protected void draw() {
        float cx = TrioTilesGame.WORLD_WIDTH / 2f;
        float itemW = 520, itemH = 70, firstY = 400, gap = 95;

        game.shapes.begin(ShapeRenderer.ShapeType.Filled);
        game.shapes.setColor(HIGHLIGHT);
        game.shapes.rect(cx - itemW / 2f, firstY - selected * gap - itemH / 2f, itemW, itemH);
        game.shapes.end();

        game.batch.begin();
        drawCentered(game.fontBig, "TrioTiles", cx, 600, Color.WHITE);
        for (int i = 0; i < ITEMS.length; i++) {
            Color c = ENABLED[i] ? Color.WHITE : Color.GRAY;
            drawCentered(game.font, ITEMS[i], cx, firstY - i * gap + 12, c);
        }
        drawCentered(game.font, "Gire o knob para escolher, clique para confirmar  (no PC: setas e Enter)",
                cx, 60, Color.LIGHT_GRAY);
        game.batch.end();
    }
}
