package io.github.triotiles.input;

/** As 6 ações físicas do aparelho (3 teclas + knob). O jogo só conhece estas ações, nunca teclas. */
public enum DeviceAction {
    LANE_1("Tecla 1", true),
    LANE_2("Tecla 2", true),
    LANE_3("Tecla 3", true),
    KNOB_LEFT("Knob esquerda", false),
    KNOB_RIGHT("Knob direita", false),
    KNOB_PRESS("Knob clique", false);

    public final String label;
    public final boolean lane;

    DeviceAction(String label, boolean lane) {
        this.label = label;
        this.lane = lane;
    }

    /** Índice da trilha (0, 1 ou 2), ou -1 se não for uma tecla de trilha. */
    public int laneIndex() {
        return lane ? ordinal() : -1;
    }

    /** A ação da trilha 0, 1 ou 2. */
    public static DeviceAction forLane(int laneIndex) {
        return switch (laneIndex) {
            case 0 -> LANE_1;
            case 1 -> LANE_2;
            case 2 -> LANE_3;
            default -> throw new IllegalArgumentException("trilha inválida: " + laneIndex);
        };
    }
}
