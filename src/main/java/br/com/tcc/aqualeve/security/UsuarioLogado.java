package br.com.tcc.aqualeve.security;

import org.springframework.security.oauth2.jwt.Jwt;

// Dados de quem está logado, lidos do TOKEN (nunca do formulário).
// Também fica disponível nas telas como ${usuario}.
public class UsuarioLogado {

    private final Long id;
    private final String nome;
    private final String perfil;

    public UsuarioLogado(Long id, String nome, String perfil) {
        this.id = id;
        this.nome = nome;
        this.perfil = perfil;
    }

    public static UsuarioLogado de(Jwt jwt) {
        Number id = jwt.getClaim("id");
        return new UsuarioLogado(id.longValue(), jwt.getClaimAsString("nome"), jwt.getClaimAsString("perfil"));
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getPrimeiroNome() {
        return nome == null ? "" : nome.split(" ")[0];
    }

    public String getPerfil() {
        return perfil;
    }

    public boolean isAdministrador() {
        return Perfil.ADMINISTRADOR.equals(perfil);
    }

    public boolean isCliente() {
        return Perfil.CLIENTE.equals(perfil);
    }
}
