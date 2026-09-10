package br.com.newe.ms_veiculos.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import br.com.newe.ms_veiculos.models.entity.ArquivoImport;
import br.com.newe.ms_veiculos.models.enums.StatusImportacao;
import br.com.newe.ms_veiculos.repository.ArquivoImportRepository;

@Service 
public class ArquivoImportService {
    @Autowired 
    private ArquivoImportRepository repositorio;
    private static final Set<String> EXTENSOES_PERMITIDAS = Set.of("xml", "xlsx");
    private static final Pattern MES_REFERENCIA = Pattern.compile("\\d{4}-(0[1-9]|1[0-2])");

    public ArquivoImportService(ArquivoImportRepository repositorio) {
        this.repositorio = repositorio;
    }

    public ArquivoImport importar(MultipartFile arquivo, String mesReferencia) {
        if(arquivo == null || arquivo.isEmpty()) {
            throw new IllegalArgumentException("Arquivo inválido");
        }
        if (mesReferencia == null || !MES_REFERENCIA.matcher(mesReferencia).matches()) {
            throw new IllegalArgumentException("Mês de referência inválido. Use o formato aaaa-MM");
        }

        String extensao = getFileExtension(arquivo.getOriginalFilename());
        if (!EXTENSOES_PERMITIDAS.contains(extensao)) {
            throw new IllegalArgumentException("Extensão de arquivo não permitida");
        }

        String nomeArquivo = arquivo.getOriginalFilename();
        byte[] conteudoArquivo;
        try {
            conteudoArquivo = arquivo.getBytes();
        } catch (IOException exception) {
            throw new IllegalStateException("Não foi possível ler o arquivo", exception);
        }

        ArquivoImport importacao = new ArquivoImport(
            nomeArquivo,
            arquivo.getSize(),
            extensao,
            mesReferencia,
            conteudoArquivo,
            LocalDateTime.now(),
            StatusImportacao.EM_PROCESSAMENTO

        );
        
        return repositorio.save(importacao);
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            throw new IllegalArgumentException("Arquivo sem extensão");
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase(Locale.ROOT);
    }

}
