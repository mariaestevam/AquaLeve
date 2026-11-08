package br.com.tcc.aqualeve.dto;
import jakarta.validation.constraints.*;
import org.springframework.web.multipart.MultipartFile;
import lombok.Getter;
import lombok.Setter;
@Getter @Setter
public class PrainhaForm {
    private Integer idPrainha;
    @NotBlank(message = "Informe o nome da prainha.")
    @Size(max = 40, message = "O nome deve ter no máximo 40 caracteres.")
    private String nomePrainha;

    @NotBlank(message = "Informe a descrição.")
    @Size(max = 1000, message = "A descrição deve ter no máximo 1000 caracteres.")
    private String descricaoPrainha;

    @NotBlank(message = "Informe a cidade.")
    @Size(max = 50, message = "A cidade deve ter no máximo 50 caracteres.")
    private String cidadePrainha;

    @NotBlank(message = "Informe os serviços oferecidos.")
    @Size(max = 200, message = "Os serviços devem ter no máximo 200 caracteres.")
    private String servicosPrainha;

    private Integer administradorId;
    private MultipartFile imagem;
    private boolean removerImagem;
}
