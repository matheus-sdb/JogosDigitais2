package io.github.triotiles.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.TimeUtils;
import io.github.triotiles.TrioTilesGame;
import io.github.triotiles.game.Chart;
import io.github.triotiles.game.LevelData;
import io.github.triotiles.game.Note;
import io.github.triotiles.game.Pipo;
import io.github.triotiles.game.Playfield;
import io.github.triotiles.game.Score;
import io.github.triotiles.game.SessionRecord;
import io.github.triotiles.game.SessionStats;
import io.github.triotiles.game.Settings;
import io.github.triotiles.input.DeviceAction;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Uma fase: as notas caem nas 3 trilhas e o paciente toca com o aparelho.
 * Notas simples (tecla), longas (segurar), acordes (duas teclas) e de giro (knob), conforme a fase.
 * Ao terminar, registra o progresso e abre a {@link ResultScreen}. Clique do knob: pausa.
 */
public class GameScreen extends BaseScreen {
    private static final double END_DELAY = 1.0;    // s depois da última nota até mostrar o resultado

    // ---- desenho ----
    private static final float LANE_W = 170;
    private static final float LANE_GAP = 10;
    private static final float LANES_W = Playfield.LANES * LANE_W + (Playfield.LANES - 1) * LANE_GAP;
    private static final float LANES_X = (TrioTilesGame.WORLD_WIDTH - LANES_W) / 2f;
    private static final float PAD_Y = 25;
    private static final float PAD_H = 75;
    private static final float HIT_Y = 150;          // linha de acerto
    private static final float NOTE_H = 40;
    private static final float HOLD_BODY_MARGIN = 38;
    private static final float FEEDBACK_TIME = 0.45f;
    private static final float PIPO_X = LANES_X + LANES_W + (TrioTilesGame.WORLD_WIDTH - LANES_X - LANES_W) / 2f;

    private static final Color LANE_BG = new Color(0.09f, 0.10f, 0.14f, 1f);
    private static final Color LANES_FRAME = new Color(0.04f, 0.04f, 0.06f, 1f);
    private static final Color MISSED_NOTE = new Color(0.30f, 0.30f, 0.35f, 1f);
    private static final Color SPIN_COLOR = new Color(0.72f, 0.45f, 0.95f, 1f);
    private static final Color CHORD_LINK = new Color(0.85f, 0.85f, 0.90f, 1f);

    private static final float PAUSE_OPTION_W = 420;
    private static final float PAUSE_OPTION_H = 58;
    private static final float PAUSE_OPTION_GAP = 14;
    private static final float PAUSE_TOP = 430;
    private static final float INTRO_TOP = 560;

    private final LevelData level;
    private final Playfield field;
    /** Velocidade de queda (px/s) e ajuste de atraso (s), já com as Configurações aplicadas. */
    private final float speed;
    private final double latencySeconds;
    private final Pipo pipo = new Pipo();
    private final OptionList pauseMenu = new OptionList("Continuar", "Reiniciar fase", "Voltar ao menu");
    private long startNanos;
    private long pausedNanos;
    private long pauseStart = -1; // -1 = jogando
    private boolean finished;
    private float clock;

    private final float[] feedbackTimer = new float[Playfield.LANES];
    private final String[] feedbackText = new String[Playfield.LANES];
    private final Color[] feedbackColor = new Color[Playfield.LANES];
    private final Color tmp = new Color();

    private final Playfield.Listener events = new Playfield.Listener() {
        @Override
        public void onMiss(Note n) {
            showFeedback(feedbackLane(n), "Perdeu", BAD_COLOR);
            pipo.onMiss();
            game.sfx.miss();
        }

        @Override
        public void onHoldCompleted(Note n) {
            showFeedback(n.lane, "Segurou!", GOOD_COLOR);
            game.sfx.holdCompleted(n.lane);
        }
    };

    public GameScreen(TrioTilesGame game, LevelData level) {
        super(game);
        this.level = level;
        Settings settings = game.settings;
        Chart chart = settings.chartFor(level, TimeUtils.millis());
        field = new Playfield(chart, settings.judgeFor(level));
        speed = settings.speedFor(level);
        latencySeconds = settings.latencyMs() / 1000.0;
    }

    @Override
    public void show() {
        super.show();
        startNanos = TimeUtils.nanoTime();
    }

    // ---------- tempo ----------

    private boolean isPaused() {
        return pauseStart >= 0;
    }

    /** Tempo da fase em segundos no instante dado. Fica parado durante a pausa. */
    private double phaseTime(long nanos) {
        long t = isPaused() ? pauseStart : nanos;
        return (t - startNanos - pausedNanos) / 1e9;
    }

    /**
     * Momento de um toque no tempo da fase, descontado o ajuste de atraso das Configurações
     * (compensa o atraso do monitor e do computador).
     */
    private double inputTime(long timeNanos) {
        return phaseTime(timeNanos > 0 ? timeNanos : TimeUtils.nanoTime()) - latencySeconds;
    }

