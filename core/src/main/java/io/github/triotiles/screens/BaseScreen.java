package io.github.triotiles.screens;

import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import io.github.triotiles.TrioTilesGame;
import io.github.triotiles.input.DeviceAction;
import io.github.triotiles.input.DeviceListener;

/** Base das telas: viewport 1280x720, fundo e recebimento das ações do aparelho. */
public abstract class BaseScreen extends ScreenAdapter implements DeviceListener {
    protected static final Color BACKGROUND = new Color(0.07f, 0.08f, 0.12f, 1f);

    protected final TrioTilesGame game;
    protected final Viewport viewport;
    private final GlyphLayout layout = new GlyphLayout();

    protected BaseScreen(TrioTilesGame game) {
        this.game = game;
        this.viewport = new FitViewport(TrioTilesGame.WORLD_WIDTH, TrioTilesGame.WORLD_HEIGHT);
    }

    @Override
    public void show() {
        game.device.releaseAll();
        game.device.setListener(this);
    }

    @Override
    public void hide() {
        game.device.setListener(null);
        game.device.setRawListener(null);
    }

    @Override
    public final void render(float delta) {
        update(delta);
        ScreenUtils.clear(BACKGROUND);
        viewport.apply();
        game.shapes.setProjectionMatrix(viewport.getCamera().combined);
        game.batch.setProjectionMatrix(viewport.getCamera().combined);
        draw();
    }

    protected void update(float delta) {
    }

    protected abstract void draw();

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void actionDown(DeviceAction action, long timeNanos) {
    }

    @Override
    public void actionUp(DeviceAction action, long timeNanos, long heldNanos) {
    }

    /** Escreve texto centralizado em x. Chamar entre batch.begin() e batch.end(). */
    protected void drawCentered(BitmapFont font, String text, float centerX, float y, Color color) {
        layout.setText(font, text, color, 0, com.badlogic.gdx.utils.Align.left, false);
        font.draw(game.batch, layout, centerX - layout.width / 2f, y);
    }
}
