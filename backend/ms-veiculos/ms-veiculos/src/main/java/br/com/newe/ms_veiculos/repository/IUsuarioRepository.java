package br.com.newe.ms_veiculos.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.newe.ms_veiculos.entities.Usuario;

public interface IUsuarioRepository extends JpaRepository<Usuario, UUID> {

    Usuario findByLogin(String email);
}
