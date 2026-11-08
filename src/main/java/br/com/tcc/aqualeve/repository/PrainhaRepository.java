package br.com.tcc.aqualeve.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.tcc.aqualeve.entity.Prainha;

public interface PrainhaRepository extends JpaRepository<Prainha, Integer>{

    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select p from Prainha p where p.idPrainha = :id")
    java.util.Optional<Prainha> buscarParaReserva(@org.springframework.data.repository.query.Param("id") Integer id);

    List<Prainha> findAllByOrderByNomePrainhaAsc();

    List<Prainha> findByCidadePrainhaContainingIgnoreCaseOrderByNomePrainhaAsc(String cidadePrainha);

}
