package br.com.tcc.aqualeve.dto;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
@Getter @Setter
public class ReservaAdministradorForm extends ReservaForm {
    @NotNull(message = "Selecione o cliente.")
    @Positive(message = "Selecione um cliente válido.")
    private Integer clienteId;
}
