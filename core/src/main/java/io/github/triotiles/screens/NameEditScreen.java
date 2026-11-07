package io.github.triotiles.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import io.github.triotiles.TrioTilesGame;
import io.github.triotiles.game.Settings;
import io.github.triotiles.input.DeviceAction;

/**
 * Digitar o nome do paciente só com o aparelho: girar o knob escolhe a letra e clicar escreve.
 * Atalhos: tecla 1 apaga, tecla 2 escreve, tecla 3 termina.
 * A primeira letra de cada palavra sai maiúscula; as outras, minúsculas.
 */
public class NameEditScreen extends BaseScreen {
    private static final String LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZÁÂÃÉÊÍÓÔÕÚÇ";
    private static final String SPACE = "espaço";
    private static final String DELETE = "apagar";
    private static final String DONE = "pronto";
    private static final String[] ITEMS = buildItems();
    private static final int VISIBLE_SIDE = 4;
    private static final Color ACTION_COLOR = new Color(0.85f, 0.62f, 0.15f, 1f);

    private final StringBuilder name = new StringBuilder();
    private int selected;

    public NameEditScreen(TrioTilesGame game) {
        super(game);
        String current = game.settings.patient();
        if (!current.equals(Settings.DEFAULT_PATIENT)) name.append(current);
    }

    private static String[] buildItems() {
        String[] items = new String[LETTERS.length() + 3];
        for (int i = 0; i < LETTERS.length(); i++) items[i] = String.valueOf(LETTERS.charAt(i));
        items[LETTERS.length()] = SPACE;
        items[LETTERS.length() + 1] = DELETE;
        items[LETTERS.length() + 2] = DONE;
        return items;
    }

    @Override
    public void actionDown(DeviceAction action, long timeNanos) {
        switch (action) {
            case KNOB_LEFT -> {
                selected = (selected + ITEMS.length - 1) % ITEMS.length;
                game.sfx.menuMove();
            }
            case KNOB_RIGHT -> {
                selected = (selected + 1) % ITEMS.length;
                game.sfx.menuMove();
            }
            case KNOB_PRESS, LANE_2 -> apply(ITEMS[selected]);
            case LANE_1 -> apply(DELETE);
            case LANE_3 -> apply(DONE);
        }
    }

    private void apply(String item) {
        switch (item) {
            case DELETE -> {
                if (name.length() > 0) name.setLength(name.length() - 1);
                game.sfx.menuMove();
            }
            case DONE -> {
                game.sfx.menuConfirm();
                game.changePatient(name.toString());
                game.setScreen(new SettingsScreen(game, 0));
            }
            case SPACE -> add(' ');
            default -> add(item.charAt(0));
        }
    }

    private void add(char c) {
        if (name.length() >= Settings.MAX_NAME) {
            game.sfx.miss();
            return;
        }
        if (c == ' ' && (name.length() == 0 || name.charAt(name.length() - 1) == ' ')) return;
        boolean wordStart = name.length() == 0 || name.charAt(name.length() - 1) == ' ';
        name.append(wordStart ? Character.toUpperCase(c) : Character.toLowerCase(c));
        game.sfx.hit(1);
    }

    @Override
    protected void draw() {
        float cx = TrioTilesGame.WORLD_WIDTH / 2f;
        ShapeRenderer s = game.shapes;
        s.begin(ShapeRenderer.ShapeType.Filled);
        s.setColor(PANEL_COLOR);
        s.rect(cx - 400, 470, 800, 100);                 // caixa do nome
        s.setColor(HIGHLIGHT_COLOR);
        s.rect(cx - 75, 265, 150, 130);                  // letra escolhida
        s.end();

        game.batch.begin();
        drawCentered(game.fontBig, "Nome do paciente", cx, 700, Color.WHITE);
        String shown = name.length() == 0 ? "(vazio)" : name + "_";
        drawCentered(game.fontBig, shown, cx, 545, name.length() == 0 ? Color.GRAY : Color.WHITE);

        for (int k = -VISIBLE_SIDE; k <= VISIBLE_SIDE; k++) {
            String item = ITEMS[Math.floorMod(selected + k, ITEMS.length)];
            boolean action = item.length() > 1;
            float x = cx + k * 140;
            if (k == 0) {
                drawCentered(action ? game.font : game.fontBig, item, x, action ? 342 : 358,
                    action ? Color.WHITE : Color.WHITE);
            } else {
                Color c = action ? ACTION_COLOR : Color.GRAY;
                drawCentered(game.font, item, x, 342, c);
            }
        }

        drawCentered(game.font, "Gire o knob para escolher, clique para escrever", cx, 190, Color.LIGHT_GRAY);
        drawCentered(game.font, "Tecla 1: apagar   ·   Tecla 2: escrever   ·   Tecla 3: pronto", cx, 140,
            ACTION_COLOR);
        drawCentered(game.font, "Cada paciente tem seu próprio progresso e histórico", cx, 70, Color.LIGHT_GRAY);
        game.batch.end();
    }
}
