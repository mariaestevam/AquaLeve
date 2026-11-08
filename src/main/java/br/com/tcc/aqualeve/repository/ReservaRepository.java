package br.com.tcc.aqualeve.repository;

import br.com.tcc.aqualeve.entity.Reserva;
import br.com.tcc.aqualeve.entity.StatusReserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ReservaRepository extends JpaRepository<Reserva, Integer> {

    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Reserva r where r.numeroReserva = :id")
    java.util.Optional<Reserva> buscarParaAtualizar(@Param("id") Integer id);

    List<Reserva> findAllByOrderByDataHoraDesc();

    List<Reserva> findByClienteIdClienteOrderByDataHoraDesc(Integer idCliente);

    // RN01/RN02: quantas reservas ativas já ocupam esse tipo de espaço nessa prainha dentro do período (o dia)
    @Query("select count(r) from Reserva r where r.prainha.idPrainha = :idPrainha and r.tipoReserva = :tipo "
            + "and r.dataHora >= :inicio and r.dataHora < :fim and r.status in :status")
    long contarAtivas(@Param("idPrainha") Integer idPrainha,
                      @Param("tipo") String tipo,
                      @Param("inicio") LocalDateTime inicio,
                      @Param("fim") LocalDateTime fim,
                      @Param("status") List<StatusReserva> status);

    @Query("select count(r) from Reserva r where r.prainha.idPrainha = :idPrainha and r.tipoReserva = :tipo "
            + "and r.dataHora >= :inicio and r.dataHora < :fim and r.status in :status and r.numeroReserva <> :numero")
    long contarAtivasExceto(@Param("idPrainha") Integer idPrainha, @Param("tipo") String tipo,
        @Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim,
        @Param("status") List<StatusReserva> status, @Param("numero") Integer numero);

    // Tipos de espaço já ocupados na prainha dentro do período (o dia)
    @Query("select distinct r.tipoReserva from Reserva r where r.prainha.idPrainha = :idPrainha "
            + "and r.dataHora >= :inicio and r.dataHora < :fim and r.status in :status")
    List<String> tiposOcupados(@Param("idPrainha") Integer idPrainha,
                               @Param("inicio") LocalDateTime inicio,
                               @Param("fim") LocalDateTime fim,
                               @Param("status") List<StatusReserva> status);
}
