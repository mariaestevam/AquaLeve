package br.com.tcc.aqualeve.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;

@Entity
@Table(name = "avaliacao")
@Getter
@Setter
@NoArgsConstructor
public class Avaliacao {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer idAvaliacao;

    @Column(nullable = false)
    private Integer notaAvaliacao;

    @Column(length = 500)
    private String comentarioAvaliacao;

    @Column(nullable = false)
    private LocalDate dataAvaliacao;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_cliente", nullable = false)
    private Cliente cliente;
    
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_prainha", nullable = false)
    private Prainha prainha;

}
