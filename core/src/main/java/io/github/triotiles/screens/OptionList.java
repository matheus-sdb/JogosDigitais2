package io.github.triotiles.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import io.github.triotiles.audio.Sfx;
import io.github.triotiles.input.DeviceAction;

/** Lista vertical de opções controlada pelo knob: girar escolhe, clicar confirma. */
final class OptionList {
    private static final Color HIGHLIGHT = new Color(0.25f, 0.55f, 0.95f, 1f);
    private static final Color BOX = new Color(0.16f, 0.17f, 0.24f, 1f);

    private final String[] items;
    private final GlyphLayout layout = new GlyphLayout();
    private int selected;

    OptionList(String... items) {
        this.items = items;
    }

    int selected() {
        return selected;
    }

    void select(int index) {
        selected = index;
    }

    /** @return o índice escolhido se o knob foi clicado; -1 se não houve escolha. */
    int handle(DeviceAction action, Sfx sfx) {
        switch (action) {
            case KNOB_LEFT -> {
                selected = (selected + items.length - 1) % items.length;
                sfx.menuMove();
            }
            case KNOB_RIGHT -> {
                selected = (selected + 1) % items.length;
                sfx.menuMove();
            }
            case KNOB_PRESS -> {
                sfx.menuConfirm();
                return selected;
            }
            default -> { }
        }
        return -1;
    }

    /** Caixas das opções. Chamar entre shapes.begin(Filled) e end(). */
    void drawBoxes(ShapeRenderer s, float centerX, float topY, float width, float height, float gap) {
        for (int i = 0; i < items.length; i++) {
            s.setColor(i == selected ? HIGHLIGHT : BOX);
            s.rect(centerX - width / 2f, topY - i * (height + gap) - height, width, height);
        }
    }

    /** Textos das opções. Chamar entre batch.begin() e end(). */
    void drawLabels(SpriteBatch batch, BitmapFont font, float centerX, float topY, float height, float gap) {
        for (int i = 0; i < items.length; i++) {
            layout.setText(font, items[i], Color.WHITE, 0, com.badlogic.gdx.utils.Align.left, false);
            float y = topY - i * (height + gap) - height / 2f + layout.height / 2f;
            font.draw(batch, layout, centerX - layout.width / 2f, y);
        }
    }
}
