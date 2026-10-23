package br.com.newe.api_gateway.security;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

/** Perfis que o ms-usuarios grava no claim "perfil" do token. */
public enum Perfil {
    ADMINISTRADOR,
    GESTOR,
    OPERADOR;

    /**
     * Aceita qualquer caixa: tokens emitidos antes do enum no ms-usuarios trazem
     * "Administrador". Valor ausente ou desconhecido vira vazio - e sem perfil
     * nao ha permissao nenhuma.
     */
    public static Optional<Perfil> de(String valor) {
        if (valor == null) {
            return Optional.empty();
        }
        String normalizado = valor.trim().toUpperCase(Locale.ROOT);
        return Arrays.stream(values()).filter(p -> p.name().equals(normalizado)).findFirst();
    }
}
