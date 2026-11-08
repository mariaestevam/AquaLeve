package br.com.tcc.aqualeve.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.tcc.aqualeve.entity.Administrador;

public interface AdministradorRepository extends JpaRepository<Administrador, Integer>{

    Optional<Administrador> findByEmailAdministradorIgnoreCase(String emailAdministrador);

    boolean existsByEmailAdministradorIgnoreCase(String emailAdministrador);

}
