package br.com.newe.ms_veiculos.service;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import br.com.newe.ms_veiculos.dto.GerencialResponseDTO;
import br.com.newe.ms_veiculos.dto.OperacionalResponseDTO;

@Service
public class ResponseProfileGerenciamento {

    private final ResponseProfileService responseProfileService;

    public ResponseProfileGerenciamento(ResponseProfileService responseProfileService) {
        this.responseProfileService = responseProfileService;
    }

    public Object criarResposta(
            Authentication authentication,
            OperacionalResponseDTO operacional,
            GerencialResponseDTO gerencial) {
        if (responseProfileService.isGestor(authentication)) {
            return gerencial;
        }

        return operacional;
    }
}