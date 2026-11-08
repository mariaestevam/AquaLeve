package br.com.tcc.aqualeve.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Setter
@Getter
@NoArgsConstructor
public class Reserva {
 
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer numeroReserva;

    @Column(nullable = false)
    private Integer capacidadeReserva;

    @Column(nullable = false)
    private double valorReserva;

    @Column(nullable = false, length = 50)
    private String tipoReserva;

    // RN04: acompanha o status (OCUPADO enquanto a reserva está confirmada/em uso; DISPONIVEL ao finalizar/cancelar)
    @Column(nullable = false, length = 15)
    private String disponibilidadeReserva;

    @Column(nullable = false)
    private LocalDateTime dataHora;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private StatusReserva status;

    // RF09: entrada e saída do visitante
    private LocalDateTime entradaEm;

    private LocalDateTime saidaEm;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_cliente", nullable = false)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_prainha", nullable = false)
    private Prainha prainha;

}
