package br.com.tcc.aqualeve.controller;

import br.com.tcc.aqualeve.dto.CadastroClienteForm;
import br.com.tcc.aqualeve.dto.LoginForm;
import br.com.tcc.aqualeve.dto.TokenResponse;
import br.com.tcc.aqualeve.exception.NaoAutenticadoException;
import br.com.tcc.aqualeve.exception.RegraNegocioException;
import br.com.tcc.aqualeve.security.JwtCookieFilter;
import br.com.tcc.aqualeve.security.JwtService;
import br.com.tcc.aqualeve.security.Perfil;
import br.com.tcc.aqualeve.service.AuthService;
import br.com.tcc.aqualeve.service.ClienteService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.Duration;

@Controller
public class AuthController {

    private final AuthService authService;
    private final ClienteService clienteService;
    private final JwtService jwtService;
    private final boolean cookieSeguro;
    private final CookieCsrfTokenRepository csrfTokens;

    public AuthController(AuthService authService,
                          ClienteService clienteService,
                          JwtService jwtService,
                          CookieCsrfTokenRepository csrfTokens,
                          @Value("${aqualeve.cookie.secure:false}") boolean cookieSeguro) {
        this.authService = authService;
        this.clienteService = clienteService;
        this.jwtService = jwtService;
        this.cookieSeguro = cookieSeguro;
        this.csrfTokens = csrfTokens;
    }

    @GetMapping("/login")
    public String telaLogin(Model model) {
        model.addAttribute("loginForm", new LoginForm());
        return "login";
    }

    // Valida e-mail/senha, gera o token e guarda no cookie HttpOnly (o JavaScript da página não consegue ler)
    @PostMapping("/login")
    public String entrar(@Valid @ModelAttribute("loginForm") LoginForm form,
                         BindingResult result,
                         HttpServletRequest request,
                         HttpServletResponse response,
                         Model model) {

        if (result.hasErrors()) {
            return "login";
        }

        try {
            TokenResponse token = authService.login(form);

            ResponseCookie cookie = ResponseCookie.from(JwtCookieFilter.NOME_COOKIE, token.token())
                    .httpOnly(true)
                    .secure(cookieSeguro)
                    .sameSite("Lax")
                    .path("/")
                    .maxAge(Duration.ofMinutes(jwtService.getExpiracaoMinutos()))
                    .build();

            response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
            // Renova o CSRF uma vez após o login; a página de destino cria o novo token.
            csrfTokens.saveToken(null, request, response);

            return Perfil.ADMINISTRADOR.equals(token.perfil()) ? "redirect:/admin" : "redirect:/";

        } catch (NaoAutenticadoException e) {
            model.addAttribute("erro", e.getMessage());
            return "login";
        }
    }

    @GetMapping("/cadastro")
    public String telaCadastro(Model model) {
        model.addAttribute("cadastroForm", new CadastroClienteForm());
        return "cadastro";
    }

    // Cadastro público: cria SOMENTE cliente
    @PostMapping("/cadastro")
    public String cadastrar(@Valid @ModelAttribute("cadastroForm") CadastroClienteForm form,
                            BindingResult result,
                            Model model,
                            RedirectAttributes flash) {

        if (result.hasErrors()) {
            return "cadastro";
        }

        try {
            clienteService.cadastrar(form);
            flash.addFlashAttribute("sucesso", "Cadastro realizado com sucesso! Entre com seu e-mail e senha.");
            return "redirect:/login";

        } catch (RegraNegocioException e) {
            model.addAttribute("erro", e.getMessage());
            return "cadastro";
        }
    }
}
