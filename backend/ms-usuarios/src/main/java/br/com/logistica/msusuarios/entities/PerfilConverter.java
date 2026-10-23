package br.com.logistica.msusuarios.entities;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Grava o rotulo ("Administrador") em vez do nome do enum.
 *
 * Com @Enumerated(STRING) o banco passaria a ter "ADMINISTRADOR", e as linhas que
 * ja existem ("Administrador") deixariam de ser lidas - exigiria migrar a tabela.
 * Assim o banco fica como esta e a leitura tolera qualquer caixa.
 */
@Converter(autoApply = true)
public class PerfilConverter implements AttributeConverter<Perfil, String> {

    @Override
    public String convertToDatabaseColumn(Perfil perfil) {
        return perfil == null ? null : perfil.getRotulo();
    }

    @Override
    public Perfil convertToEntityAttribute(String valor) {
        return valor == null ? null : Perfil.de(valor);
    }
}
