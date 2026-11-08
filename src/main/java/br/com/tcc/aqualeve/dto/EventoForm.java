package br.com.tcc.aqualeve.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class EventoForm {

    @NotNull(message = "Selecione a data liberada do calendário.")
    private Long calendarioId;

    @NotNull(message = "Informe a data do evento.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dataEvento;

    @NotBlank(message = "Informe a descrição do evento.")
    @Size(max = 200, message = "A descrição deve ter no máximo 200 caracteres.")
    private String descricaoEvento;
}
