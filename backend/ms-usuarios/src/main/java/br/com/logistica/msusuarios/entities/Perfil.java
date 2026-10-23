package br.com.logistica.msusuarios.entities;

import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Perfis de acesso. O nome (ADMINISTRADOR) e o que circula no token e na API;
 * o rotulo (Administrador) e o que fica gravado no banco - ver PerfilConverter.
 */
public enum Perfil {
    OPERADOR("Operador"),
    GESTOR("Gestor"),
    ADMINISTRADOR("Administrador");

    private final String rotulo;

    Perfil(String rotulo) {
        this.rotulo = rotulo;
    }

    public String getRotulo() {
        return rotulo;
    }

    /**
     * Aceita nome ou rotulo, em qualquer caixa ("GESTOR", "Gestor", "gestor").
     *
     * @throws IllegalArgumentException perfil ausente ou inexistente
     */
    public static Perfil de(String valor) {
        String texto = valor == null ? "" : valor.trim().toUpperCase(Locale.ROOT);
        return Arrays.stream(values())
                .filter(p -> p.name().equals(texto))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Perfil invalido: " + valor + ". Use um destes: " + nomes()));
    }

    private static String nomes() {
        return Arrays.stream(values()).map(Enum::name).collect(Collectors.joining(", "));
    }
}
