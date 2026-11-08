package br.com.tcc.aqualeve.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import br.com.tcc.aqualeve.dto.CalendarioForm;
import br.com.tcc.aqualeve.exception.RegraNegocioException;
import br.com.tcc.aqualeve.service.CalendarioService;
import br.com.tcc.aqualeve.service.PrainhaService;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/calendarios")
public class CalendarioController {
    
     @Autowired
    private CalendarioService calendarioService;

     @Autowired
    private PrainhaService prainhaService;

    //Método para listar as datas liberadas e exibir o formulário para liberar uma nova data
    @GetMapping({"", "/listar"})
    public String listar(Model model) {
        model.addAttribute("calendarioForm", new CalendarioForm());
        carregarListas(model);
        return "calendario/listar";
    }

    //Método para liberar uma data
    @PostMapping("/salvar")
    public String salvar(@Valid @ModelAttribute("calendarioForm") CalendarioForm form,
                         BindingResult result,
                         Model model,
                         RedirectAttributes flash) {
        if (result.hasErrors()) {
            carregarListas(model);
            return "calendario/listar";
        }

        try {
            calendarioService.liberar(form);
        } catch (RegraNegocioException e) {
            model.addAttribute("erro", e.getMessage());
            carregarListas(model);
            return "calendario/listar";
        }

        flash.addFlashAttribute("sucesso", "Data liberada com sucesso.");
        return "redirect:/calendarios";
    }

    @PostMapping("/excluir/{id}")
    public String excluir(@PathVariable("id") Integer id, RedirectAttributes flash) {
        try {
            calendarioService.deleteById(id);
            flash.addFlashAttribute("sucesso", "Data removida do calendário.");
        } catch (RegraNegocioException e) {
            flash.addFlashAttribute("erro", e.getMessage());
        } catch (DataIntegrityViolationException e) {
            flash.addFlashAttribute("erro", "Não é possível remover: existem eventos vinculados a esta data.");
        }
        return "redirect:/calendarios";
    }

    private void carregarListas(Model model) {
        model.addAttribute("prainhas", prainhaService.findAll());
        model.addAttribute("datas", calendarioService.findAll());
    }
}
