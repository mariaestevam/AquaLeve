package br.com.tcc.aqualeve.controller;

import br.com.tcc.aqualeve.security.UsuarioLogado;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

// Deixa ${usuario} disponível em TODAS as telas (menu mostra "Entrar" ou "Olá, fulano")
@ControllerAdvice
public class AtributosGlobais {

    @ModelAttribute("usuario")
    public UsuarioLogado usuario(@AuthenticationPrincipal Jwt jwt) {
        return jwt == null ? null : UsuarioLogado.de(jwt);
    }
}
