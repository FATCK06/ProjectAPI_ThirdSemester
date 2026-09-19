package br.com.newe.ms_veiculos;

import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import br.com.newe.ms_veiculos.dto.GerencialResponseDTO;
import br.com.newe.ms_veiculos.dto.OperacionalResponseDTO;
import br.com.newe.ms_veiculos.service.ResponseProfileGerenciamento;
import br.com.newe.ms_veiculos.service.ResponseProfileService;

class ResponseProfileGerenciamentoTest {

        private final ResponseProfileService responseProfileService = new ResponseProfileService();

        private final ResponseProfileGerenciamento gerenciamento = new ResponseProfileGerenciamento(
                        responseProfileService);

        @Test
        void deveRetornarRespostaGerencialParaGestor() {

                Authentication authentication = new UsernamePasswordAuthenticationToken(
                                "gestor@teste.com",
                                null,
                                java.util.List.of(
                                                new SimpleGrantedAuthority("ROLE_GESTOR"),
                                                new SimpleGrantedAuthority("ROLE_OPERADOR")));

                OperacionalResponseDTO operacional = criarOperacional();
                GerencialResponseDTO gerencial = criarGerencial();

                Object resposta = gerenciamento.criarResposta(
                                authentication,
                                operacional,
                                gerencial);

                assertSame(gerencial, resposta);
        }

        @Test
        void deveRetornarRespostaOperacionalParaOperador() {

                Authentication authentication = new UsernamePasswordAuthenticationToken(
                                "operador@teste.com",
                                null,
                                java.util.List.of(
                                                new SimpleGrantedAuthority("ROLE_OPERADOR")));

                OperacionalResponseDTO operacional = criarOperacional();
                GerencialResponseDTO gerencial = criarGerencial();

                Object resposta = gerenciamento.criarResposta(
                                authentication,
                                operacional,
                                gerencial);

                assertSame(operacional, resposta);
        }

        private OperacionalResponseDTO criarOperacional() {
                return new OperacionalResponseDTO(
                                "M001",
                                "Filial 01",
                                null,
                                "Motorista Teste",
                                "00000000000",
                                "00000000000",
                                null,
                                "Rua Teste",
                                "00000-000",
                                "Centro",
                                "São Paulo",
                                "SP",
                                "Chefe",
                                "Vigilante 1",
                                "Vigilante 2",
                                "Agregado",
                                "00000000000000",
                                "00000000000",
                                "Regime",
                                "Veículo",
                                "Reboque 1",
                                "Reboque 2",
                                "Reboque 3",
                                "São Paulo",
                                "Serviço",
                                "NF001",
                                100.0,
                                100.0,
                                10.0,
                                "Finalizado",
                                100.0,
                                1000.0,
                                100.0,
                                0.0,
                                100.0,
                                1,
                                2,
                                3,
                                4,
                                5,
                                "A",
                                "Observação",
                                "OK",
                                "usuario");
        }

        private GerencialResponseDTO criarGerencial() {
                return new GerencialResponseDTO(
                                "M001",
                                "Filial 01",
                                null,
                                "Motorista Teste",
                                "00000000000",
                                "00000000000",
                                null,
                                "Rua Teste",
                                "00000-000",
                                "Centro",
                                "São Paulo",
                                "SP",
                                "Chefe",
                                "Vigilante 1",
                                "Vigilante 2",
                                "Agregado",
                                "00000000000000",
                                "00000000000",
                                "Regime",
                                "Veículo",
                                "Reboque 1",
                                "Reboque 2",
                                "Reboque 3",
                                "São Paulo",
                                "Serviço",
                                "NF001",
                                100.0,
                                100.0,
                                10.0,
                                null,
                                null,
                                null,
                                "Finalizado",
                                100.0,
                                1000.0,
                                100.0,
                                0.0,
                                100.0,
                                null,
                                null,
                                null,
                                null,
                                1,
                                2,
                                3,
                                4,
                                5,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                "A",
                                "Observação",
                                "OK",
                                "usuario");
        }

        @Test
        void operadorNaoDeveReceberRespostaGerencial() {

                Authentication authentication = new UsernamePasswordAuthenticationToken(
                                "operador@teste.com",
                                null,
                                java.util.List.of(
                                                new SimpleGrantedAuthority("ROLE_OPERADOR")));

                OperacionalResponseDTO operacional = criarOperacional();
                GerencialResponseDTO gerencial = criarGerencial();

                Object resposta = gerenciamento.criarResposta(
                                authentication,
                                operacional,
                                gerencial);

                assertSame(operacional, resposta);
                assertNotSame(gerencial, resposta);
        }

        @Test
        void gestorNaoDeveReceberRespostaOperacional() {

                Authentication authentication = new UsernamePasswordAuthenticationToken(
                                "gestor@teste.com",
                                null,
                                java.util.List.of(
                                                new SimpleGrantedAuthority("ROLE_GESTOR")));

                OperacionalResponseDTO operacional = criarOperacional();
                GerencialResponseDTO gerencial = criarGerencial();

                Object resposta = gerenciamento.criarResposta(
                                authentication,
                                operacional,
                                gerencial);

                assertSame(gerencial, resposta);
                assertNotSame(operacional, resposta);
        }

}
