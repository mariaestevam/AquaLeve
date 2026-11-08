package br.com.tcc.aqualeve.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Repare: NÃO existe campo "perfil". Quem se cadastra por aqui é sempre CLIENTE.
@Getter
@Setter
@NoArgsConstructor
public class CadastroClienteForm {

    @NotBlank(message = "Informe seu nome completo.")
    @Size(max = 40, message = "O nome deve ter no máximo 40 caracteres.")
    private String nome;

    @NotBlank(message = "Informe seu CPF.")
    @Pattern(regexp = "\\d{3}\\.?\\d{3}\\.?\\d{3}-?\\d{2}", message = "Informe um CPF válido.")
    private String cpf;

    @NotBlank(message = "Informe seu e-mail.")
    @Email(message = "Informe um e-mail válido.")
    @Size(max = 100, message = "O e-mail deve ter no máximo 100 caracteres.")
    private String email;

    @NotBlank(message = "Informe seu telefone.")
    @Size(max = 15, message = "O telefone deve ter no máximo 15 caracteres.")
    private String telefone;

    @NotBlank(message = "Crie uma senha.")
    @Size(min = 8, max = 72, message = "A senha deve ter entre 8 e 72 caracteres.")
    private String senha;

    @NotBlank(message = "Confirme sua senha.")
    private String confirmarSenha;
}
