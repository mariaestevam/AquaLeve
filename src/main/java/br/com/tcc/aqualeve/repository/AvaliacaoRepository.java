package br.com.tcc.aqualeve.repository;

import br.com.tcc.aqualeve.entity.Avaliacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AvaliacaoRepository extends JpaRepository<Avaliacao, Integer> {

    List<Avaliacao> findByPrainhaIdPrainhaOrderByDataAvaliacaoDescIdAvaliacaoDesc(Integer idPrainha);

    @Query("select avg(a.notaAvaliacao) from Avaliacao a where a.prainha.idPrainha = :idPrainha")
    Double mediaPorPrainha(@Param("idPrainha") Integer idPrainha);
}
