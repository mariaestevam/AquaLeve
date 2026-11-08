package br.com.tcc.aqualeve.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import br.com.tcc.aqualeve.dto.ReservaForm;
import br.com.tcc.aqualeve.exception.RegraNegocioException;
import br.com.tcc.aqualeve.security.UsuarioLogado;
import br.com.tcc.aqualeve.service.PrainhaService;
import br.com.tcc.aqualeve.service.ReservaService;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/reservas")
public class ReservaController {
    
     @Autowired
    private ReservaService reservaService;

     @Autowired
    private PrainhaService prainhaService;

    //Exibe o formulário de nova reserva (cliente)
    @GetMapping("/nova")
    public String nova(@RequestParam(name = "prainhaId", required = false) Long prainhaId, Model model) {
        ReservaForm form = new ReservaForm();
        form.setPrainhaId(prainhaId);
        model.addAttribute("reservaForm", form);
        carregarListas(model);
        return "reserva/formulario";
    }

    //Método para salvar a reserva: o cliente vem do token; valor e status são definidos pelo sistema
    @PostMapping("/salvar")
    public String salvar(@Valid @ModelAttribute("reservaForm") ReservaForm form,
                         BindingResult result,
                         @AuthenticationPrincipal Jwt jwt,
                         Model model,
                         RedirectAttributes flash) {
        if (result.hasErrors()) {
            carregarListas(model);
            return "reserva/formulario";
        }

        try {
            reservaService.reservar(form, UsuarioLogado.de(jwt).getId());
        } catch (RegraNegocioException e) {
            model.addAttribute("erro", e.getMessage());
            carregarListas(model);
            return "reserva/formulario";
        }

        flash.addFlashAttribute("sucesso", "Reserva confirmada com sucesso!");
        return "redirect:/reservas/minhas";
    }

    //Reservas do cliente logado
    @GetMapping("/minhas")
    public String minhas(@AuthenticationPrincipal Jwt jwt, Model model) {
        model.addAttribute("reservas", reservaService.findByCliente(UsuarioLogado.de(jwt).getId().intValue()));
        return "reserva/minhas";
    }

    //Todas as reservas (administrador)
    @GetMapping("/listar")
    public String listar(Model model) {
        model.addAttribute("reservas", reservaService.findAll());
        return "reserva/listar";
    }

    //RF09: registrar a entrada do visitante (administrador)
    @PostMapping("/entrada/{id}")
    public String entrada(@PathVariable("id") Integer id, RedirectAttributes flash) {
        executar(() -> reservaService.registrarEntrada(id), "Entrada registrada.", flash);
        return "redirect:/reservas/listar";
    }

    //RF09: registrar a saída do visitante (administrador)
    @PostMapping("/saida/{id}")
    public String saida(@PathVariable("id") Integer id, RedirectAttributes flash) {
        executar(() -> reservaService.registrarSaida(id), "Saída registrada.", flash);
        return "redirect:/reservas/listar";
    }

    //Cliente cancela a própria reserva; administrador cancela qualquer uma
    @PostMapping("/cancelar/{id}")
    public String cancelar(@PathVariable("id") Integer id,
                           @AuthenticationPrincipal Jwt jwt,
                           RedirectAttributes flash) {
        UsuarioLogado usuario = UsuarioLogado.de(jwt);
        executar(() -> reservaService.cancelar(id, usuario), "Reserva cancelada.", flash);
        return usuario.isAdministrador() ? "redirect:/reservas/listar" : "redirect:/reservas/minhas";
    }

    private void executar(Runnable acao, String mensagemSucesso, RedirectAttributes flash) {
        try {
            acao.run();
            flash.addFlashAttribute("sucesso", mensagemSucesso);
        } catch (RegraNegocioException e) {
            flash.addFlashAttribute("erro", e.getMessage());
        }
    }

    private void carregarListas(Model model) {
        model.addAttribute("prainhas", prainhaService.findAll());
        model.addAttribute("tipos", ReservaService.TIPOS);
    }
}
