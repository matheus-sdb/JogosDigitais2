package io.github.triotiles.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import io.github.triotiles.TrioTilesGame;
import io.github.triotiles.game.LevelData;
import io.github.triotiles.game.SessionRecord;
import io.github.triotiles.input.DeviceAction;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Evolução do paciente sessão a sessão (docs/PROPOSTA.md, seção 5.5): um gráfico por métrica.
 * Girar o knob troca a métrica; clicar volta ao menu.
 */
public class ProgressScreen extends BaseScreen {
    /** Quantas sessões aparecem no gráfico (as mais recentes). */
    private static final int MAX_POINTS = 20;

    private enum Metric {
        ACCURACY("Precisão", "%", true, r -> r.accuracy * 100),
        SPEED("Velocidade", "ms", false, r -> r.speedMs),
        CONSISTENCY("Consistência", "ms", false, r -> r.consistencyMs),
        SUSTAIN("Sustentação", "%", true, r -> r.sustain == null ? null : r.sustain * 100);

        final String label;
        final String unit;
        /** true = quanto maior, melhor (precisão); false = quanto menor, melhor (ms). */
        final boolean higherIsBetter;
        final Function<SessionRecord, Double> value;

        Metric(String label, String unit, boolean higherIsBetter, Function<SessionRecord, Double> value) {
            this.label = label;
            this.unit = unit;
            this.higherIsBetter = higherIsBetter;
            this.value = value;
        }
    }

    private static final Metric[] METRICS = Metric.values();
    private static final float CHART_X0 = 170;
    private static final float CHART_X1 = 1190;
    private static final float CHART_Y0 = 200;
    private static final float CHART_Y1 = 545;
    private static final float TAB_Y = 625;
    private static final float TAB_W = 230;
    private static final float TAB_H = 46;
    private static final float LEGEND_Y = 140;
    private static final float SUMMARY_Y = 95;
    private static final int MAX_PATH_CHARS = 32;
    private static final Color GRID = new Color(0.18f, 0.19f, 0.25f, 1f);
    private static final Color LINE = new Color(0.75f, 0.77f, 0.85f, 1f);

    private final List<SessionRecord> sessions;
    private int metric;

    public ProgressScreen(TrioTilesGame game) {
        super(game);
        sessions = game.history.forPatient(game.settings.patient());
    }

    @Override
    public void actionDown(DeviceAction action, long timeNanos) {
        switch (action) {
            case KNOB_LEFT -> {
                metric = (metric + METRICS.length - 1) % METRICS.length;
                game.sfx.menuMove();
            }
            case KNOB_RIGHT -> {
                metric = (metric + 1) % METRICS.length;
                game.sfx.menuMove();
            }
            case KNOB_PRESS -> {
                game.sfx.menuConfirm();
                game.setScreen(new MenuScreen(game));
            }
            default -> { }
        }
    }

    /** As sessões mais recentes que têm valor para a métrica, da mais antiga à mais nova. */
    private List<SessionRecord> points(Metric m) {
        List<SessionRecord> withValue = new ArrayList<>();
        for (SessionRecord r : sessions) if (m.value.apply(r) != null) withValue.add(r);
        int from = Math.max(0, withValue.size() - MAX_POINTS);
        return withValue.subList(from, withValue.size());
    }

    private static float maxY(Metric m, List<SessionRecord> pts) {
        if (m.unit.equals("%")) return 100;
        double max = 0;
        for (SessionRecord r : pts) max = Math.max(max, m.value.apply(r));
        return (float) Math.max(100, Math.ceil(max / 100) * 100); // múltiplo de 100: eixo com 25, 50, 75...
    }

    private static float px(int i, int count) {
        if (count == 1) return (CHART_X0 + CHART_X1) / 2f;
        return CHART_X0 + 30 + i * (CHART_X1 - CHART_X0 - 60) / (count - 1f);
    }

    private static float py(double v, float max) {
        return CHART_Y0 + (float) (v / max) * (CHART_Y1 - CHART_Y0);
    }

    private static float legendX(LevelData level) {
        return CHART_X0 + level.ordinal() * 140;
    }

    private static float tabX(int i) {
        float total = METRICS.length * TAB_W + (METRICS.length - 1) * 12;
        return (TrioTilesGame.WORLD_WIDTH - total) / 2f + i * (TAB_W + 12);
    }

