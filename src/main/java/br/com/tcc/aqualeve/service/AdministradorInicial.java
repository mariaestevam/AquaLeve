package br.com.tcc.aqualeve.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

// Cria o primeiro administrador ao subir a aplicação (a partir das variáveis de ambiente)
@Component
public class AdministradorInicial implements ApplicationRunner {

    private final AdministradorService administradorService;
    private final String email;
    private final String senha;

    public AdministradorInicial(AdministradorService administradorService,
                                @Value("${aqualeve.admin.email:}") String email,
                                @Value("${aqualeve.admin.senha:}") String senha) {
        this.administradorService = administradorService;
        this.email = email;
        this.senha = senha;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (email.isBlank() || senha.isBlank()) {
            return;
        }
        administradorService.criarInicialSeNaoExistir(email, senha);
    }
}
