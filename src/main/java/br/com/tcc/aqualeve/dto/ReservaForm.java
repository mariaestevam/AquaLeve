package br.com.tcc.aqualeve.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

// Sem clienteId (vem do token), sem valor e sem status (definidos pelo sistema)
@Getter
@Setter
@NoArgsConstructor
public class ReservaForm {

    @NotNull(message = "Selecione a prainha.")
    private Long prainhaId;

    @NotBlank(message = "O tipo da reserva é obrigatório.")
    @Size(max = 50, message = "O tipo da reserva deve ter no máximo 50 caracteres.")
    private String tipoReserva;

    @NotNull(message = "A capacidade da reserva é obrigatória.")
    @Min(value = 1, message = "A capacidade deve ser maior que zero.")
    private Integer capacidadeReserva;

    @NotNull(message = "A data e hora da reserva são obrigatórias.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime dataHora;
}
