package br.com.tcc.aqualeve.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

// diaLiberado = a data existe no calendário da prainha; tiposOcupados = espaços já reservados
@Getter
@AllArgsConstructor
public class DisponibilidadeResponse {

    private final LocalDate data;
    private final boolean diaLiberado;
    private final List<String> tiposOcupados;
}
