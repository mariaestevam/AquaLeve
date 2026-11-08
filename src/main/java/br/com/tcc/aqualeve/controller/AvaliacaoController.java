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

import br.com.tcc.aqualeve.dto.AvaliacaoForm;
import br.com.tcc.aqualeve.entity.Prainha;
import br.com.tcc.aqualeve.exception.RecursoNaoEncontradoException;
import br.com.tcc.aqualeve.security.UsuarioLogado;
import br.com.tcc.aqualeve.service.AvaliacaoService;
import br.com.tcc.aqualeve.service.PrainhaService;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/avaliacoes")
public class AvaliacaoController {
    
     @Autowired
    private AvaliacaoService avaliacaoService;

     @Autowired
    private PrainhaService prainhaService;

    //Exibe o formulário para o cliente avaliar uma prainha
    @GetMapping("/nova")
    public String nova(@RequestParam("prainhaId") Integer prainhaId, Model model) {
        AvaliacaoForm form = new AvaliacaoForm();
        form.setPrainhaId(prainhaId.longValue());
        model.addAttribute("avaliacaoForm", form);
        model.addAttribute("prainha", buscarPrainha(prainhaId));
        return "avaliacao/formulario";
    }

    //Método para salvar uma avaliação (o cliente vem do token)
    @PostMapping("/salvar")
    public String salvar(@Valid @ModelAttribute("avaliacaoForm") AvaliacaoForm form,
                         BindingResult result,
                         @AuthenticationPrincipal Jwt jwt,
                         Model model,
                         RedirectAttributes flash) {
        if (result.hasErrors()) {
            if (form.getPrainhaId() == null) {
                throw new RecursoNaoEncontradoException("Prainha não encontrada.");
            }
            model.addAttribute("prainha", buscarPrainha(form.getPrainhaId().intValue()));
            return "avaliacao/formulario";
        }

        avaliacaoService.avaliar(form, UsuarioLogado.de(jwt).getId());
        flash.addFlashAttribute("sucesso", "Obrigado! Sua avaliação foi publicada.");
        return "redirect:/prainhas/" + form.getPrainhaId();
    }

    //Cliente exclui a própria avaliação; administrador exclui qualquer uma
    @PostMapping("/excluir/{id}")
    public String excluir(@PathVariable("id") Integer id,
                          @AuthenticationPrincipal Jwt jwt,
                          RedirectAttributes flash) {
        Integer idPrainha = avaliacaoService.excluir(id, UsuarioLogado.de(jwt));
        flash.addFlashAttribute("sucesso", "Avaliação excluída.");
        return "redirect:/prainhas/" + idPrainha;
    }

    private Prainha buscarPrainha(Integer id) {
        Prainha prainha = prainhaService.findById(id);
        if (prainha == null) {
            throw new RecursoNaoEncontradoException("Prainha não encontrada.");
        }
        return prainha;
    }
}
