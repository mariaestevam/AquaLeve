package br.com.tcc.aqualeve.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;

// Cada linha é uma data LIBERADA para reservas em uma prainha
@Entity
@Table(name = "calendario", uniqueConstraints = @UniqueConstraint(columnNames = {"id_prainha", "data_disponivel"}))
@Getter
@Setter
@NoArgsConstructor
public class Calendario {
    @Id

    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer idCalendario;

    @Column(nullable = false)
    private LocalDate dataDisponivel;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_prainha", nullable = false)
    private Prainha prainha;

}
