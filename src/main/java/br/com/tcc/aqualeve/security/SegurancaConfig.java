package br.com.tcc.aqualeve.security;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.authentication.session.NullAuthenticatedSessionStrategy;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Configuration
public class SegurancaConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Chave usada para ASSINAR e VALIDAR os tokens (HS256 exige 32+ bytes)
    @Bean
    public SecretKey chaveJwt(@Value("${aqualeve.jwt.secret}") String segredo) {
        byte[] bytes = segredo.getBytes(StandardCharsets.UTF_8);

        if (bytes.length < 32) {
            throw new IllegalStateException("AQUALEVE_JWT_SECRET deve ter no mínimo 32 caracteres.");
        }

        return new SecretKeySpec(bytes, "HmacSHA256");
    }

    @Bean
    public JwtEncoder jwtEncoder(SecretKey chaveJwt) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(chaveJwt));
    }

    @Bean
    public JwtDecoder jwtDecoder(SecretKey chaveJwt) {
        return NimbusJwtDecoder.withSecretKey(chaveJwt)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }

    // Transforma o campo "perfil" do token em ROLE_CLIENTE / ROLE_ADMINISTRADOR
    @Bean
    public JwtAuthenticationConverter conversorJwt() {
        JwtGrantedAuthoritiesConverter autoridades = new JwtGrantedAuthoritiesConverter();
        autoridades.setAuthoritiesClaimName("perfil");
        autoridades.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter conversor = new JwtAuthenticationConverter();
        conversor.setJwtGrantedAuthoritiesConverter(autoridades);
        return conversor;
    }

    @Bean
    public CookieCsrfTokenRepository csrfTokens() {
        return new CookieCsrfTokenRepository();
    }

    @Bean
    public SecurityFilterChain configurarSeguranca(HttpSecurity http, CookieCsrfTokenRepository csrfTokens,
                                                   JwtDecoder jwtDecoder,
                                                   JwtAuthenticationConverter conversorJwt,
                                                   br.com.tcc.aqualeve.repository.ClienteRepository clientes,
                                                   br.com.tcc.aqualeve.repository.AdministradorRepository administradores) throws Exception {

        // Não é @Bean de propósito: só deve rodar dentro da cadeia do Spring Security
        JwtCookieFilter filtroJwt = new JwtCookieFilter(jwtDecoder, conversorJwt, clientes, administradores);

        http
            // O token mora em cookie, então o CSRF fica LIGADO (o th:action das telas já envia o token CSRF)
            .csrf(csrf -> csrf.csrfTokenRepository(csrfTokens)
                // A rotação ocorre no login do controller, não em cada autenticação JWT.
                .sessionAuthenticationStrategy(new NullAuthenticatedSessionStrategy()))
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .addFilterBefore(filtroJwt, UsernamePasswordAuthenticationFilter.class)
            .authorizeHttpRequests(a -> a

                // ---------- Público ----------
                .requestMatchers("/", "/sobre", "/login", "/cadastro", "/acesso-negado", "/error", "/favicon.png",
                        "/css/**", "/js/**", "/imagens/**", "/favicon.ico").permitAll()
                .requestMatchers(HttpMethod.GET,
                        "/prainhas", "/prainhas/listar", "/prainhas/{id:[0-9]+}", "/prainhas/imagem/*", "/eventos", "/eventos/listar").permitAll()

                // ---------- Somente ADMINISTRADOR (RN03) ----------
                .requestMatchers("/prainhas/**", "/eventos/**", "/calendarios/**",
                        "/administradores/**", "/admin/**").hasRole(Perfil.ADMINISTRADOR)
                .requestMatchers(HttpMethod.GET, "/reservas/listar").hasRole(Perfil.ADMINISTRADOR)
                .requestMatchers(HttpMethod.POST, "/reservas/entrada/*", "/reservas/saida/*")
                        .hasRole(Perfil.ADMINISTRADOR)

                // ---------- Somente CLIENTE ----------
                .requestMatchers("/clientes/perfil", "/clientes/senha").hasRole(Perfil.CLIENTE)
                .requestMatchers(HttpMethod.GET, "/reservas/minhas", "/reservas/nova",
                        "/avaliacoes/nova").hasRole(Perfil.CLIENTE)
                .requestMatchers(HttpMethod.POST, "/reservas/salvar", "/avaliacoes/salvar")
                        .hasRole(Perfil.CLIENTE)

                // ---------- Os dois perfis (a posse é conferida no service) ----------
                .requestMatchers(HttpMethod.POST, "/reservas/cancelar/*", "/avaliacoes/excluir/*")
                        .hasAnyRole(Perfil.CLIENTE, Perfil.ADMINISTRADOR)

                // ---------- Gerenciar clientes = ADMINISTRADOR ----------
                .requestMatchers("/clientes/**").hasRole(Perfil.ADMINISTRADOR)

                // Qualquer rota não prevista exige pelo menos estar logado
                .anyRequest().authenticated()
            )
            .exceptionHandling(e -> e
                // Visitante tentando acessar área restrita: vai para o login
                .authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/login"))
                // Logado, mas sem permissão (ex.: cliente em área de admin): tela de acesso negado
                .accessDeniedHandler((request, response, excecao) ->
                        response.sendRedirect(request.getContextPath() + "/acesso-negado"))
            )
            .logout(logout -> logout
                .logoutUrl("/logout") // POST (o botão "Sair" das telas já envia o CSRF)
                .deleteCookies(JwtCookieFilter.NOME_COOKIE)
                .logoutSuccessUrl("/login?logout")
            );

        return http.build();
    }
}