    private void setPaused(boolean pause) {
        long now = TimeUtils.nanoTime();
        if (pause && !isPaused()) {
            pauseStart = now;
        } else if (!pause && isPaused()) {
            pausedNanos += now - pauseStart;
            pauseStart = -1;
        }
    }

    /** Janela minimizada ou sem foco: pausa sozinho. */
    @Override
    public void pause() {
        if (!finished) setPaused(true);
    }

    // ---------- aparelho ----------

    @Override
    public void actionDown(DeviceAction action, long timeNanos) {
        if (finished) return; // indo para o resultado
        if (isPaused()) {
            if (action.lane) return;
            switch (pauseMenu.handle(action, game.sfx)) {
                case 0 -> setPaused(false);
                case 1 -> game.setScreen(new GameScreen(game, level));
                case 2 -> game.setScreen(new MenuScreen(game));
                default -> { }
            }
            return;
        }
        double t = inputTime(timeNanos);
        switch (action) {
            case KNOB_PRESS -> {
                pauseMenu.select(0);
                game.sfx.menuConfirm();
                setPaused(true);
            }
            case KNOB_LEFT -> spin(Note.SPIN_LEFT, t);
            case KNOB_RIGHT -> spin(Note.SPIN_RIGHT, t);
            default -> press(action.laneIndex(), t);
        }
    }

    private void press(int lane, double t) {
        Note hit = field.press(lane, t);
        if (hit != null) {
            showFeedback(lane, hit.judgement().label, colorOf(hit.judgement()));
            pipo.onHit(lane, field.score().combo());
            game.sfx.hit(lane);
        } else {
            showFeedback(lane, "Errou", BAD_COLOR);
            pipo.onMiss();
            game.sfx.miss();
        }
    }

    private void spin(int direction, double t) {
        Note hit = field.spin(direction, t);
        if (hit == null) return; // giro sem nota não é erro: o knob manda vários passos por volta
        showFeedback(1, hit.judgement().label, colorOf(hit.judgement()));
        pipo.onHit(-1, field.score().combo());
        game.sfx.spin();
    }

    @Override
    public void actionUp(DeviceAction action, long timeNanos, long heldNanos) {
        if (finished || !action.lane) return;
        Note n = field.release(action.laneIndex(), inputTime(timeNanos));
        if (n != null && n.holdState() == Note.HoldState.BROKEN) {
            showFeedback(n.lane, "Soltou cedo", BAD_COLOR);
            pipo.onMiss();
            game.sfx.miss();
        } else if (n != null) {
            showFeedback(n.lane, "Segurou!", GOOD_COLOR);
            game.sfx.holdCompleted(n.lane);
        }
    }

    private static int feedbackLane(Note n) {
        return n.lane >= 0 ? n.lane : 1; // giro: aviso na trilha do meio
    }

    private void showFeedback(int lane, String text, Color color) {
        feedbackText[lane] = text;
        feedbackColor[lane] = color;
        feedbackTimer[lane] = FEEDBACK_TIME;
    }

    // ---------- lógica por frame ----------

    @Override
    protected void update(float delta) {
        clock += delta;
        for (int i = 0; i < Playfield.LANES; i++) feedbackTimer[i] = Math.max(0, feedbackTimer[i] - delta);
        if (isPaused() || finished) return;
        pipo.update(delta);

        double t = phaseTime(TimeUtils.nanoTime());
        field.update(t, events);
        if (field.isFinished() && t > field.chart().endTime() + END_DELAY) finish();
    }

