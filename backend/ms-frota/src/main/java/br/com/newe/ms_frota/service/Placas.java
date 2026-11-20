package br.com.newe.ms_frota.service;

final class Placas {

    private Placas() {
    }

    /**
     * ABC1234 e ABC1C34 sao a mesma placa: na conversao para Mercosul so o 5o
     * caractere muda, de digito para letra (0=A, 1=B ... 9=J).
     */
    static boolean mesmaConvertida(String a, String b) {
        if (a == null || b == null || a.length() != 7 || b.length() != 7 || a.equals(b)) {
            return false;
        }
        for (int i = 0; i < 7; i++) {
            if (i != 4 && a.charAt(i) != b.charAt(i)) {
                return false;
            }
        }
        char x = a.charAt(4);
        char y = b.charAt(4);
        return Character.isDigit(x) ? y == 'A' + (x - '0') : Character.isDigit(y) && x == 'A' + (y - '0');
    }
}
