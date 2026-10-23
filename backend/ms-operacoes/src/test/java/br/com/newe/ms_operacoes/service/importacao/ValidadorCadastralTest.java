package br.com.newe.ms_operacoes.service.importacao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ValidadorCadastralTest {

    private static final String CPF_VALIDO = "12345678909";
    private static final String CNPJ_VALIDO = "11222333000181";
    private static final String PIS_VALIDO = "12012345672";

    @Nested
    class Cpf {

        @Test
        void valido() {
            assertThat(ValidadorCadastral.cpf(CPF_VALIDO)).isEqualTo(CPF_VALIDO);
        }

        @Test
        void comMascaraDevolveSoOsDigitos() {
            assertThat(ValidadorCadastral.cpf("123.456.789-09")).isEqualTo(CPF_VALIDO);
        }

        @Test
        void digitoVerificadorErrado() {
            assertThatThrownBy(() -> ValidadorCadastral.cpf("12345678900"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("CPF invalido: digito verificador nao confere");
        }

        @Test
        void todosIguais() {
            // 111.111.111-11 passa no calculo do digito, por isso a regra a parte.
            assertThatThrownBy(() -> ValidadorCadastral.cpf("111.111.111-11"))
                    .hasMessage("CPF invalido: todos os digitos iguais");
        }

        @Test
        void comLetrasERecusadoEmVezDeDescartado() {
            assertThatThrownBy(() -> ValidadorCadastral.cpf("12a34"))
                    .hasMessage("CPF invalido: contem letras");
            assertThatThrownBy(() -> ValidadorCadastral.cpf("abc123"))
                    .hasMessage("CPF invalido: contem letras");
        }

        @Test
        void comCaractereForaDaMascara() {
            assertThatThrownBy(() -> ValidadorCadastral.cpf("123#456#789#09"))
                    .hasMessageStartingWith("CPF invalido: contem caracteres");
        }

        @Test
        void cnpjNoCampoCpf() {
            assertThatThrownBy(() -> ValidadorCadastral.cpf("11.222.333/0001-81"))
                    .hasMessage("CPF invalido: 14 digitos, parece um CNPJ");
        }

        @Test
        void tamanhoErrado() {
            assertThatThrownBy(() -> ValidadorCadastral.cpf("1234567890"))
                    .hasMessage("CPF invalido: 10 digitos, esperado 11");
        }
    }

    @Nested
    class Cnpj {

        @Test
        void valido() {
            assertThat(ValidadorCadastral.cnpj(CNPJ_VALIDO)).isEqualTo(CNPJ_VALIDO);
        }

        @Test
        void comMascaraDevolveSoOsDigitos() {
            assertThat(ValidadorCadastral.cnpj("11.222.333/0001-81")).isEqualTo(CNPJ_VALIDO);
        }

        @Test
        void digitoVerificadorErrado() {
            assertThatThrownBy(() -> ValidadorCadastral.cnpj("11222333000182"))
                    .hasMessage("CNPJ invalido: digito verificador nao confere");
        }

        @Test
        void todosIguais() {
            assertThatThrownBy(() -> ValidadorCadastral.cnpj("00000000000000"))
                    .hasMessage("CNPJ invalido: todos os digitos iguais");
        }

        @Test
        void comLetras() {
            assertThatThrownBy(() -> ValidadorCadastral.cnpj("11.222.333/0001-8X"))
                    .hasMessage("CNPJ invalido: contem letras");
        }

        @Test
        void cpfNoCampoCnpj() {
            assertThatThrownBy(() -> ValidadorCadastral.cnpj("123.456.789-09"))
                    .hasMessage("CNPJ invalido: 11 digitos, parece um CPF");
        }
    }

    @Nested
    class CpfOuCnpj {

        @Test
        void aceitaCpfECnpjValidos() {
            assertThat(ValidadorCadastral.cpfOuCnpj("123.456.789-09")).isEqualTo(CPF_VALIDO);
            assertThat(ValidadorCadastral.cpfOuCnpj("11.222.333/0001-81")).isEqualTo(CNPJ_VALIDO);
        }

        @Test
        void aplicaODigitoVerificadorDeCadaUm() {
            assertThatThrownBy(() -> ValidadorCadastral.cpfOuCnpj("12345678900"))
                    .hasMessage("CPF invalido: digito verificador nao confere");
            assertThatThrownBy(() -> ValidadorCadastral.cpfOuCnpj("11222333000182"))
                    .hasMessage("CNPJ invalido: digito verificador nao confere");
        }

        @Test
        void todosIguais() {
            assertThatThrownBy(() -> ValidadorCadastral.cpfOuCnpj("11111111111"))
                    .hasMessage("CPF invalido: todos os digitos iguais");
        }

        @Test
        void comLetras() {
            assertThatThrownBy(() -> ValidadorCadastral.cpfOuCnpj("12a34"))
                    .hasMessage("CPF/CNPJ invalido: contem letras");
        }

        @Test
        void tamanhoQueNaoEhNenhumDosDois() {
            assertThatThrownBy(() -> ValidadorCadastral.cpfOuCnpj("123456789012"))
                    .hasMessage("CPF/CNPJ invalido: 12 digitos, esperado 11 (CPF) ou 14 (CNPJ)");
        }
    }

    @Nested
    class Pis {

        @Test
        void valido() {
            assertThat(ValidadorCadastral.pis(PIS_VALIDO)).isEqualTo(PIS_VALIDO);
        }

        @Test
        void comMascaraDevolveSoOsDigitos() {
            assertThat(ValidadorCadastral.pis("120.12345.67-2")).isEqualTo(PIS_VALIDO);
        }

        @Test
        void digitoVerificadorErrado() {
            assertThatThrownBy(() -> ValidadorCadastral.pis("12012345673"))
                    .hasMessage("PIS invalido: digito verificador nao confere");
        }

        @Test
        void todosIguais() {
            // 000.00000.00-0 passa no calculo do digito, por isso a regra a parte.
            assertThatThrownBy(() -> ValidadorCadastral.pis("00000000000"))
                    .hasMessage("PIS invalido: todos os digitos iguais");
            assertThatThrownBy(() -> ValidadorCadastral.pis("111.11111.11-1"))
                    .hasMessage("PIS invalido: todos os digitos iguais");
        }

        @Test
        void comLetras() {
            assertThatThrownBy(() -> ValidadorCadastral.pis("1201234567X"))
                    .hasMessage("PIS invalido: contem letras");
        }

        @Test
        void tamanhoErrado() {
            assertThatThrownBy(() -> ValidadorCadastral.pis("1201234567"))
                    .hasMessage("PIS invalido: 10 digitos, esperado 11");
        }
    }

    @Nested
    class Cep {

        @Test
        void validoComESemMascara() {
            assertThat(ValidadorCadastral.cep("12245000")).isEqualTo("12245000");
            assertThat(ValidadorCadastral.cep("12245-000")).isEqualTo("12245000");
        }

        @Test
        void comLetras() {
            assertThatThrownBy(() -> ValidadorCadastral.cep("12245-ABC"))
                    .hasMessage("CEP invalido: contem letras");
        }

        @Test
        void tamanhoErrado() {
            assertThatThrownBy(() -> ValidadorCadastral.cep("1224500"))
                    .hasMessage("CEP invalido: 7 digitos, esperado 8");
        }
    }

    @Nested
    class Placa {

        @Test
        void padraoAntigo() {
            assertThat(ValidadorCadastral.placa("ABC1234")).isEqualTo("ABC1234");
            assertThat(ValidadorCadastral.placa("abc-1234")).isEqualTo("abc-1234");
        }

        @Test
        void padraoMercosul() {
            assertThat(ValidadorCadastral.placa("ABC1D23")).isEqualTo("ABC1D23");
            assertThat(ValidadorCadastral.placa("abc-1d23")).isEqualTo("abc-1d23");
        }

        @Test
        void foraDoPadrao() {
            assertThatThrownBy(() -> ValidadorCadastral.placa("AB12345"))
                    .hasMessage("Placa invalida: 'AB12345' fora do padrao AAA9999 ou AAA9A99");
            assertThatThrownBy(() -> ValidadorCadastral.placa("ABC12D3"))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> ValidadorCadastral.placa("ABC123"))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    class Uf {

        @Test
        void valida() {
            assertThat(ValidadorCadastral.uf("SP")).isEqualTo("SP");
            assertThat(ValidadorCadastral.uf("df")).isEqualTo("DF");
        }

        @Test
        void siglaInexistente() {
            assertThatThrownBy(() -> ValidadorCadastral.uf("XX"))
                    .hasMessage("UF invalida: 'XX' nao e uma UF brasileira");
        }

        @Test
        void foraDoFormato() {
            assertThatThrownBy(() -> ValidadorCadastral.uf("Sao Paulo"))
                    .hasMessage("UF invalida: 'Sao Paulo', esperado 2 letras");
            assertThatThrownBy(() -> ValidadorCadastral.uf("S1"))
                    .hasMessage("UF invalida: 'S1', esperado 2 letras");
        }
    }
}
