package io.github.triotiles.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import io.github.triotiles.TrioTilesGame;
import io.github.triotiles.game.Pipo;
import io.github.triotiles.input.DeviceAction;

/** Menu principal. Navegação só pelo knob. O Pipo fica tocando ao lado. */
public class MenuScreen extends BaseScreen {
    private static final float OPTIONS_X = 500;
    private static final float OPTIONS_TOP = 470;
    private static final float OPTION_W = 460;
    private static final float OPTION_H = 70;
    private static final float OPTION_GAP = 22;
    /** A cada quantos segundos o Pipo "toca" uma nota no menu. */
    private static final float PIPO_BEAT = 0.9f;

    private final OptionList options = new OptionList("Jogar", "Teste do Aparelho", "Sair");
    private final Pipo pipo = new Pipo();
    private float beatTimer;
    private int beatLane;

    public MenuScreen(TrioTilesGame game) {
        super(game);
    }

    @Override
    public void actionDown(DeviceAction action, long timeNanos) {
        switch (options.handle(action, game.sfx)) {
            case 0 -> game.setScreen(new LevelSelectScreen(game));
            case 1 -> game.setScreen(new DeviceTestScreen(game));
            case 2 -> Gdx.app.exit();
            default -> { }
        }
    }

    @Override
    protected void update(float delta) {
        pipo.update(delta);
        beatTimer += delta;
        if (beatTimer >= PIPO_BEAT) {
            beatTimer -= PIPO_BEAT;
            pipo.onHit(beatLane, 1);
            beatLane = (beatLane + 1) % 3;
        }
    }

    @Override
    protected void draw() {
        float cx = TrioTilesGame.WORLD_WIDTH / 2f;
        ShapeRenderer s = game.shapes;
        s.begin(ShapeRenderer.ShapeType.Filled);
        options.drawBoxes(s, OPTIONS_X, OPTIONS_TOP, OPTION_W, OPTION_H, OPTION_GAP);
        PipoRenderer.draw(s, pipo, 960, 200, 1.9f, LANE_COLORS);
        s.end();

        game.batch.begin();
        drawCentered(game.fontBig, "TrioTiles", cx, 650, Color.WHITE);
        drawCentered(game.font, "Reabilitação da mão com música", cx, 565, Color.LIGHT_GRAY);
        options.drawLabels(game.batch, game.font, OPTIONS_X, OPTIONS_TOP, OPTION_H, OPTION_GAP);
        drawCentered(game.font, "Gire o knob para escolher, clique para confirmar  (no PC: setas e Enter)",
                cx, 60, Color.LIGHT_GRAY);
        game.batch.end();
    }
}
