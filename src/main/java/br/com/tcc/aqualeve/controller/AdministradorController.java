package br.com.tcc.aqualeve.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import br.com.tcc.aqualeve.dto.CadastroAdministradorForm;
import br.com.tcc.aqualeve.dto.ContaForm;
import br.com.tcc.aqualeve.entity.Administrador;
import br.com.tcc.aqualeve.exception.RecursoNaoEncontradoException;
import br.com.tcc.aqualeve.exception.RegraNegocioException;
import br.com.tcc.aqualeve.security.UsuarioLogado;
import br.com.tcc.aqualeve.service.AdministradorService;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/administradores")
public class AdministradorController {
    
     @Autowired
    private AdministradorService administradorService;

    @GetMapping({"", "/listar"})
    public String listar(Model model) {
        model.addAttribute("administradores", administradorService.findAll());
        return "administrador/listar";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("administradorForm", new CadastroAdministradorForm());
        return "administrador/formulario";
    }

    //Método para cadastrar um novo administrador
    @PostMapping("/salvar")
    public String salvar(@Valid @ModelAttribute("administradorForm") CadastroAdministradorForm form,
                         BindingResult result,
                         Model model,
                         RedirectAttributes flash) {
        if (result.hasErrors()) {
            return "administrador/formulario";
        }

        try {
            administradorService.cadastrar(form);
        } catch (RegraNegocioException e) {
            model.addAttribute("erro", e.getMessage());
            return "administrador/formulario";
        }

        flash.addFlashAttribute("sucesso", "Administrador cadastrado com sucesso.");
        return "redirect:/administradores";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable("id") Integer id, Model model) {
        Administrador administrador = buscar(id);
        ContaForm form = new ContaForm();
        form.setNome(administrador.getNomeAdministrador());
        form.setTelefone(administrador.getTelefoneAdministrador());
        model.addAttribute("contaForm", form);
        complementarConta(model, administrador, id);
        return "conta/formulario";
    }

    @PostMapping("/editar/{id}")
    public String salvarEdicao(@PathVariable("id") Integer id,
                               @Valid @ModelAttribute("contaForm") ContaForm form,
                               BindingResult result,
                               Model model,
                               RedirectAttributes flash) {
        if (result.hasErrors()) {
            complementarConta(model, buscar(id), id);
            return "conta/formulario";
        }
        administradorService.atualizarConta(id, form);
        flash.addFlashAttribute("sucesso", "Administrador atualizado com sucesso.");
        return "redirect:/administradores";
    }

    @PostMapping("/excluir/{id}")
    public String excluir(@PathVariable("id") Integer id,
                          @AuthenticationPrincipal Jwt jwt,
                          RedirectAttributes flash) {
        try {
            administradorService.excluir(id, UsuarioLogado.de(jwt).getId());
            flash.addFlashAttribute("sucesso", "Administrador excluído.");
        } catch (RegraNegocioException e) {
            flash.addFlashAttribute("erro", e.getMessage());
        } catch (DataIntegrityViolationException e) {
            flash.addFlashAttribute("erro", "Transfira as prainhas deste administrador para outro responsável antes de excluí-lo.");
        }
        return "redirect:/administradores";
    }

    private Administrador buscar(Integer id) {
        Administrador administrador = administradorService.findById(id);
        if (administrador == null) {
            throw new RecursoNaoEncontradoException("Administrador não encontrado.");
        }
        return administrador;
    }

    private void complementarConta(Model model, Administrador administrador, Integer id) {
        model.addAttribute("emailExibido", administrador.getEmailAdministrador());
        model.addAttribute("titulo", "Editar administrador");
        model.addAttribute("acao", "/administradores/editar/" + id);
        model.addAttribute("voltar", "/administradores");
    }
}
