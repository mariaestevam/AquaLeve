package br.com.tcc.aqualeve.repository;

import br.com.tcc.aqualeve.entity.Calendario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface CalendarioRepository extends JpaRepository<Calendario, Integer> {

    List<Calendario> findAllByOrderByDataDisponivelAsc();

    List<Calendario> findByPrainhaIdPrainhaAndDataDisponivelGreaterThanEqualOrderByDataDisponivelAsc(Integer idPrainha, LocalDate data);

    boolean existsByPrainhaIdPrainhaAndDataDisponivel(Integer idPrainha, LocalDate data);
}
