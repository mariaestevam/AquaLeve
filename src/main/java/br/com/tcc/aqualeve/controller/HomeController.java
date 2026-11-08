package br.com.tcc.aqualeve.controller;

import br.com.tcc.aqualeve.service.PrainhaService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final PrainhaService prainhaService;
    @org.springframework.beans.factory.annotation.Autowired private br.com.tcc.aqualeve.service.ReservaService reservas;
    @org.springframework.beans.factory.annotation.Autowired private br.com.tcc.aqualeve.service.ClienteService clientes;
    @org.springframework.beans.factory.annotation.Autowired private br.com.tcc.aqualeve.service.CalendarioService calendarios;

    public HomeController(PrainhaService prainhaService) {
        this.prainhaService = prainhaService;
    }

    @GetMapping("/")
    public String inicio(Model model) {
        model.addAttribute("prainhas", prainhaService.findAll().stream().limit(6).toList());
        return "index";
    }

    @GetMapping("/sobre")
    public String sobre() { return "sobre"; }

    // Painel do administrador (rota protegida: só ADMINISTRADOR)
    @GetMapping("/admin")
    public String painelAdmin(Model model) {
        var lista = reservas.findAll();
        model.addAttribute("totalPrainhas", prainhaService.findAll().size());
        model.addAttribute("totalDatasLiberadas", calendarios.findAll().stream().filter(c -> !c.getDataDisponivel().isBefore(java.time.LocalDate.now())).count());
        model.addAttribute("totalConfirmadas", lista.stream().filter(r -> r.getStatus() == br.com.tcc.aqualeve.entity.StatusReserva.CONFIRMADA).count());
        model.addAttribute("visitantesNoLocal", lista.stream().filter(r -> r.getStatus() == br.com.tcc.aqualeve.entity.StatusReserva.EM_USO).mapToInt(r -> r.getCapacidadeReserva()).sum());
        model.addAttribute("totalClientes", clientes.findAll().size());
        model.addAttribute("ultimasReservas", lista.stream().limit(5).toList());
        return "admin/painel";
    }

    // Para onde o Spring Security manda quem está logado mas não tem permissão
    @GetMapping("/acesso-negado")
    public String acessoNegado(Model model) {
        model.addAttribute("codigo", 403);
        model.addAttribute("titulo", "Acesso negado");
        model.addAttribute("mensagem",
                "Você não tem permissão para acessar esta área, ou sua sessão expirou. Tente entrar novamente.");
        return "erro";
    }
}
