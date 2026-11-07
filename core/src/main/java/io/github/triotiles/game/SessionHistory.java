package io.github.triotiles.game;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Histórico de todas as sessões jogadas (de todos os pacientes), em ordem de data.
 * Vira JSON para salvar e CSV para abrir no Excel. A leitura e escrita do arquivo ficam no
 * {@code HistoryStore}.
 */
public class SessionHistory {
    /** Formato do arquivo salvo. */
    public static class FileFormat {
        public int version = 1;
        public ArrayList<SessionRecord> sessions = new ArrayList<>();
    }

    private final List<SessionRecord> sessions = new ArrayList<>();

    public void add(SessionRecord record) {
        sessions.add(record);
    }

    /** Sessões de um paciente (nome sem diferenciar maiúsculas), da mais antiga à mais nova. */
    public List<SessionRecord> forPatient(String patient) {
        List<SessionRecord> out = new ArrayList<>();
        for (SessionRecord r : sessions) {
            if (r.patient != null && r.patient.equalsIgnoreCase(patient)) out.add(r);
        }
        return out;
    }

    /**
     * Ajuste de atraso sugerido, em ms: o quanto o paciente atrasa em média nas últimas
     * {@code count} sessões, sem nenhum ajuste (tendência medida + ajuste que estava em uso).
     * NaN se houver menos sessões que isso.
     */
    public double suggestedLatencyMs(String patient, int count) {
        List<SessionRecord> list = forPatient(patient);
        double sum = 0;
        int n = 0;
        for (int i = list.size() - 1; i >= 0 && n < count; i--) {
            SessionRecord r = list.get(i);
            if (r.tendencyMs != null) {
                sum += r.tendencyMs + r.latencyMs;
                n++;
            }
        }
        return n < count ? Double.NaN : sum / n;
    }

    private static Json json() {
        Json json = new Json(JsonWriter.OutputType.json);
        json.setElementType(FileFormat.class, "sessions", SessionRecord.class);
        json.setUsePrototypes(false);
        return json;
    }

    public String toJson() {
        FileFormat file = new FileFormat();
        file.sessions.addAll(sessions);
        return json().prettyPrint(file);
    }

    public static SessionHistory fromJson(String text) {
        SessionHistory h = new SessionHistory();
        if (text == null || text.isBlank()) return h;
        FileFormat file = json().fromJson(FileFormat.class, text);
        if (file != null && file.sessions != null) {
            for (SessionRecord r : file.sessions) if (r != null) h.add(r);
        }
        return h;
    }

    /** Planilha para o Excel em português: separador ";" e vírgula decimal. */
    public String toCsv() {
        StringBuilder sb = new StringBuilder();
        sb.append("data;paciente;fase;precisao_%;velocidade_ms;consistencia_ms;tendencia_ms;sustentacao_%;")
            .append("giros;acertos;notas;perdidas;teclas_erradas;pontos;maior_combo;")
            .append("tecla1_%;tecla2_%;tecla3_%;mao;velocidade;janela;ajuste_atraso_ms\n");
        for (SessionRecord r : sessions) {
            sb.append(r.date).append(';').append(csvText(r.patient)).append(';').append(r.level).append(';')
                .append(num(r.accuracy * 100)).append(';').append(num(r.speedMs)).append(';')
                .append(num(r.consistencyMs)).append(';').append(num(r.tendencyMs)).append(';')
                .append(r.sustain == null ? "" : num(r.sustain * 100)).append(';')
                .append(r.spinNotes == 0 ? "" : r.spinHits + " de " + r.spinNotes).append(';')
                .append(r.hits).append(';').append(r.totalNotes).append(';').append(r.misses).append(';')
                .append(r.strays).append(';').append(r.points).append(';').append(r.maxCombo).append(';');
            for (int lane = 0; lane < Playfield.LANES; lane++) {
                Double a = r.laneAccuracy == null || r.laneAccuracy.length <= lane ? null : r.laneAccuracy[lane];
                sb.append(a == null ? "" : num(a * 100)).append(';');
            }
            sb.append(csvText(r.hand)).append(';').append(csvText(r.tempo)).append(';')
                .append(csvText(r.window)).append(';').append(r.latencyMs).append('\n');
        }
        return sb.toString();
    }

    private static String num(Double v) {
        return v == null ? "" : String.format(Locale.ROOT, "%.1f", v).replace('.', ',');
    }

    private static String csvText(String s) {
        if (s == null) return "";
        return s.replace(';', ',').replace('\n', ' ');
    }
}
