package br.com.tcc.aqualeve.controller;
import br.com.tcc.aqualeve.dto.ReservaAdministradorForm;
import br.com.tcc.aqualeve.entity.Reserva;
import br.com.tcc.aqualeve.entity.StatusReserva;
import br.com.tcc.aqualeve.exception.*;
import br.com.tcc.aqualeve.service.*;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
@Controller
@RequestMapping("/admin/reservas")
public class ReservaAdministradorController {
    private final ReservaService reservas;
    private final ClienteService clientes;
    private final PrainhaService prainhas;
    public ReservaAdministradorController(ReservaService reservas, ClienteService clientes, PrainhaService prainhas) {
        this.reservas = reservas; this.clientes = clientes; this.prainhas = prainhas;
    }
    @GetMapping("/nova")
    public String nova(Model model) {
        model.addAttribute("reservaForm", new ReservaAdministradorForm());
        return formulario(model, null);
    }
    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Integer id, Model model) {
        Reserva r = reservas.findById(id);
        if (r == null) throw new RecursoNaoEncontradoException("Reserva não encontrada.");
        if (r.getStatus() != StatusReserva.CONFIRMADA) throw new RegraNegocioException("Somente reservas confirmadas podem ser editadas.");
        ReservaAdministradorForm f = new ReservaAdministradorForm();
        f.setClienteId(r.getCliente().getIdCliente()); f.setPrainhaId(r.getPrainha().getIdPrainha().longValue());
        f.setCapacidadeReserva(r.getCapacidadeReserva()); f.setTipoReserva(r.getTipoReserva()); f.setDataHora(r.getDataHora());
        model.addAttribute("reservaForm", f); return formulario(model, id);
    }
    @PostMapping("/salvar")
    public String salvar(@Valid @ModelAttribute("reservaForm") ReservaAdministradorForm form, BindingResult result, Model model, RedirectAttributes flash) {
        if (result.hasErrors()) return formulario(model, null);
        try { reservas.cadastrarReserva(form); }
        catch (RegraNegocioException e) { model.addAttribute("erro", e.getMessage()); return formulario(model, null); }
        flash.addFlashAttribute("sucesso", "Reserva cadastrada para o cliente."); return "redirect:/reservas/listar";
    }
    @PostMapping("/editar/{id}")
    public String atualizar(@PathVariable Integer id, @Valid @ModelAttribute("reservaForm") ReservaAdministradorForm form, BindingResult result, Model model, RedirectAttributes flash) {
        if (result.hasErrors()) return formulario(model, id);
        try { reservas.editarReserva(id, form); }
        catch (RegraNegocioException e) { model.addAttribute("erro", e.getMessage()); return formulario(model, id); }
        flash.addFlashAttribute("sucesso", "Reserva atualizada."); return "redirect:/reservas/listar";
    }
    private String formulario(Model model, Integer numero) {
        model.addAttribute("titulo", numero == null ? "Nova reserva para cliente" : "Editar reserva nº " + numero);
        model.addAttribute("acao", numero == null ? "/admin/reservas/salvar" : "/admin/reservas/editar/" + numero);
        model.addAttribute("prainhas", prainhas.findAll()); model.addAttribute("clientes", clientes.findAll());
        model.addAttribute("tipos", ReservaService.TIPOS); return "reserva/administrador";
    }
}
