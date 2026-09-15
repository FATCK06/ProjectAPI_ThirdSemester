package br.com.logistica.msusuarios.config;

import br.com.logistica.msusuarios.repositories.UsuarioRepository;
import br.com.logistica.msusuarios.services.TokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class SecurityFilter extends OncePerRequestFilter {
    
    @Autowired 
    private TokenService tokenService;

    @Autowired 
    private UsuarioRepository usuarioRepository;

    @Override 
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        var token = this.recuperarToken(request);

        if (token != null) {
            var email = tokenService.validarToken(token);

            if(!email.isEmpty()){
                var usuarioOp = usuarioRepository.findByEmail(email);

                if(usuarioOp.isPresent()){
                    var authentication = new UsernamePasswordAuthenticationToken(usuarioOp.get(), null, Collections.emptyList());
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    private String recuperarToken(HttpServletRequest request){
        var authHeader = request.getHeader("Authorization");
        if (authHeader == null) return null;
        return authHeader.replace("Bearer ", "");
    }
}
