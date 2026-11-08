package br.com.tcc.aqualeve.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Setter
@Getter
@NoArgsConstructor
public class Eventos {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer idEvento;

    @Column(nullable = false)
    private LocalDate dataEvento;

    @Column(nullable = false, length = 200)
    private String descricaoEvento;

    // O evento acontece em uma data liberada do calendário (e o calendário pertence a uma prainha)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_calendario", nullable = false)
    private Calendario calendario;
}
