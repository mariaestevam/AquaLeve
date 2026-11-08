package br.com.tcc.aqualeve.repository;

import br.com.tcc.aqualeve.entity.Eventos;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface EventosRepository extends JpaRepository<Eventos, Integer> {

    List<Eventos> findAllByOrderByDataEventoAsc();

    List<Eventos> findByCalendarioPrainhaIdPrainhaAndDataEventoGreaterThanEqualOrderByDataEventoAsc(Integer idPrainha, LocalDate data);
}
