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
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class Administrador {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer idAdministrador;

    @Column(nullable = false, length = 80)
    private String nomeAdministrador;

    @Column(nullable = false, length = 15)
    private String telefoneAdministrador;

    @Column(nullable = false, length = 100)
    private String emailAdministrador;

    @Column(nullable = false, length = 100)
    private String senhaAdministrador;


}
