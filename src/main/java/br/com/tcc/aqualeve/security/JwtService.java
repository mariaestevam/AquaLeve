package br.com.tcc.aqualeve.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class JwtService {

    private final br.com.tcc.aqualeve.repository.ClienteRepository clientes;
    private final JwtEncoder jwtEncoder;
    private final long expiracaoMinutos;

    public JwtService(JwtEncoder jwtEncoder, br.com.tcc.aqualeve.repository.ClienteRepository clientes,
                      @Value("${aqualeve.jwt.expiracao-minutos:30}") long expiracaoMinutos) {
        this.clientes = clientes;
        this.jwtEncoder = jwtEncoder;
        this.expiracaoMinutos = expiracaoMinutos;
    }

    // Gera o token assinado com: quem é (id, e-mail, nome) e o perfil (CLIENTE ou ADMINISTRADOR)
    public String gerarToken(Long id, String email, String nome, String perfil) {
        Instant agora = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("aqualeve")
                .issuedAt(agora)
                .expiresAt(agora.plus(expiracaoMinutos, ChronoUnit.MINUTES))
                .subject(email)
                .claim("id", id)
                .claim("nome", nome)
                .claim("perfil", perfil)
                .claim("versaoSenha", Perfil.CLIENTE.equals(perfil) ? clientes.findById(id.intValue())
                    .map(c -> c.getVersaoSenha() == null ? 0L : c.getVersaoSenha()).orElse(0L) : 0L)
                .build();

        JwsHeader cabecalho = JwsHeader.with(MacAlgorithm.HS256).build();

        return jwtEncoder.encode(JwtEncoderParameters.from(cabecalho, claims)).getTokenValue();
    }

    public long getExpiracaoMinutos() {
        return expiracaoMinutos;
    }
}
