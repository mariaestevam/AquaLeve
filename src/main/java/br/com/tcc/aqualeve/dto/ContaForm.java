package br.com.tcc.aqualeve.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Edição de nome e telefone (clientes e administradores)
@Getter
@Setter
@NoArgsConstructor
public class ContaForm {

    @NotBlank(message = "Informe o nome.")
    @Size(max = 40, message = "O nome deve ter no máximo 40 caracteres.")
    private String nome;

    @NotBlank(message = "Informe o telefone.")
    @Size(max = 15, message = "O telefone deve ter no máximo 15 caracteres.")
    private String telefone;
}
