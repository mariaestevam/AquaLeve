package br.com.tcc.aqualeve.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import br.com.tcc.aqualeve.dto.EventoForm;
import br.com.tcc.aqualeve.service.CalendarioService;
import br.com.tcc.aqualeve.service.EventosService;
import jakarta.validation.Valid;


@Controller
@RequestMapping("/eventos")
public class EventosController {
    
    //injeção de depêndencia da service de eventos
    @Autowired
    private EventosService eventoService;

    @Autowired
    private CalendarioService calendarioService;

    //Método para listar todos os eventos (tela pública; o formulário aparece só para o administrador)
    @GetMapping({"", "/listar"})
    public String listar(Model model) {
        model.addAttribute("eventoForm", new EventoForm());
        carregarListas(model);
        return "evento/listar";
    }

    //Método para salvar um evento
    @PostMapping("/salvar")
    public String salvar(@Valid @ModelAttribute("eventoForm") EventoForm form,
                         BindingResult result,
                         Model model,
                         RedirectAttributes flash) {
        if (result.hasErrors()) {
            carregarListas(model);
            return "evento/listar";
        }
        try {
            eventoService.cadastrar(form);
        } catch (br.com.tcc.aqualeve.exception.RegraNegocioException e) {
            model.addAttribute("erro", e.getMessage());
            carregarListas(model);
            return "evento/listar";
        }
        flash.addFlashAttribute("sucesso", "Evento cadastrado com sucesso.");
        return "redirect:/eventos";
    }

    //Método para excluir um evento
    @PostMapping("/excluir/{id}")
    public String excluir(@PathVariable("id") Integer id, RedirectAttributes flash) {
        eventoService.deleteById(id);
        flash.addFlashAttribute("sucesso", "Evento excluído.");
        return "redirect:/eventos";
    }

    private void carregarListas(Model model) {
        model.addAttribute("eventos", eventoService.findAll());
        model.addAttribute("datas", calendarioService.findAll());
    }
    
}
