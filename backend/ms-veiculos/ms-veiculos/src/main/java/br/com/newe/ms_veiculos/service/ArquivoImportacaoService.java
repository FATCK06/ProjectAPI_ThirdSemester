package br.com.newe.ms_veiculos.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import br.com.newe.ms_veiculos.models.ArquivoImportacao;
import br.com.newe.ms_veiculos.models.enums.StatusImportacaoEnum;

@Service 
public class ArquivoImportacaoService {
    private final Map<Long, ArquivoImportacao> arquivoImportado = new ConcurrentHashMap<>();

    private final AtomicLong geradorId = new AtomicLong(1);

    public ArquivoImportacao criar(
        MultipartFile arquivo,
        String mesReferencia
    ) throws IOException {
        Long id = geradorId.incrementAndGet();

        ArquivoImportacao importacao = new ArquivoImportacao();
        importacao.setId(id);
        importacao.setArquivoNome(arquivo.getOriginalFilename());
        importacao.setArquivoConteudo(arquivo.getBytes());
        importacao.setMesReferencia(mesReferencia);
        importacao.setDataImportacao(LocalDateTime.now());
        importacao.setStatus(StatusImportacaoEnum.EM_PROCESSAMENTO);

        arquivoImportado.put(id, importacao);

        return importacao;
    }

    public ArquivoImportacao buscarPorId(Long id) {
        return arquivoImportado.get(id);
    }
}
