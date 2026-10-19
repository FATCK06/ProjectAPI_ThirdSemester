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
import br.com.logistica.msusuarios.entities.Usuario;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.util.List;

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
                usuarioRepository.findByEmail(email)
                .filter(u -> u.getStatusAtivo() == null || u.getStatusAtivo()) // mesma regra do login
                .ifPresent(usuario -> {
                    var authentication = new UsernamePasswordAuthenticationToken(
                        usuario, null, autoridadesDe(usuario));
                    SecurityContextHolder.getContext().setAuthentication(authentication);    
                }); 
            }
        }

        filterChain.doFilter(request, response);
    }

    private List<GrantedAuthority> autoridadesDe(Usuario usuario) {
        String perfil = usuario.getPerfilAcesso();
        if (perfil == null) return List.of();
        return List.of(new SimpleGrantedAuthority("ROLE_" + perfil.toUpperCase())); 
    }


    private String recuperarToken(HttpServletRequest request){
        var authHeader = request.getHeader("Authorization");
        if (authHeader == null) return null;
        return authHeader.replace("Bearer ", "");
    }
}