    private void finish() {
        finished = true;
        SessionStats stats = SessionStats.from(field);
        LevelData unlocked = game.progress.record(level, stats.accuracy);
        game.saveProgress();
        String date = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES).toString();
        game.history.add(SessionRecord.of(stats, level, game.settings, date));
        game.historyStore.save(game.history);
        game.setScreen(new ResultScreen(game, level, stats, unlocked));
    }

    // ---------- desenho ----------

    private static float laneX(int lane) {
        return LANES_X + lane * (LANE_W + LANE_GAP);
    }

    @Override
    protected void draw() {
        double t = phaseTime(TimeUtils.nanoTime());
        ShapeRenderer s = game.shapes;
        s.begin(ShapeRenderer.ShapeType.Filled);
        StageRenderer.draw(s, level.stage, PIPO_X, clock);
        s.setColor(LANES_FRAME); // moldura escura: o cenário não aparece entre as trilhas
        s.rect(LANES_X - 12, 0, LANES_W + 24, TrioTilesGame.WORLD_HEIGHT);

        for (int lane = 0; lane < Playfield.LANES; lane++) {
            float x = laneX(lane);
            s.setColor(LANE_BG);
            s.rect(x, 0, LANE_W, TrioTilesGame.WORLD_HEIGHT);

            Color c = LANE_COLORS[lane];
            float k = game.device.isPressed(DeviceAction.forLane(lane)) ? 1f : 0.35f;
            s.setColor(c.r * k, c.g * k, c.b * k, 1f);
            s.rect(x, PAD_Y, LANE_W, PAD_H);
        }

        drawChordLinks(s, t);
        for (Note n : field.chart().notes()) {
            float headY = noteY(n.time, t);
            if (headY - NOTE_H / 2f > TrioTilesGame.WORLD_HEIGHT) break; // as próximas estão ainda mais acima
            switch (n.type) {
                case TAP -> drawTap(s, n, headY);
                case HOLD -> drawHold(s, n, headY, t);
                case SPIN -> drawSpin(s, n, headY);
            }
        }

        s.setColor(Color.WHITE);
        s.rect(LANES_X - 12, HIT_Y - 2, LANES_W + 24, 4);

        if (showIntro(t)) { // painel atrás do texto de introdução, para as primeiras notas não atrapalharem a leitura
            float h = 125 + introLines() * 45; // título + "um dedo em cada tecla" + uma linha por tipo de nota novo
            s.setColor(PANEL_COLOR);
            s.rect(TrioTilesGame.WORLD_WIDTH / 2f - 380, INTRO_TOP + 25 - h, 760, h);
        }

        PipoRenderer.draw(s, pipo, PIPO_X, StageRenderer.FLOOR_Y + 5, 1.25f, LANE_COLORS);

        if (isPaused()) {
            float cx = TrioTilesGame.WORLD_WIDTH / 2f;
            s.setColor(PANEL_COLOR);
            s.rect(cx - 300, 150, 600, 430);
            pauseMenu.drawBoxes(s, cx, PAUSE_TOP, PAUSE_OPTION_W, PAUSE_OPTION_H, PAUSE_OPTION_GAP);
        }
        s.end();

        game.batch.begin();
        drawHud(t);
        drawFeedback();
        if (isPaused()) drawPause();
        game.batch.end();
    }

    private float noteY(double noteTime, double t) {
        return (float) (HIT_Y + (noteTime - t) * speed);
    }

    private void drawTap(ShapeRenderer s, Note n, float headY) {
        if (n.state() == Note.State.HIT) return;
        float y = headY - NOTE_H / 2f;
        if (y + NOTE_H < 0) return;
        s.setColor(n.state() == Note.State.MISSED ? MISSED_NOTE : LANE_COLORS[n.lane]);
        s.rect(laneX(n.lane) + 8, y, LANE_W - 16, NOTE_H);
    }

    private void drawHold(ShapeRenderer s, Note n, float headY, double t) {
        Note.HoldState hs = n.holdState();
        if (hs == Note.HoldState.COMPLETED) return;
        float tailY = noteY(n.endTime(), t);
        if (tailY < 0) return;
        float x = laneX(n.lane);
        boolean grey = n.state() == Note.State.MISSED || hs == Note.HoldState.BROKEN;
        Color c = grey ? MISSED_NOTE : LANE_COLORS[n.lane];

        float bodyBottom = hs == Note.HoldState.HOLDING ? HIT_Y : headY; // segurando: a barra vai sendo "consumida"
        float k = hs == Note.HoldState.HOLDING ? 1f : 0.6f;
        s.setColor(c.r * k, c.g * k, c.b * k, 1f);
        s.rect(x + HOLD_BODY_MARGIN, bodyBottom, LANE_W - 2 * HOLD_BODY_MARGIN, Math.max(0, tailY - bodyBottom));
        s.setColor(c);
        s.rect(x + 8, tailY - 8, LANE_W - 16, 16); // fim da nota longa
        if (hs != Note.HoldState.HOLDING) s.rect(x + 8, headY - NOTE_H / 2f, LANE_W - 16, NOTE_H);
        else s.rect(x + 8, HIT_Y - 10, LANE_W - 16, 20); // brilho na linha enquanto segura
    }

    private void drawSpin(ShapeRenderer s, Note n, float headY) {
        if (n.state() == Note.State.HIT) return;
        float y = headY - NOTE_H / 2f;
        if (y + NOTE_H < 0) return;
        s.setColor(n.state() == Note.State.MISSED ? MISSED_NOTE : SPIN_COLOR);
        s.rect(LANES_X + 8, y, LANES_W - 16, NOTE_H);
        s.setColor(Color.WHITE);
        for (int i = 0; i < 3; i++) { // setas para o lado do giro
            float cx = LANES_X + LANES_W * (0.3f + 0.2f * i);
            float d = 12 * n.direction;
            s.triangle(cx - d, y + 8, cx - d, y + NOTE_H - 8, cx + d, y + NOTE_H / 2f);
        }
    }

    /** Liga as duas notas de um acorde com uma faixa, para mostrar que são juntas. */
    private void drawChordLinks(ShapeRenderer s, double t) {
        List<Note> notes = field.chart().notes();
        for (int i = 1; i < notes.size(); i++) {
            Note a = notes.get(i - 1), b = notes.get(i);
            if (a.type != Note.Type.TAP || b.type != Note.Type.TAP || a.time != b.time) continue;
            if (a.state() == Note.State.HIT && b.state() == Note.State.HIT) continue;
            float y = noteY(a.time, t);
            if (y - NOTE_H > TrioTilesGame.WORLD_HEIGHT) break;
            if (y + NOTE_H < 0) continue;
            s.setColor(CHORD_LINK);
            s.rect(laneX(a.lane) + LANE_W / 2f, y - 4, laneX(b.lane) - laneX(a.lane), 8);
        }
    }

    private void drawHud(double t) {
        Score score = field.score();
        float x = 40;
        text(level.fullTitle(), x, 690, Color.WHITE);
        text("Pontos: " + score.points(), x, 630, Color.WHITE);
        if (score.combo() > 1) {
            text("Combo " + score.combo() + "  (" + score.multiplier() + "x)", x, 590,
                score.multiplier() > 1 ? PERFECT_COLOR : Color.LIGHT_GRAY);
        }
        text("Acertos: " + field.hits(), x, 520, GOOD_COLOR);
        text("Perdidas: " + field.misses(), x, 480, BAD_COLOR);
        text("Tecla errada: " + field.strays(), x, 440, BAD_COLOR);
        int done = field.hits() + field.misses();
        text("Notas: " + done + " / " + field.chart().size(), x, 380, Color.LIGHT_GRAY);

        float rx = LANES_X + LANES_W + 40;
        text("Clique o knob:", rx, 690, Color.LIGHT_GRAY);
        text("pausar", rx, 655, Color.LIGHT_GRAY);

        for (int lane = 0; lane < Playfield.LANES; lane++) {
            drawCentered(game.font, String.valueOf(lane + 1), laneX(lane) + LANE_W / 2f, PAD_Y + PAD_H / 2f + 12, Color.WHITE);
        }

        if (showIntro(t)) drawIntro();
    }

    private boolean showIntro(double t) {
        return !isPaused() && t < Chart.FIRST_NOTE - 0.6;
    }

    /** Quantas linhas de explicação de notas novas a fase tem. */
    private int introLines() {
        return (level.holdChance > 0 ? 1 : 0) + (level.chordChance > 0 ? 1 : 0) + (level.spinChance > 0 ? 1 : 0);
    }

    /** Antes da primeira nota: o nome da fase e como tocar as notas novas. */
    private void drawIntro() {
        float cx = TrioTilesGame.WORLD_WIDTH / 2f;
        drawCentered(game.font, level.fullTitle() + "  ·  " + level.stage.label, cx, INTRO_TOP, Color.WHITE);
        float y = INTRO_TOP - 60;
        drawCentered(game.font, "Um dedo em cada tecla...", cx, y, Color.LIGHT_GRAY);
        if (level.holdChance > 0) {
            y -= 45;
            drawCentered(game.font, "Nota comprida: segure a tecla até o fim", cx, y, LANE_COLORS[1]);
        }
        if (level.chordChance > 0) {
            y -= 45;
            drawCentered(game.font, "Notas ligadas: aperte as duas juntas", cx, y, CHORD_LINK);
        }
        if (level.spinChance > 0) {
            y -= 45;
            drawCentered(game.font, "Nota roxa: gire o knob para o lado das setas", cx, y, SPIN_COLOR);
        }
    }

    private void drawFeedback() {
        for (int lane = 0; lane < Playfield.LANES; lane++) {
            if (feedbackTimer[lane] <= 0) continue;
            Color c = feedbackColor[lane];
            tmp.set(c.r, c.g, c.b, Math.min(1f, feedbackTimer[lane] / FEEDBACK_TIME * 1.5f));
            drawCentered(game.font, feedbackText[lane], laneX(lane) + LANE_W / 2f, HIT_Y + 60, tmp);
        }
    }

    private void drawPause() {
        float cx = TrioTilesGame.WORLD_WIDTH / 2f;
        drawCentered(game.fontBig, "Pausado", cx, 545, Color.WHITE);
        pauseMenu.drawLabels(game.batch, game.font, cx, PAUSE_TOP, PAUSE_OPTION_H, PAUSE_OPTION_GAP);
        drawCentered(game.font, "Gire o knob para escolher, clique para confirmar", cx, 195, Color.LIGHT_GRAY);
    }

    private void text(String s, float x, float y, Color color) {
        game.font.setColor(color);
        game.font.draw(game.batch, s, x, y);
    }
}
