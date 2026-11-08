package br.com.tcc.aqualeve.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

// Em TODA requisição: lê o cookie com o token, valida a assinatura e a validade
// e, se estiver tudo certo, marca o usuário como autenticado (com o perfil do token).
// Token ausente, adulterado ou expirado = usuário segue como visitante (anônimo).
public class JwtCookieFilter extends OncePerRequestFilter {

    public static final String NOME_COOKIE = "AQUALEVE_TOKEN";

    private final br.com.tcc.aqualeve.repository.ClienteRepository clientes;
    private final br.com.tcc.aqualeve.repository.AdministradorRepository administradores;
    private final JwtDecoder jwtDecoder;
    private final JwtAuthenticationConverter conversor;

    public JwtCookieFilter(JwtDecoder jwtDecoder, JwtAuthenticationConverter conversor,
                           br.com.tcc.aqualeve.repository.ClienteRepository clientes,
                           br.com.tcc.aqualeve.repository.AdministradorRepository administradores) {
        this.clientes = clientes;
        this.administradores = administradores;
        this.jwtDecoder = jwtDecoder;
        this.conversor = conversor;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        String token = lerToken(request);

        if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                Jwt jwt = jwtDecoder.decode(token);
                if (contaAtiva(jwt)) {
                    SecurityContextHolder.getContext().setAuthentication(conversor.convert(jwt));
                }
            } catch (JwtException e) {
                SecurityContextHolder.clearContext();
            }
        }

        chain.doFilter(request, response);
    }

    // Exclusão de conta revoga o acesso inclusive quando o JWT ainda não expirou.
    private boolean contaAtiva(Jwt jwt) {
        Object claim = jwt.getClaim("id");
        if (!(claim instanceof Number numero) || numero.longValue() < 1 || numero.longValue() > Integer.MAX_VALUE) return false;
        int id = numero.intValue();
        String perfil = jwt.getClaimAsString("perfil");
        if (Perfil.CLIENTE.equals(perfil)) {
            Object versao = jwt.getClaim("versaoSenha");
            long versaoToken = versao instanceof Number n ? n.longValue() : 0L;
            return clientes.findById(id).map(c -> c.getEmailCliente().equalsIgnoreCase(jwt.getSubject())
                && versaoToken == (c.getVersaoSenha() == null ? 0L : c.getVersaoSenha())).orElse(false);
        }
        if (Perfil.ADMINISTRADOR.equals(perfil)) {
            return administradores.findById(id).map(a -> a.getEmailAdministrador().equalsIgnoreCase(jwt.getSubject())).orElse(false);
        }
        return false;
    }

    private String lerToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (NOME_COOKIE.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
