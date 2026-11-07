package io.github.triotiles.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import io.github.triotiles.TrioTilesGame;
import io.github.triotiles.game.LevelData;

/**
 * Cenário de cada fase, só com formas: o quarto, a praça e o teatro onde o Pipo toca.
 * Os enfeites ficam nos cantos e no lado direito, para não atrapalhar o texto da esquerda.
 * Chamar entre shapes.begin(Filled) e end(), antes das trilhas.
 */
final class StageRenderer {
    private static final float W = TrioTilesGame.WORLD_WIDTH;
    private static final float H = TrioTilesGame.WORLD_HEIGHT;
    /** Altura do chão: o Pipo fica em pé nessa linha. */
    static final float FLOOR_Y = 185;

    private StageRenderer() {
    }

    static void draw(ShapeRenderer r, LevelData.Stage stage, float pipoX, float time) {
        switch (stage) {
            case QUARTO -> quarto(r, pipoX);
            case PRACA -> praca(r, pipoX, time);
            case TEATRO -> teatro(r, pipoX);
        }
    }

    // ---------------- Fase 1: quarto ----------------
    private static void quarto(ShapeRenderer r, float pipoX) {
        r.setColor(0.17f, 0.13f, 0.16f, 1f);               // parede
        r.rect(0, 0, W, H);
        r.setColor(0.20f, 0.15f, 0.19f, 1f);               // rodapé da parede (faixa)
        r.rect(0, FLOOR_Y, W, 14);
        r.setColor(0.33f, 0.22f, 0.15f, 1f);               // piso de madeira
        r.rect(0, 0, W, FLOOR_Y);
        r.setColor(0.28f, 0.19f, 0.13f, 1f);
        for (float y = 30; y < FLOOR_Y; y += 45) r.rect(0, y, W, 3);

        // janela com a lua (direita, acima do Pipo)
        float wx = pipoX - 120, wy = 420, ww = 240, wh = 200;
        r.setColor(0.35f, 0.25f, 0.20f, 1f);
        r.rect(wx - 10, wy - 10, ww + 20, wh + 20);
        r.rect(wx, wy, ww, wh, new Color(0.10f, 0.14f, 0.32f, 1f), new Color(0.10f, 0.14f, 0.32f, 1f),
            new Color(0.04f, 0.05f, 0.14f, 1f), new Color(0.04f, 0.05f, 0.14f, 1f));
        r.setColor(0.95f, 0.92f, 0.75f, 1f);
        r.circle(wx + ww * 0.7f, wy + wh * 0.68f, 24);
        r.setColor(0.35f, 0.25f, 0.20f, 1f);
        r.rect(wx + ww / 2f - 4, wy, 8, wh);
        r.rect(wx, wy + wh / 2f - 4, ww, 8);

        // tapete embaixo do Pipo
        r.setColor(0.45f, 0.20f, 0.25f, 1f);
        r.ellipse(pipoX - 130, FLOOR_Y - 45, 260, 70);
        r.setColor(0.55f, 0.28f, 0.30f, 1f);
        r.ellipse(pipoX - 100, FLOOR_Y - 35, 200, 50);

        // abajur (canto inferior esquerdo, abaixo do texto)
        r.setColor(0.25f, 0.18f, 0.13f, 1f);
        r.rect(70, FLOOR_Y - 20, 80, 22);
        r.rect(105, FLOOR_Y, 10, 80);
        r.setColor(0.95f, 0.80f, 0.45f, 1f);
        r.triangle(65, FLOOR_Y + 80, 155, FLOOR_Y + 80, 110, FLOOR_Y + 140);
    }

