package br.com.tcc.aqualeve.dto;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
@Getter @Setter
public class RedefinirSenhaForm {
    @NotBlank(message = "Informe a senha atual.")
    private String senhaAtual;
    @NotBlank(message = "Informe a nova senha.")
    @Size(min=8, max=72, message="A nova senha deve ter entre 8 e 72 caracteres.")
    private String novaSenha;
    @NotBlank(message = "Confirme a nova senha.")
    private String confirmarSenha;
}
