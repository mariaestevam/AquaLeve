package br.com.tcc.aqualeve.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Prainha {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer idPrainha;

    @NotBlank(message = "Informe o nome da prainha.")
    @Size(max = 40, message = "O nome deve ter no máximo 40 caracteres.")
    @Column(nullable = false, length = 40)
    private String nomePrainha;

    @NotBlank(message = "Informe a descrição.")
    @Size(max = 1000, message = "A descrição deve ter no máximo 1000 caracteres.")
    @Column(nullable = false, length = 1000)
    private String descricaoPrainha;

    @NotBlank(message = "Informe a cidade.")
    @Size(max = 50, message = "A cidade deve ter no máximo 50 caracteres.")
    @Column(nullable = false, length = 50)
    private String cidadePrainha;

    @NotBlank(message = "Informe os serviços oferecidos.")
    @Size(max = 200, message = "Os serviços devem ter no máximo 200 caracteres.")
    @Column(nullable = false, length = 200)
    private String servicosPrainha;

    @jakarta.persistence.ManyToOne(fetch = jakarta.persistence.FetchType.LAZY)
    @jakarta.persistence.JoinColumn(name = "id_administrador")
    private Administrador administrador;

    // bytea no PostgreSQL: a foto acompanha o backup do banco.
    @Column(name = "imagem", columnDefinition = "bytea")
    private byte[] imagem;
    private String imagemTipo;
    public boolean isTemImagem() { return imagem != null && imagem.length > 0; }
}

