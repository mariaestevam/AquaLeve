package br.com.tcc.aqualeve.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Cliente {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer idCliente;

    // Nulo em registros anteriores equivale à versão zero, sem perda de dados.
    private Long versaoSenha = 0L;

    @Column(nullable = false, length = 14)
    private String cpfCliente;

    @Column(nullable = false, length = 40)
    private String nomeCliente;

    @Column(nullable = false, length = 100)
    private String emailCliente;

    @Column(nullable = false, length = 15)
    private String telefoneCliente;

    @Column(nullable = false, length = 100)
    private String senhaCliente;

}
