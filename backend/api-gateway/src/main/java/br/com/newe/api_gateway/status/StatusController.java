package br.com.newe.api_gateway.status;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

import br.com.newe.api_gateway.security.JwtAutenticacaoFilter;

/**
 * Situacao dos servicos por tras do gateway, para diagnostico.
 *
 * Consulta o /actuator/health de cada um, que ja considera a conexao com o banco -
 * entao um servico "no ar mas sem banco" aparece como DOWN, e nao como UP. Foi
 * exatamente esse caso que confundiu o time: o ms-usuarios respondia na porta mas
 * travava por falta de conexao no pooler.
 */
@RestController
@RequestMapping("/api/status")
public class StatusController {

    private static final Duration TIMEOUT = Duration.ofSeconds(3);

    private static final String UP = "UP";
    private static final String DOWN = "DOWN";

    private final Map<String, String> servicos;
    private final RestClient restClient;

    public StatusController(
            @Value("${MS_USUARIOS_URL:http://localhost:8081}") String usuarios,
            @Value("${MS_FROTA_URL:http://localhost:8082}") String frota,
            @Value("${MS_OPERACOES_URL:http://localhost:8083}") String operacoes) {

        this.servicos = new LinkedHashMap<>();
        this.servicos.put("ms-usuarios", usuarios);
        this.servicos.put("ms-frota", frota);
        this.servicos.put("ms-operacoes", operacoes);

        // Timeout curto: servico fora do ar tem que responder DOWN rapido, nao
        // deixar a tela de status pendurada.
        SimpleClientHttpRequestFactory fabrica = new SimpleClientHttpRequestFactory();
        fabrica.setConnectTimeout(TIMEOUT);
        fabrica.setReadTimeout(TIMEOUT);

        this.restClient = RestClient.builder().requestFactory(fabrica).build();
    }

    /**
     * Restrito a Administrador. O perfil vem do header que o
     * {@link JwtAutenticacaoFilter} injeta depois de validar o token - nunca do
     * que o cliente mandou, que e descartado antes de chegar aqui.
     */
    @GetMapping
    public ResponseEntity<?> status(
            @RequestHeader(name = JwtAutenticacaoFilter.HEADER_PERFIL, required = false) String perfil) {

        if (!"Administrador".equalsIgnoreCase(perfil == null ? "" : perfil.trim())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("erro", "Disponivel apenas para o perfil Administrador"));
        }

        List<Map<String, Object>> resultado = servicos.entrySet().stream()
                .map(e -> consultar(e.getKey(), e.getValue()))
                .toList();

        boolean todosNoAr = resultado.stream().allMatch(s -> UP.equals(s.get("status")));

        return ResponseEntity.ok(Map.of(
                "geral", todosNoAr ? UP : DOWN,
                "servicos", resultado));
    }

    private Map<String, Object> consultar(String nome, String baseUrl) {
        long inicio = System.currentTimeMillis();
        try {
            restClient.get().uri(baseUrl + "/actuator/health").retrieve().toBodilessEntity();
            return dados(nome, UP, System.currentTimeMillis() - inicio, null);
        } catch (Exception e) {
            // Nao propaga: um servico fora do ar e justamente o que queremos reportar.
            return dados(nome, DOWN, System.currentTimeMillis() - inicio, resumir(e));
        }
    }

    private Map<String, Object> dados(String nome, String status, long ms, String detalhe) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("servico", nome);
        m.put("status", status);
        m.put("tempoMs", ms);
        if (detalhe != null) {
            m.put("detalhe", detalhe);
        }
        return m;
    }

    private String resumir(Exception e) {
        String mensagem = e.getMessage();
        if (mensagem == null) {
            return e.getClass().getSimpleName();
        }
        return mensagem.length() > 120 ? mensagem.substring(0, 120) : mensagem;
    }
}
