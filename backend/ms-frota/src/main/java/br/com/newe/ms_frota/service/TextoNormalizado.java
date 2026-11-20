package br.com.newe.ms_frota.service;

import java.text.Normalizer;
import java.util.Locale;
import java.util.Objects;

/** Forma de comparar nomes vindos de planilha: cidade, tipo de veiculo, agregado, motorista. */
public final class TextoNormalizado {

    private TextoNormalizado() {
    }

    /** Sem acento, maiúsculo, e hífen/apóstrofo/espaços viram um espaço só: "Embu-Guaçu" = "EMBU GUACU". */
    public static String de(String texto) {
        String semAcento = Normalizer.normalize(Objects.toString(texto, ""), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return semAcento.toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]+", " ").trim();
    }
}
