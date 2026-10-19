package io.github.triotiles;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import io.github.triotiles.input.DeviceInput;
import io.github.triotiles.input.DeviceMapping;
import io.github.triotiles.screens.MenuScreen;

/** Classe principal: guarda os recursos compartilhados e troca as telas. */
public class TrioTilesGame extends Game {
    public static final float WORLD_WIDTH = 1280;
    public static final float WORLD_HEIGHT = 720;

    public SpriteBatch batch;
    public ShapeRenderer shapes;
    public BitmapFont font;
    public BitmapFont fontBig;
    public DeviceInput device;

    @Override
    public void create() {
        batch = new SpriteBatch();
        shapes = new ShapeRenderer();
        // Fonte padrão do libGDX por enquanto; trocar por FreeType quando tivermos a arte.
        font = makeFont(1.5f);
        fontBig = makeFont(3.5f);

        device = new DeviceInput(DeviceMapping.standard());
        Gdx.input.setInputProcessor(device);

        setScreen(new MenuScreen(this));
    }

    private BitmapFont makeFont(float scale) {
        BitmapFont f = new BitmapFont();
        f.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        f.getData().setScale(scale);
        f.setUseIntegerPositions(false);
        return f;
    }

    @Override
    public void dispose() {
        if (screen != null) screen.dispose();
        batch.dispose();
        shapes.dispose();
        font.dispose();
        fontBig.dispose();
    }
}
