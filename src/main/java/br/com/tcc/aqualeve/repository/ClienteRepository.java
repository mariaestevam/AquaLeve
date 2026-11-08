package br.com.tcc.aqualeve.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.tcc.aqualeve.entity.Cliente;

public interface ClienteRepository extends JpaRepository<Cliente, Integer> {

    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select c from Cliente c where c.idCliente = :id")
    Optional<Cliente> buscarParaAtualizar(@org.springframework.data.repository.query.Param("id") Integer id);

    Optional<Cliente> findByEmailClienteIgnoreCase(String emailCliente);

    boolean existsByEmailClienteIgnoreCase(String emailCliente);
}
