package br.com.newe.ms_frota.service;

/**
 * Separa o endereco que vem em uma unica coluna do CSV nas colunas logradouro
 * e numero da tabela motoristas.
 *
 * Formato observado no arquivo real da Newelog, em ~1800 registros:
 *   "Rua das Flores, 123 - "          -> logradouro + numero
 *   "Rua das Flores, 123 - Apto 45"   -> idem, com complemento
 *   "Rua das Flores,  - "             -> sem numero (34 registros)
 *
 * O complemento e descartado: motoristas nao tem coluna para ele. Se o time
 * quiser guardar, e uma migration nova (V3) adicionando a coluna.
 */
public record EnderecoPartes(String logradouro, String numero) {

    private static final EnderecoPartes VAZIO = new EnderecoPartes(null, null);

    public static EnderecoPartes de(String bruto) {
        String texto = limpar(bruto);
        if (texto == null) {
            return VAZIO;
        }

        int virgula = texto.lastIndexOf(',');
        if (virgula < 0) {
            return new EnderecoPartes(texto, null);
        }

        String logradouro = limpar(texto.substring(0, virgula));
        // Separa o numero do complemento no primeiro hifen cercado por espacos.
        String numero = limpar(texto.substring(virgula + 1).split("\\s-\\s", 2)[0]);

        return new EnderecoPartes(logradouro, numero);
    }

    private static String limpar(String valor) {
        if (valor == null) {
            return null;
        }
        String texto = valor.trim();
        // Sobra em enderecos como "Rua X, 123 -" onde o hifen fecha a string.
        while (texto.endsWith("-")) {
            texto = texto.substring(0, texto.length() - 1).trim();
        }
        return texto.isEmpty() ? null : texto;
    }
}
