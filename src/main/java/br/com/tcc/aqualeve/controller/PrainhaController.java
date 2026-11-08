package br.com.tcc.aqualeve.controller;

import java.time.LocalDate;
import br.com.tcc.aqualeve.dto.PrainhaForm;
import br.com.tcc.aqualeve.exception.RegraNegocioException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.ResponseBody;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.format.annotation.DateTimeFormat;
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

import br.com.tcc.aqualeve.entity.Prainha;
import br.com.tcc.aqualeve.exception.RecursoNaoEncontradoException;
import br.com.tcc.aqualeve.service.AvaliacaoService;
import br.com.tcc.aqualeve.service.CalendarioService;
import br.com.tcc.aqualeve.service.EventosService;
import br.com.tcc.aqualeve.service.PrainhaService;
import br.com.tcc.aqualeve.service.ReservaService;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/prainhas")
public class PrainhaController {

     //injeção de depêndencia das services
     @Autowired private br.com.tcc.aqualeve.service.AdministradorService administradores;

     @Autowired
     private PrainhaService prainhaService;

     @Autowired
     private AvaliacaoService avaliacaoService;

     @Autowired
     private CalendarioService calendarioService;

     @Autowired
     private EventosService eventosService;

     @Autowired
     private ReservaService reservaService;
 
     //Método para listar as prainhas (com busca opcional por cidade) - tela pública
     @GetMapping({"", "/listar"})
     public String listar(@RequestParam(name = "cidade", required = false) String cidade, Model model) {
         model.addAttribute("prainhas", prainhaService.findByCidade(cidade));
         model.addAttribute("cidade", cidade);
         return "prainha/listar";
     }

     //Detalhes da prainha: informações, avaliações, datas liberadas, eventos e consulta de disponibilidade
     @GetMapping("/{id:[0-9]+}")
     public String detalhe(@PathVariable("id") Integer id,
                           @RequestParam(name = "data", required = false)
                           @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
                           Model model) {
         Prainha prainha = prainhaService.findById(id);
         if (prainha == null) {
             throw new RecursoNaoEncontradoException("Prainha não encontrada.");
         }

         model.addAttribute("tipos", br.com.tcc.aqualeve.service.ReservaService.TIPOS);
         model.addAttribute("prainha", prainha);
         model.addAttribute("media", avaliacaoService.mediaDaPrainha(id));
         model.addAttribute("avaliacoes", avaliacaoService.findByPrainha(id));
         model.addAttribute("datas", calendarioService.datasFuturas(id));
         model.addAttribute("eventos", eventosService.proximosDaPrainha(id));
         model.addAttribute("dataConsulta", data);
         if (data != null) {
             model.addAttribute("disponibilidade", reservaService.consultarDisponibilidade(id, data));
         }
         return "prainha/detalhe";
     }

     //Método para exibir o formulário de cadastro de prainha
     @GetMapping("/criar")
     public String criar(Model model, @org.springframework.security.core.annotation.AuthenticationPrincipal org.springframework.security.oauth2.jwt.Jwt jwt) {
         PrainhaForm form = new PrainhaForm();
         form.setAdministradorId(br.com.tcc.aqualeve.security.UsuarioLogado.de(jwt).getId().intValue());
         model.addAttribute("administradores", administradores.findAll());
         model.addAttribute("prainha", form);
         model.addAttribute("titulo", "Nova prainha");
         return "prainha/formulario";
     }

     //Método para exibir o fómulario de edição de prainha
     @GetMapping("/editar/{id}")
     public String editar(@PathVariable("id") Integer id, Model model) {
        Prainha prainha = prainhaService.findById(id);
        if (prainha == null) {
            throw new RecursoNaoEncontradoException("Prainha não encontrada.");
        }
         model.addAttribute("administradores", administradores.findAll());
         PrainhaForm form = new PrainhaForm();
         model.addAttribute("fotoAtual", prainha.isTemImagem());
         form.setAdministradorId(prainha.getAdministrador() == null ? null : prainha.getAdministrador().getIdAdministrador());
         form.setIdPrainha(prainha.getIdPrainha());
         form.setNomePrainha(prainha.getNomePrainha());
         form.setCidadePrainha(prainha.getCidadePrainha());
         form.setDescricaoPrainha(prainha.getDescricaoPrainha());
         form.setServicosPrainha(prainha.getServicosPrainha());
         model.addAttribute("prainha", form);
         model.addAttribute("titulo", "Editar prainha");
         return "prainha/formulario";
     }

     //Método para salvar uma prainha (nova ou editada)
     @PostMapping("/salvar")
     public String salvar(@Valid @ModelAttribute("prainha") PrainhaForm prainha,
                          BindingResult result,
                          Model model,
                          RedirectAttributes flash) {
        model.addAttribute("administradores", administradores.findAll());
        Prainha existente = prainha.getIdPrainha() == null ? null : prainhaService.findById(prainha.getIdPrainha());
        model.addAttribute("fotoAtual", existente != null && existente.isTemImagem());
        if (result.hasErrors()) {
            model.addAttribute("titulo", prainha.getIdPrainha() == null ? "Nova prainha" : "Editar prainha");
            return "prainha/formulario";
        }
        try {
            prainhaService.save(prainha);
        } catch (RegraNegocioException e) {
            model.addAttribute("erro", e.getMessage());
            model.addAttribute("titulo", prainha.getIdPrainha() == null ? "Nova prainha" : "Editar prainha");
            return "prainha/formulario";
        }
        flash.addFlashAttribute("sucesso", "Prainha salva com sucesso.");
         return "redirect:/prainhas";
     }
 
     @GetMapping("/imagem/{id}")
     @ResponseBody
     public ResponseEntity<byte[]> imagem(@PathVariable("id") Integer id) {
         Prainha p = prainhaService.findById(id);
         if (p == null || !p.isTemImagem()) return ResponseEntity.notFound().build();
         return ResponseEntity.ok().contentType(MediaType.parseMediaType(p.getImagemTipo()))
             .header("X-Content-Type-Options", "nosniff").body(p.getImagem());
     }

     //Método para excluir uma prainha
     @PostMapping("/excluir/{id}")
     public String excluir(@PathVariable("id") Integer id, RedirectAttributes flash) {
        try {
            prainhaService.deleteById(id);
            flash.addFlashAttribute("sucesso", "Prainha excluída.");
        } catch (DataIntegrityViolationException e) {
            flash.addFlashAttribute("erro", "Não é possível excluir: a prainha possui reservas, avaliações ou datas no calendário.");
        }
         return "redirect:/prainhas";
     }
     
 }
