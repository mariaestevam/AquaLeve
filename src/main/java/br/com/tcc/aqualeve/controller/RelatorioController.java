package br.com.tcc.aqualeve.controller;

import java.time.LocalDate;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import br.com.tcc.aqualeve.entity.Reserva;
import br.com.tcc.aqualeve.entity.StatusReserva;
import br.com.tcc.aqualeve.service.ReservaService;
import br.com.tcc.aqualeve.exception.RegraNegocioException;

@Controller
@RequestMapping("/admin/relatorios")
public class RelatorioController {
    private final ReservaService reservas;
    public RelatorioController(ReservaService reservas) { this.reservas = reservas; }

    @GetMapping
    public String relatorio(@RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate inicio,
                            @RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate fim,
                            Model model) {
        List<Reserva> lista = filtrar(inicio, fim);
        model.addAttribute("reservas", lista);
        model.addAttribute("inicio", inicio); model.addAttribute("fim", fim);
        model.addAttribute("total", lista.size());
        model.addAttribute("confirmadas", lista.stream().filter(r -> r.getStatus() == StatusReserva.CONFIRMADA).count());
        model.addAttribute("emUso", lista.stream().filter(r -> r.getStatus() == StatusReserva.EM_USO).count());
        model.addAttribute("finalizadas", lista.stream().filter(r -> r.getStatus() == StatusReserva.FINALIZADA).count());
        model.addAttribute("canceladas", lista.stream().filter(r -> r.getStatus() == StatusReserva.CANCELADA).count());
        return "admin/relatorio";
    }

    @GetMapping("/csv") @ResponseBody
    public ResponseEntity<byte[]> csv(@RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate inicio,
                                       @RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate fim) {
        StringBuilder texto = new StringBuilder("\uFEFFReserva;Prainha;Cliente;Data;Espaço;Pessoas;Status;Entrada;Saída\r\n");
        for (Reserva r : filtrar(inicio, fim)) {
            Object[] campos = {r.getNumeroReserva(), r.getPrainha().getNomePrainha(), r.getCliente().getNomeCliente(),
                r.getDataHora(), r.getTipoReserva(), r.getCapacidadeReserva(), r.getStatus(), r.getEntradaEm(), r.getSaidaEm()};
            texto.append(java.util.Arrays.stream(campos).map(RelatorioController::celula).collect(java.util.stream.Collectors.joining(";"))).append("\r\n");
        }
        return ResponseEntity.ok().header("Content-Type", "text/csv;charset=UTF-8")
            .header("Content-Disposition", "attachment; filename=relatorio-aqualeve.csv")
            .body(texto.toString().getBytes(StandardCharsets.UTF_8));
    }
    private List<Reserva> filtrar(LocalDate inicio, LocalDate fim) {
        if (inicio != null && fim != null && inicio.isAfter(fim)) throw new RegraNegocioException("A data inicial deve ser anterior à final.");
        return reservas.findAll().stream().filter(r -> inicio == null || !r.getDataHora().toLocalDate().isBefore(inicio))
            .filter(r -> fim == null || !r.getDataHora().toLocalDate().isAfter(fim)).toList();
    }
    private static String celula(Object valor) {
        String s = valor == null ? "" : valor.toString();
        if (s.matches("(?s)^[\\s]*[=+@-].*")) s = "'" + s;
        return "\"" + s.replace("\"", "\"\"") + "\"";
    }
}
