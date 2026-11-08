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

import br.com.tcc.aqualeve.dto.ContaForm;
import br.com.tcc.aqualeve.entity.Cliente;
import br.com.tcc.aqualeve.exception.RecursoNaoEncontradoException;
import br.com.tcc.aqualeve.security.UsuarioLogado;
import br.com.tcc.aqualeve.service.ClienteService;
import jakarta.validation.Valid;


@Controller
@RequestMapping("/clientes")
public class ClienteController {
    
    //injeção de depêndencia da service de clientes
    @Autowired
    private ClienteService clienteService;

    //Método para listar todos os clientes (administrador)
    @GetMapping({"", "/listar"})
    public String listar(Model model) {
        model.addAttribute("clientes", clienteService.findAll());
        return "cliente/listar";
    }

    //Método para exibir o fómulario de edição de cliente (administrador)
    @GetMapping("/editar/{id}")
    public String editar(@PathVariable("id") Integer id, Model model) {
        Cliente cliente = buscar(id);
        preencherConta(model, cliente, "Editar cliente", "/clientes/editar/" + id, "/clientes");
        return "conta/formulario";
    }

    @PostMapping("/editar/{id}")
    public String salvarEdicao(@PathVariable("id") Integer id,
                               @Valid @ModelAttribute("contaForm") ContaForm form,
                               BindingResult result,
                               Model model,
                               RedirectAttributes flash) {
        if (result.hasErrors()) {
            Cliente cliente = buscar(id);
            complementarConta(model, cliente, "Editar cliente", "/clientes/editar/" + id, "/clientes");
            return "conta/formulario";
        }
        clienteService.atualizarConta(id, form);
        flash.addFlashAttribute("sucesso", "Cliente atualizado com sucesso.");
        return "redirect:/clientes";
    }

    //Método para excluir um cliente (administrador)
    @PostMapping("/excluir/{id}")
    public String excluir(@PathVariable("id") Integer id, RedirectAttributes flash) {
        try {
            clienteService.deleteById(id);
            flash.addFlashAttribute("sucesso", "Cliente excluído.");
        } catch (DataIntegrityViolationException e) {
            flash.addFlashAttribute("erro", "Não é possível excluir: o cliente possui reservas ou avaliações.");
        }
        return "redirect:/clientes";
    }

    //Meu perfil (cliente logado): o id vem do token, nunca da tela
    @GetMapping("/perfil")
    public String perfil(@AuthenticationPrincipal Jwt jwt, Model model) {
        Integer id = UsuarioLogado.de(jwt).getId().intValue();
        Cliente cliente = buscar(id);
        preencherConta(model, cliente, "Meu perfil", "/clientes/perfil", "/");
        return "conta/formulario";
    }

    @PostMapping("/perfil")
    public String salvarPerfil(@AuthenticationPrincipal Jwt jwt,
                               @Valid @ModelAttribute("contaForm") ContaForm form,
                               BindingResult result,
                               Model model,
                               RedirectAttributes flash) {
        Integer id = UsuarioLogado.de(jwt).getId().intValue();
        if (result.hasErrors()) {
            Cliente cliente = buscar(id);
            complementarConta(model, cliente, "Meu perfil", "/clientes/perfil", "/");
            return "conta/formulario";
        }
        clienteService.atualizarConta(id, form);
        flash.addFlashAttribute("sucesso", "Dados atualizados com sucesso.");
        return "redirect:/clientes/perfil";
    }

    @GetMapping("/senha")
    public String senha(Model model) {
        model.addAttribute("senhaForm", new br.com.tcc.aqualeve.dto.RedefinirSenhaForm());
        return "conta/senha";
    }
    @PostMapping("/senha")
    public String redefinirSenha(@AuthenticationPrincipal Jwt jwt,
            @Valid @ModelAttribute("senhaForm") br.com.tcc.aqualeve.dto.RedefinirSenhaForm form,
            BindingResult result, Model model, RedirectAttributes flash, jakarta.servlet.http.HttpServletResponse response) {
        if (result.hasErrors()) return "conta/senha";
        try { clienteService.redefinirSenha(UsuarioLogado.de(jwt).getId().intValue(), form); }
        catch (br.com.tcc.aqualeve.exception.RegraNegocioException e) {
            model.addAttribute("erro", e.getMessage()); return "conta/senha";
        }
        response.addHeader("Set-Cookie", org.springframework.http.ResponseCookie.from(br.com.tcc.aqualeve.security.JwtCookieFilter.NOME_COOKIE, "")
            .httpOnly(true).path("/").maxAge(0).sameSite("Lax").build().toString());
        flash.addFlashAttribute("sucesso", "Senha alterada. Entre novamente com a nova senha.");
        return "redirect:/login";
    }

    private Cliente buscar(Integer id) {
        Cliente cliente = clienteService.findById(id);
        if (cliente == null) {
            throw new RecursoNaoEncontradoException("Cliente não encontrado.");
        }
        return cliente;
    }

    private void preencherConta(Model model, Cliente cliente, String titulo, String acao, String voltar) {
        ContaForm form = new ContaForm();
        form.setNome(cliente.getNomeCliente());
        form.setTelefone(cliente.getTelefoneCliente());
        model.addAttribute("contaForm", form);
        complementarConta(model, cliente, titulo, acao, voltar);
    }

    private void complementarConta(Model model, Cliente cliente, String titulo, String acao, String voltar) {
        model.addAttribute("emailExibido", cliente.getEmailCliente());
        model.addAttribute("titulo", titulo);
        model.addAttribute("acao", acao);
        model.addAttribute("voltar", voltar);
    }
    
}
