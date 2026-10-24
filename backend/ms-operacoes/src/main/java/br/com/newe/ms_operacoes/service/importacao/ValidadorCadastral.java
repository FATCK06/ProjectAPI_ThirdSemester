package br.com.newe.ms_operacoes.service.importacao;

import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Regras de formato dos campos cadastrais: CPF, CNPJ, PIS, CEP, placa e UF.
 *
 * Cada metodo recebe o texto ja sem o envolucro do Excel e devolve o valor
 * normalizado, ou lanca IllegalArgumentException dizendo o que veio e o que
 * era esperado. Documento so aceita digitos e mascara: letra no meio e erro de
 * digitacao, nao algo a ser descartado em silencio.
 */
public final class ValidadorCadastral {

    /** Digitos e a mascara usual de documento (".", "-", "/" e espacos). */
    private static final Pattern DOCUMENTO_COM_MASCARA = Pattern.compile("[0-9./\\-\\s]+");
    private static final Pattern TEM_LETRA = Pattern.compile("\\p{L}");
    private static final Pattern NAO_DIGITO = Pattern.compile("\\D");

    /** Antiga (AAA9999) e Mercosul (AAA9A99), ja sem hifen e em maiusculas. */
    private static final Pattern PLACA = Pattern.compile("[A-Z]{3}[0-9][A-Z0-9][0-9]{2}");
    private static final Pattern DUAS_LETRAS = Pattern.compile("[A-Z]{2}");

    private static final Set<String> UFS = Set.of(
            "AC", "AL", "AP", "AM", "BA", "CE", "DF", "ES", "GO", "MA", "MT", "MS", "MG", "PA",
            "PB", "PR", "PE", "PI", "RJ", "RN", "RS", "RO", "RR", "SC", "SP", "SE", "TO");

    private static final int[] PESOS_CPF_1 = {10, 9, 8, 7, 6, 5, 4, 3, 2};
    private static final int[] PESOS_CPF_2 = {11, 10, 9, 8, 7, 6, 5, 4, 3, 2};
    private static final int[] PESOS_CNPJ_1 = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
    private static final int[] PESOS_CNPJ_2 = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
    private static final int[] PESOS_PIS = {3, 2, 9, 8, 7, 6, 5, 4, 3, 2};

    private ValidadorCadastral() {
    }

    /** @return os 11 digitos do CPF */
    public static String cpf(String texto) {
        String digitos = digitosDoDocumento("CPF", texto);
        if (digitos.length() == 14) {
            throw invalido("CPF", "14 digitos, parece um CNPJ");
        }
        exigirTamanho("CPF", digitos, 11);
        if (todosIguais(digitos)) {
            throw invalido("CPF", "todos os digitos iguais");
        }
        if (!cpfComDigitoValido(digitos)) {
            throw invalido("CPF", "digito verificador nao confere");
        }
        return digitos;
    }

    /** @return os 14 digitos do CNPJ */
    public static String cnpj(String texto) {
        String digitos = digitosDoDocumento("CNPJ", texto);
        if (digitos.length() == 11) {
            throw invalido("CNPJ", "11 digitos, parece um CPF");
        }
        exigirTamanho("CNPJ", digitos, 14);
        if (todosIguais(digitos)) {
            throw invalido("CNPJ", "todos os digitos iguais");
        }
        if (!cnpjComDigitoValido(digitos)) {
            throw invalido("CNPJ", "digito verificador nao confere");
        }
        return digitos;
    }

    /** Agregado pode ser pessoa fisica ou juridica: o tamanho decide qual regra vale. */
    public static String cpfOuCnpj(String texto) {
        String digitos = digitosDoDocumento("CPF/CNPJ", texto);
        return switch (digitos.length()) {
            case 11 -> cpf(digitos);
            case 14 -> cnpj(digitos);
            default -> throw invalido("CPF/CNPJ",
                    digitos.length() + " digitos, esperado 11 (CPF) ou 14 (CNPJ)");
        };
    }

    /** @return os 11 digitos do PIS */
    public static String pis(String texto) {
        String digitos = digitosDoDocumento("PIS", texto);
        exigirTamanho("PIS", digitos, 11);
        if (todosIguais(digitos)) {
            throw invalido("PIS", "todos os digitos iguais");
        }
        if (digitoVerificador(digitos, PESOS_PIS) != digitos.charAt(10) - '0') {
            throw invalido("PIS", "digito verificador nao confere");
        }
        return digitos;
    }

    /** @return os 8 digitos do CEP */
    public static String cep(String texto) {
        String digitos = digitosDoDocumento("CEP", texto);
        exigirTamanho("CEP", digitos, 8);
        return digitos;
    }

    /** Aceita hifen e minusculas; devolve o texto como veio, a normalizacao da chave fica no ResolvedorFrota. */
    public static String placa(String texto) {
        String normalizada = texto.trim().toUpperCase(Locale.ROOT).replace("-", "");
        if (!PLACA.matcher(normalizada).matches()) {
            throw new IllegalArgumentException("Placa invalida: '" + texto.trim()
                    + "' fora do padrao AAA9999 ou AAA9A99");
        }
        return texto.trim();
    }

    /** @return a sigla em maiusculas */
    public static String uf(String texto) {
        String sigla = texto.trim().toUpperCase(Locale.ROOT);
        if (!DUAS_LETRAS.matcher(sigla).matches()) {
            throw new IllegalArgumentException("UF invalida: '" + texto.trim() + "', esperado 2 letras");
        }
        if (!UFS.contains(sigla)) {
            throw new IllegalArgumentException("UF invalida: '" + sigla + "' nao e uma UF brasileira");
        }
        return sigla;
    }

    private static boolean cpfComDigitoValido(String digitos) {
        return digitoVerificador(digitos, PESOS_CPF_1) == digitos.charAt(9) - '0'
                && digitoVerificador(digitos, PESOS_CPF_2) == digitos.charAt(10) - '0';
    }

    private static boolean cnpjComDigitoValido(String digitos) {
        return digitoVerificador(digitos, PESOS_CNPJ_1) == digitos.charAt(12) - '0'
                && digitoVerificador(digitos, PESOS_CNPJ_2) == digitos.charAt(13) - '0';
    }

    /** Modulo 11 sobre os primeiros pesos.length digitos; resto menor que 2 vira 0. */
    private static int digitoVerificador(String digitos, int[] pesos) {
        int soma = 0;
        for (int i = 0; i < pesos.length; i++) {
            soma += (digitos.charAt(i) - '0') * pesos[i];
        }
        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }

    private static String digitosDoDocumento(String nome, String texto) {
        if (TEM_LETRA.matcher(texto).find()) {
            throw invalido(nome, "contem letras");
        }
        if (!DOCUMENTO_COM_MASCARA.matcher(texto).matches()) {
            throw invalido(nome, "contem caracteres fora de digitos e mascara (. - /)");
        }
        return NAO_DIGITO.matcher(texto).replaceAll("");
    }

    private static void exigirTamanho(String nome, String digitos, int esperado) {
        if (digitos.length() != esperado) {
            throw invalido(nome, digitos.length() + " digitos, esperado " + esperado);
        }
    }

    private static boolean todosIguais(String digitos) {
        return digitos.chars().allMatch(c -> c == digitos.charAt(0));
    }

    private static IllegalArgumentException invalido(String nome, String motivo) {
        return new IllegalArgumentException(nome + " invalido: " + motivo);
    }
}
