package io.github.triotiles.save;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import io.github.triotiles.game.SessionHistory;

/**
 * Lê e grava o histórico de sessões em {@code <pasta do usuário>/TrioTiles/}:
 * historico.json (o jogo usa) e historico.csv (para abrir no Excel).
 */
public class HistoryStore {
    private static final String DIR = "TrioTiles";

    private FileHandle json() {
        return Gdx.files.external(DIR + "/historico.json");
    }

    private FileHandle csv() {
        return Gdx.files.external(DIR + "/historico.csv");
    }

    /** Caminho completo da pasta, para mostrar na tela de Progresso. */
    public String folderPath() {
        try {
            return Gdx.files.external(DIR).file().getAbsolutePath();
        } catch (RuntimeException e) {
            return DIR;
        }
    }

    public SessionHistory load() {
        try {
            FileHandle f = json();
            if (!f.exists()) return new SessionHistory();
            return SessionHistory.fromJson(f.readString("UTF-8"));
        } catch (RuntimeException e) {
            // Arquivo estragado: guarda uma cópia para não perder nada e começa um novo.
            Gdx.app.error("HistoryStore", "histórico não carregou; guardando cópia e começando outro", e);
            try {
                json().copyTo(Gdx.files.external(DIR + "/historico-com-erro-" + System.currentTimeMillis() + ".json"));
            } catch (RuntimeException ignored) {
                // sem cópia, segue
            }
            return new SessionHistory();
        }
    }

    public void save(SessionHistory history) {
        try {
            json().writeString(history.toJson(), false, "UTF-8");
            // BOM no início: o Excel reconhece os acentos.
            csv().writeString("﻿" + history.toCsv(), false, "UTF-8");
        } catch (RuntimeException e) {
            Gdx.app.error("HistoryStore", "histórico não foi salvo", e);
        }
    }
}
