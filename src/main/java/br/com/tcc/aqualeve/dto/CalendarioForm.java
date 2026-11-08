package br.com.tcc.aqualeve.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

// Data liberada no calendário de uma prainha (usado pelo administrador)
@Getter
@Setter
@NoArgsConstructor
public class CalendarioForm {

    @NotNull(message = "Selecione a prainha.")
    private Long prainhaId;

    @NotNull(message = "Informe a data disponível.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dataDisponivel;
}
