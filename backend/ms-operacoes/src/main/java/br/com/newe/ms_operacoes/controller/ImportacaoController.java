package br.com.newe.ms_operacoes.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import br.com.newe.ms_operacoes.models.Importacao;
import br.com.newe.ms_operacoes.models.ResumoImportacao;
import br.com.newe.ms_operacoes.models.Viagem;
import br.com.newe.ms_operacoes.models.enums.StatusImportacaoEnum;
import br.com.newe.ms_operacoes.repository.ViagemRepository;
import br.com.newe.ms_operacoes.security.PerfilResolver;
import br.com.newe.ms_operacoes.service.ImportacaoService;
import br.com.newe.ms_operacoes.service.importacao.ImportacaoProcessamentoService;
import br.com.newe.ms_operacoes.service.importacao.ResultadoValidacao;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Importacao em etapas.
 *
 * O POST apenas guarda o arquivo. Gravar em viagens acontece so no /executar,
 * quando o usuario confirma - antes disso ele pode validar e revisar quantas
 * vezes quiser, sem efeito no banco.
 */
@RestController
@RequestMapping("/api/importacao/arquivos")
public class ImportacaoController {

    private static final List<String> EXTENSOES_ACEITAS = List.of(".csv");

    private final ImportacaoService service;
    private final PerfilResolver perfilResolver;
    private final ImportacaoProcessamentoService processamentoService;
    private final ViagemRepository viagemRepository;

    public ImportacaoController(
            ImportacaoService service,
            PerfilResolver perfilResolver,
            ImportacaoProcessamentoService processamentoService,
            ViagemRepository viagemRepository
    ) {
        this.service = service;
        this.perfilResolver = perfilResolver;
        this.processamentoService = processamentoService;
        this.viagemRepository = viagemRepository;
    }

    // ── Etapa 1: receber ─────────────────────────────────────────────────────

    /**
     * Guarda o arquivo e devolve o id. Nada e parseado nem gravado aqui.
     *
     * Nao recebe mais mes de referencia: a planilha e trimestral, e o mes de cada
     * viagem sai da coluna Data da propria linha.
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> receber(
            @RequestParam("arquivo") MultipartFile arquivo,
            HttpServletRequest request
    ) throws IOException {

        if (arquivo.isEmpty()) {
            return erro(HttpStatus.BAD_REQUEST, "Arquivo vazio");
        }

        String nome = arquivo.getOriginalFilename();
        if (nome == null || EXTENSOES_ACEITAS.stream().noneMatch(nome.toLowerCase()::endsWith)) {
            return erro(HttpStatus.BAD_REQUEST, "Formato nao aceito. Envie um arquivo .csv");
        }

        Importacao importacao = service.receber(
                nome, arquivo.getBytes(), perfilResolver.resolverUsuario(request));

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "id", importacao.getId(),
                "arquivoNome", importacao.getArquivoNome(),
                "status", importacao.getStatus()));
    }

    // ── Etapa 2: validar (sem gravar) ────────────────────────────────────────

    /**
     * Le o arquivo e devolve colunas detectadas, erros por linha e uma amostra.
     * Pode ser chamado quantas vezes quiser: nao altera viagens.
     */
    @PostMapping("/{id}/validar")
    public ResponseEntity<?> validar(@PathVariable Long id) throws IOException {
        Importacao importacao = service.buscarPorId(id);
        if (importacao == null) {
            return ResponseEntity.notFound().build();
        }

        ResultadoValidacao resultado = processamentoService.validar(importacao);
        return ResponseEntity.ok(resultado);
    }

    // ── Etapa 3: executar (grava) ────────────────────────────────────────────

    /** Grava as viagens. Unico endpoint do fluxo que escreve no banco. */
    @PostMapping("/{id}/executar")
    public ResponseEntity<?> executar(@PathVariable Long id) {
        Importacao importacao = service.buscarPorId(id);
        if (importacao == null) {
            return ResponseEntity.notFound().build();
        }

        // Sem essa trava, recarregar a tela de confirmacao reprocessaria o arquivo.
        if (importacao.getStatus() == StatusImportacaoEnum.CONCLUIDO) {
            return erro(HttpStatus.CONFLICT, "Esta importacao ja foi executada");
        }

        try {
            int gravadas = processamentoService.executar(importacao);
            return ResponseEntity.ok(Map.of("id", id, "status", "CONCLUIDO", "linhasGravadas", gravadas));

        } catch (RuntimeException e) {
            return erro(HttpStatus.UNPROCESSABLE_ENTITY, e.getMessage());
        }
    }

    // ── Consultas ────────────────────────────────────────────────────────────

    @GetMapping
    public ResponseEntity<List<Importacao>> listar() {
        return ResponseEntity.ok(service.listar());
    }

    @GetMapping("/resumo")
    public ResponseEntity<ResumoImportacao> resumo() {
        return ResponseEntity.ok(service.gerarResumo());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Importacao> buscarPorId(@PathVariable Long id) {
        Importacao importacao = service.buscarPorId(id);
        return importacao == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(importacao);
    }

    @GetMapping("/{id}/conteudo")
    public ResponseEntity<byte[]> conteudo(@PathVariable Long id) {
        Importacao importacao = service.buscarPorId(id);
        if (importacao == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_PLAIN)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + importacao.getArquivoNome() + "\"")
                .body(importacao.getArquivoConteudo());
    }

    @GetMapping("/{id}/viagens")
    public ResponseEntity<List<Viagem>> viagens(@PathVariable Long id) {
        if (service.buscarPorId(id) == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(viagemRepository.findByImportacaoId(id));
    }

    /**
     * Ids dos motoristas desta importacao. Nome e CPF ficam no ms-frota: quem
     * precisar do cadastro consulta /api/motoristas la com estes ids.
     */
    @GetMapping("/{id}/motoristas")
    public ResponseEntity<List<UUID>> motoristas(@PathVariable Long id) {
        if (service.buscarPorId(id) == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(viagemRepository.buscarIdsMotoristasPorArquivo(id));
    }

    private ResponseEntity<Map<String, String>> erro(HttpStatus status, String mensagem) {
        return ResponseEntity.status(status).body(Map.of("erro", mensagem));
    }
}