    // ---------------- Fase 2: praça ----------------
    private static void praca(ShapeRenderer r, float pipoX, float time) {
        r.rect(0, 0, W, H, new Color(0.10f, 0.16f, 0.30f, 1f), new Color(0.10f, 0.16f, 0.30f, 1f),
            new Color(0.03f, 0.04f, 0.12f, 1f), new Color(0.03f, 0.04f, 0.12f, 1f)); // céu
        // estrelas (posições fixas, piscando devagar)
        for (int i = 0; i < 40; i++) {
            float x = (i * 337) % W;
            float y = 330 + (i * 151) % 380;
            float twinkle = 0.6f + 0.4f * (float) Math.sin(time * 1.5 + i);
            r.setColor(twinkle, twinkle, 0.85f * twinkle, 1f);
            r.circle(x, y, 1.6f + (i % 3) * 0.6f);
        }
        r.setColor(0.95f, 0.93f, 0.80f, 1f);               // lua
        r.circle(W - 110, H - 90, 34);

        r.setColor(0.16f, 0.34f, 0.20f, 1f);               // grama
        r.rect(0, 0, W, FLOOR_Y);
        r.setColor(0.20f, 0.42f, 0.24f, 1f);
        r.rect(0, FLOOR_Y - 12, W, 12);
        r.setColor(0.40f, 0.38f, 0.34f, 1f);               // calçada embaixo do Pipo
        r.ellipse(pipoX - 150, FLOOR_Y - 50, 300, 80);

        tree(r, 80, FLOOR_Y - 10, 1.0f);                   // árvores (esquerda, embaixo do texto)
        tree(r, 210, FLOOR_Y - 25, 0.8f);
        tree(r, W - 60, FLOOR_Y - 5, 0.9f);

        float lx = pipoX - 140;                            // poste de luz ao lado do Pipo
        r.setColor(0.18f, 0.18f, 0.22f, 1f);
        r.rect(lx - 4, FLOOR_Y - 10, 8, 230);
        r.rect(lx - 4, FLOOR_Y + 212, 34, 6);
        r.setColor(1f, 0.88f, 0.55f, 1f);
        r.circle(lx + 28, FLOOR_Y + 205, 11);
    }

    private static void tree(ShapeRenderer r, float x, float ground, float s) {
        r.setColor(0.30f, 0.20f, 0.14f, 1f);
        r.rect(x - 9 * s, ground, 18 * s, 70 * s);
        r.setColor(0.12f, 0.30f, 0.18f, 1f);
        r.circle(x, ground + 95 * s, 48 * s);
        r.setColor(0.15f, 0.36f, 0.21f, 1f);
        r.circle(x - 22 * s, ground + 80 * s, 32 * s);
        r.circle(x + 24 * s, ground + 84 * s, 30 * s);
    }

    // ---------------- Fase 3: teatro ----------------
    private static void teatro(ShapeRenderer r, float pipoX) {
        r.setColor(0.07f, 0.03f, 0.05f, 1f);               // fundo escuro do palco
        r.rect(0, 0, W, H);
        r.setColor(0.36f, 0.22f, 0.12f, 1f);               // palco de madeira
        r.rect(0, 0, W, FLOOR_Y);
        r.setColor(0.30f, 0.18f, 0.10f, 1f);
        for (float x = 0; x < W; x += 80) r.rect(x, 0, 3, FLOOR_Y);
        r.setColor(0.45f, 0.30f, 0.16f, 1f);               // borda do palco
        r.rect(0, FLOOR_Y - 10, W, 10);

        r.setColor(0.55f, 0.48f, 0.30f, 1f);               // luz do holofote no chão
        r.ellipse(pipoX - 140, FLOOR_Y - 55, 280, 80);
        r.setColor(0.70f, 0.62f, 0.38f, 1f);
        r.ellipse(pipoX - 95, FLOOR_Y - 40, 190, 52);

        curtain(r, 0, 90);                                 // cortinas laterais
        curtain(r, W - 90, 90);
        r.setColor(0.50f, 0.06f, 0.10f, 1f);               // bambinela (faixa de cima, fina para não cobrir o texto)
        r.rect(0, H - 18, W, 18);
        r.setColor(0.85f, 0.65f, 0.25f, 1f);               // franja dourada
        r.rect(0, H - 21, W, 3);
    }

    private static void curtain(ShapeRenderer r, float x, float width) {
        for (int i = 0; i < 6; i++) {
            float k = i % 2 == 0 ? 0.55f : 0.42f;
            r.setColor(k, 0.06f, 0.10f, 1f);
            r.rect(x + i * width / 6f, 0, width / 6f + 1, H);
        }
    }
}