    @Override
    protected void draw() {
        Metric m = METRICS[metric];
        List<SessionRecord> pts = points(m);
        float max = maxY(m, pts);

        ShapeRenderer s = game.shapes;
        s.begin(ShapeRenderer.ShapeType.Filled);
        for (int i = 0; i < METRICS.length; i++) {
            s.setColor(i == metric ? HIGHLIGHT_COLOR : PANEL_COLOR);
            s.rect(tabX(i), TAB_Y - TAB_H, TAB_W, TAB_H);
        }
        for (int g = 0; g <= 4; g++) {
            s.setColor(GRID);
            s.rect(CHART_X0, CHART_Y0 + g * (CHART_Y1 - CHART_Y0) / 4f - 1, CHART_X1 - CHART_X0, 2);
        }
        for (int i = 1; i < pts.size(); i++) {
            s.setColor(LINE);
            s.rectLine(px(i - 1, pts.size()), py(m.value.apply(pts.get(i - 1)), max),
                px(i, pts.size()), py(m.value.apply(pts.get(i)), max), 3);
        }
        for (int i = 0; i < pts.size(); i++) {
            SessionRecord r = pts.get(i);
            s.setColor(LANE_COLORS[Math.max(0, Math.min(2, r.level - 1))]); // cor da fase
            s.circle(px(i, pts.size()), py(m.value.apply(r), max), 8);
        }
        for (LevelData level : LevelData.values()) { // bolinhas da legenda
            s.setColor(LANE_COLORS[level.ordinal()]);
            s.circle(legendX(level) + 8, LEGEND_Y - 10, 8);
        }
        s.end();

        game.batch.begin();
        float cx = TrioTilesGame.WORLD_WIDTH / 2f;
        drawCentered(game.fontBig, "Progresso de " + game.settings.patient(), cx, 705, Color.WHITE);
        for (int i = 0; i < METRICS.length; i++) {
            drawCentered(game.font, METRICS[i].label, tabX(i) + TAB_W / 2f, TAB_Y - TAB_H / 2f + 11, Color.WHITE);
        }
        for (int g = 0; g <= 4; g++) {
            String label = Math.round(max * g / 4f) + (m.unit.equals("%") ? "%" : "");
            text(label, CHART_X0 - 90, CHART_Y0 + g * (CHART_Y1 - CHART_Y0) / 4f + 10, Color.GRAY);
        }
        if (m.unit.equals("ms")) text("(ms)", CHART_X0 - 90, CHART_Y1 + 40, Color.GRAY);

        if (sessions.isEmpty()) {
            drawCentered(game.font, "Nenhuma sessão ainda. Jogue uma fase para começar!", cx, 390, Color.LIGHT_GRAY);
        } else if (pts.isEmpty()) {
            drawCentered(game.font, "Ainda sem dados de " + m.label.toLowerCase()
                + (m == Metric.SUSTAIN ? " (aparece nas Fases 2 e 3)" : ""), cx, 390, Color.LIGHT_GRAY);
        } else {
            drawDates(pts);
            drawSummary(m, pts, cx);
        }

        for (LevelData level : LevelData.values()) {
            text("Fase " + level.number, legendX(level) + 24, LEGEND_Y, LANE_COLORS[level.ordinal()]);
        }
        text("Arquivos: " + shorten(game.historyStore.folderPath(), MAX_PATH_CHARS), CHART_X0 + 470, LEGEND_Y,
            Color.GRAY);
        drawCentered(game.font, "Gire o knob para trocar a métrica, clique para voltar", cx, 45, Color.LIGHT_GRAY);
        game.batch.end();
    }

    private void drawDates(List<SessionRecord> pts) {
        int step = pts.size() <= 10 ? 1 : 2;
        for (int i = 0; i < pts.size(); i += step) {
            drawCentered(game.font, shortDate(pts.get(i).date), px(i, pts.size()), CHART_Y0 - 18, Color.GRAY);
        }
    }

    /** Caminho comprido vira "...final\do\caminho", para caber na tela. */
    private static String shorten(String path, int max) {
        return path.length() <= max ? path : "..." + path.substring(path.length() - (max - 3));
    }

    /** "2026-11-04T15:30" vira "04/11". */
    private static String shortDate(String iso) {
        if (iso == null || iso.length() < 10) return "";
        return iso.substring(8, 10) + "/" + iso.substring(5, 7);
    }

    private void drawSummary(Metric m, List<SessionRecord> pts, float cx) {
        String text;
        Color color = Color.LIGHT_GRAY;
        if (pts.size() < 2) {
            text = "1 sessão até agora. Jogue de novo para ver a evolução.";
        } else {
            double first = m.value.apply(pts.get(0));
            double last = m.value.apply(pts.get(pts.size() - 1));
            double diff = last - first;
            boolean better = m.higherIsBetter ? diff > 0 : diff < 0;
            String amount = Math.round(Math.abs(diff)) + (m.unit.equals("%") ? " pontos" : " ms");
            if (Math.round(Math.abs(diff)) == 0) {
                text = m.label + ": igual à primeira sessão";
            } else {
                text = m.label + (better ? ": melhorou " : ": piorou ") + amount + " desde a primeira sessão ("
                    + pts.size() + " sessões)";
                color = better ? GOOD_COLOR : PERFECT_COLOR;
            }
        }
        drawCentered(game.font, text, cx, SUMMARY_Y, color);
    }

    private void text(String s, float x, float y, Color color) {
        game.font.setColor(color);
        game.font.draw(game.batch, s, x, y);
    }
}
