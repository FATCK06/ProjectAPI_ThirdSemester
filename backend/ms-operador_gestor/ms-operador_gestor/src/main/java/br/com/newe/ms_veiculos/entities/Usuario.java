package br.com.newe.ms_veiculos.entities;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import br.com.newe.ms_veiculos.model.PerfilAcesso;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "usuarios")
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true) 
public class Usuario implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_usuario")
    @EqualsAndHashCode.Include 
    private UUID idUsuario;

    @Column(name = "nome", nullable = false, length = 100)
    private String nome;

    @Column(name = "email", nullable = false, length = 100, unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "perfil_acesso", nullable = false, length = 50)
    private PerfilAcesso perfilAcesso;

    @Column(name = "status_ativo", nullable = false)
    private Boolean statusAtivo = true;

    @CreationTimestamp 
    @Column(name = "criado_em", updatable = false, nullable = false)
    private LocalDateTime criadoEm;

    @Column(name = "senha", nullable = false) 
    private String senha;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (this.perfilAcesso == PerfilAcesso.GESTOR) {
            return List.of(
                new SimpleGrantedAuthority("ROLE_GESTOR"),
                new SimpleGrantedAuthority("ROLE_OPERADOR")
            );
        }
        return List.of(new SimpleGrantedAuthority("ROLE_OPERADOR"));
    }

    @Override
    public String getPassword() {
        return senha;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return statusAtivo;
    }
}
