package io.github.triotiles.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import io.github.triotiles.game.Pipo;

/**
 * Desenha o Pipo só com formas (sem imagens): um músico redondo atrás de um tecladinho
 * de 3 teclas, igual ao aparelho do paciente. Chamar entre shapes.begin(Filled) e end().
 */
final class PipoRenderer {
    private static final Color BODY = new Color(0.98f, 0.78f, 0.30f, 1f);
    private static final Color BODY_DARK = new Color(0.85f, 0.62f, 0.20f, 1f);
    private static final Color INK = new Color(0.10f, 0.10f, 0.14f, 1f);
    private static final Color MOUTH = new Color(0.55f, 0.15f, 0.20f, 1f);
    private static final Color CHEEK = new Color(1f, 0.55f, 0.55f, 1f);
    private static final Color SWEAT = new Color(0.55f, 0.80f, 1f, 1f);
    private static final Color SHADOW = new Color(0.04f, 0.05f, 0.08f, 1f);
    private static final Color KEYBOARD = new Color(0.20f, 0.21f, 0.28f, 1f);

    private PipoRenderer() {
    }

    /**
     * @param x      centro, na altura do chão
     * @param y      chão
     * @param s      escala (1 = corpo com ~100 px de largura)
     * @param lanes  cores das 3 teclas do tecladinho
     */
    static void draw(ShapeRenderer r, Pipo pipo, float x, float y, float s, Color[] lanes) {
        Pipo.Mood mood = pipo.mood();
        float t = pipo.clock();
        float p = pipo.moodProgress();
        float dx = 0, dy;
        switch (mood) {
            case HAPPY -> dy = (float) Math.sin(Math.PI * p) * 28 * s;
            case DANCING -> {
                dx = (float) Math.sin(t * 7) * 10 * s;
                dy = Math.abs((float) Math.sin(t * 7)) * 14 * s;
            }
            case OOPS -> {
                dx = (float) Math.sin(pipo.moodTime() * 45) * 6 * s * (1 - p);
                dy = -4 * s * (1 - p);
            }
            default -> dy = (float) Math.sin(t * 2.2) * 3 * s;
        }
        boolean happy = mood == Pipo.Mood.HAPPY || mood == Pipo.Mood.DANCING;

        // sombra (diminui quando pula)
        float shadowW = 90 * s - Math.max(0, dy) * 0.8f;
        r.setColor(SHADOW);
        r.ellipse(x - shadowW / 2, y - 8 * s, shadowW, 16 * s);

        // corpo
        float cx = x + dx;
        float cy = y + dy + 62 * s;
        r.setColor(BODY);
        r.ellipse(cx - 50 * s, cy - 50 * s, 100 * s, 98 * s);

        // braços até o teclado
        r.setColor(BODY_DARK);
        r.rectLine(cx - 40 * s, cy - 18 * s, x - 26 * s, y + 34 * s, 9 * s);
        r.rectLine(cx + 40 * s, cy - 18 * s, x + 26 * s, y + 34 * s, 9 * s);

        // olhos
        float eyeY = cy + 12 * s;
        for (int side = -1; side <= 1; side += 2) {
            float ex = cx + side * 18 * s;
            if (happy) { // olhos fechados de alegria: ^ ^
                r.setColor(INK);
                r.arc(ex, eyeY - 4 * s, 9 * s, 0, 180);
                r.setColor(BODY);
                r.arc(ex, eyeY - 7 * s, 9 * s, 0, 180);
            } else if (mood == Pipo.Mood.OOPS) { // olhos apertados: > <  (pontas viradas para o nariz)
                r.setColor(INK);
                r.rectLine(ex + 7 * s * side, eyeY + 6 * s, ex - 6 * s * side, eyeY, 3.5f * s);
                r.rectLine(ex - 6 * s * side, eyeY, ex + 7 * s * side, eyeY - 6 * s, 3.5f * s);
            } else {
                r.setColor(Color.WHITE);
                r.circle(ex, eyeY, 10 * s);
                r.setColor(INK);
                r.circle(ex, eyeY - 3 * s, 5 * s);
            }
        }

        // boca e bochechas
        float mouthY = cy - 14 * s;
        if (happy) {
            r.setColor(MOUTH);
            r.arc(cx, mouthY + 4 * s, 13 * s, 180, 180);
            r.setColor(CHEEK);
            r.circle(cx - 32 * s, cy - 4 * s, 7 * s);
            r.circle(cx + 32 * s, cy - 4 * s, 7 * s);
        } else if (mood == Pipo.Mood.OOPS) {
            r.setColor(MOUTH);
            r.circle(cx, mouthY - 2 * s, 6 * s);
            r.setColor(SWEAT); // gota de suor
            r.circle(cx + 46 * s, cy + 26 * s, 6 * s);
            r.triangle(cx + 40.5f * s, cy + 28 * s, cx + 51.5f * s, cy + 28 * s, cx + 46 * s, cy + 40 * s);
        } else {
            r.setColor(INK);
            r.arc(cx, mouthY + 4 * s, 11 * s, 180, 180);
            r.setColor(BODY);
            r.arc(cx, mouthY + 7 * s, 11 * s, 180, 180);
        }

        // notinhas musicais saindo da cabeça
        if (mood == Pipo.Mood.HAPPY && p < 0.85f && pipo.lastLane() >= 0) {
            drawNote(r, cx + 44 * s, cy + 46 * s + p * 50 * s, s, lanes[pipo.lastLane()]);
        } else if (mood == Pipo.Mood.DANCING) {
            for (int i = 0; i < 2; i++) {
                float phase = (t * 0.9f + i * 0.5f) % 1f;
                float side = i == 0 ? 1 : -1;
                drawNote(r, cx + side * 50 * s, cy + 40 * s + phase * 60 * s, s, lanes[(i * 2 + (int) t) % 3]);
            }
        }

        // tecladinho de 3 teclas (fica parado: o Pipo se mexe atrás dele)
        r.setColor(KEYBOARD);
        r.rect(x - 60 * s, y + 10 * s, 120 * s, 28 * s);
        r.rect(x - 50 * s, y - 2 * s, 8 * s, 14 * s);
        r.rect(x + 42 * s, y - 2 * s, 8 * s, 14 * s);
        for (int lane = 0; lane < 3; lane++) {
            Color c = lanes[lane];
            boolean lit = happy && pipo.lastLane() == lane && (mood == Pipo.Mood.DANCING || p < 0.6f);
            float k = lit ? 1f : 0.45f;
            r.setColor(c.r * k, c.g * k, c.b * k, 1f);
            r.rect(x - 54 * s + lane * 37 * s, y + 15 * s, 34 * s, 18 * s);
        }
    }

    private static void drawNote(ShapeRenderer r, float x, float y, float s, Color color) {
        r.setColor(color);
        r.ellipse(x - 8 * s, y - 6 * s, 16 * s, 12 * s);
        r.rectLine(x + 6 * s, y, x + 6 * s, y + 26 * s, 3 * s);
        r.rectLine(x + 6 * s, y + 26 * s, x + 14 * s, y + 18 * s, 3 * s);
    }
}
