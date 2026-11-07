package io.github.triotiles.game;

/**
 * Configurações do paciente: nome, mão usada, velocidade,
 * tamanho da janela de acerto, volume e ajuste de atraso. Quem salva é o {@code SettingsStore}.
 */
public class Settings {
    public static final String DEFAULT_PATIENT = "Paciente";
    public static final int MAX_NAME = 16;
    public static final int LATENCY_MIN_MS = -100;
    public static final int LATENCY_MAX_MS = 200;
    public static final int LATENCY_STEP_MS = 10;
    public static final int VOLUME_STEP = 10;

    /**
     * Mão que joga. Não inverte as teclas (isso confundiria): muda qual dedo fica em cada tecla,
     * para a precisão "por dedo" ter o nome certo.
     */
    public enum Hand {
        RIGHT("Direita", "indicador", "médio", "anelar"),
        LEFT("Esquerda", "anelar", "médio", "indicador");

        public final String label;
        private final String[] fingers;

        Hand(String label, String... fingers) {
            this.label = label;
            this.fingers = fingers;
        }

        /** Nome do dedo na tecla da trilha 0, 1 ou 2. */
        public String finger(int lane) {
            return fingers[lane];
        }
    }

    /** Velocidade base: espaça as notas e muda a rapidez com que caem. */
    public enum Tempo {
        SLOW("Lenta", 0.8), NORMAL("Normal", 1.0), FAST("Rápida", 1.2);

        public final String label;
        public final double factor;

        Tempo(String label, double factor) {
            this.label = label;
            this.factor = factor;
        }
    }

    /** Tamanho da janela de acerto (o quanto o toque pode se afastar do momento certo). */
    public enum Window {
        WIDE("Larga", 1.25), NORMAL("Normal", 1.0), NARROW("Estreita", 0.8);

        public final String label;
        public final double factor;

        Window(String label, double factor) {
            this.label = label;
            this.factor = factor;
        }
    }

    private String patient = DEFAULT_PATIENT;
    private Hand hand = Hand.RIGHT;
    private Tempo tempo = Tempo.NORMAL;
    private Window window = Window.NORMAL;
    private int volumePercent = 80;
    private int latencyMs;

    public String patient() {
        return patient;
    }

    /** Nome sem espaços nas pontas, até {@link #MAX_NAME} letras. Vazio volta ao padrão. */
    public void setPatient(String name) {
        String n = name == null ? "" : name.trim();
        if (n.length() > MAX_NAME) n = n.substring(0, MAX_NAME).trim();
        patient = n.isEmpty() ? DEFAULT_PATIENT : n;
    }

    public Hand hand() {
        return hand;
    }

    public void setHand(Hand hand) {
        this.hand = hand;
    }

    public Tempo tempo() {
        return tempo;
    }

    public void setTempo(Tempo tempo) {
        this.tempo = tempo;
    }

    public Window window() {
        return window;
    }

    public void setWindow(Window window) {
        this.window = window;
    }

    public int volumePercent() {
        return volumePercent;
    }

    public void setVolumePercent(int percent) {
        volumePercent = Math.max(0, Math.min(100, percent));
    }

    /**
     * Ajuste de atraso, em ms: descontado de cada toque. Serve para compensar o atraso do monitor
     * e do computador (positivo = os toques chegam atrasados). Vai de -100 a +200 ms.
     */
    public int latencyMs() {
        return latencyMs;
    }

    public void setLatencyMs(int ms) {
        int clamped = Math.max(LATENCY_MIN_MS, Math.min(LATENCY_MAX_MS, ms));
        latencyMs = Math.round(clamped / (float) LATENCY_STEP_MS) * LATENCY_STEP_MS;
    }

    /** A janela de acerto da fase, ajustada pela configuração. */
    public Judge judgeFor(LevelData level) {
        return level.judge().scaled(window.factor);
    }

    /** Velocidade de queda das notas da fase, ajustada pela configuração. */
    public float speedFor(LevelData level) {
        return (float) (level.speed * tempo.factor);
    }

    /** Sequência de notas da fase, ajustada pela velocidade escolhida. */
    public Chart chartFor(LevelData level, long seed) {
        return Chart.generate(level, seed, tempo.factor);
    }
}
