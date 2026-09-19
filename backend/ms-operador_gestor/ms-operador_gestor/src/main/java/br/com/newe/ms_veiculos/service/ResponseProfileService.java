package br.com.newe.ms_veiculos.service;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class ResponseProfileService {

    public boolean isGestor(Authentication authentication) {
        return authentication.getAuthorities()
                .stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_GESTOR"));
    }
}
