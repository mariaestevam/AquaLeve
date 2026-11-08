package br.com.tcc.aqualeve.service;

import br.com.tcc.aqualeve.dto.LoginForm;
import br.com.tcc.aqualeve.dto.TokenResponse;
import br.com.tcc.aqualeve.entity.Administrador;
import br.com.tcc.aqualeve.entity.Cliente;
import br.com.tcc.aqualeve.exception.NaoAutenticadoException;
import br.com.tcc.aqualeve.repository.AdministradorRepository;
import br.com.tcc.aqualeve.repository.ClienteRepository;
import br.com.tcc.aqualeve.security.JwtService;
import br.com.tcc.aqualeve.security.Perfil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class AuthService {

    private static final String MENSAGEM_LOGIN_INVALIDO = "Usuário ou senha incorretos.";

    private final ClienteRepository clienteRepository;
    private final AdministradorRepository administradorRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(ClienteRepository clienteRepository,
                       AdministradorRepository administradorRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.clienteRepository = clienteRepository;
        this.administradorRepository = administradorRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    // O perfil NÃO vem do formulário: é descoberto pelo cadastro do e-mail
    @Transactional(readOnly = true)
    public TokenResponse login(LoginForm dados) {
        String email = dados.getEmail().trim().toLowerCase();

        Optional<Cliente> cliente = clienteRepository.findByEmailClienteIgnoreCase(email);
        if (cliente.isPresent()
                && passwordEncoder.matches(dados.getSenha(), cliente.get().getSenhaCliente())) {

            Cliente c = cliente.get();
            String token = jwtService.gerarToken(c.getIdCliente().longValue(), c.getEmailCliente(),
                    c.getNomeCliente(), Perfil.CLIENTE);
            return new TokenResponse(token, Perfil.CLIENTE, c.getIdCliente().longValue());
        }

        Optional<Administrador> administrador = administradorRepository.findByEmailAdministradorIgnoreCase(email);
        if (administrador.isPresent()
                && passwordEncoder.matches(dados.getSenha(), administrador.get().getSenhaAdministrador())) {

            Administrador a = administrador.get();
            String token = jwtService.gerarToken(a.getIdAdministrador().longValue(), a.getEmailAdministrador(),
                    a.getNomeAdministrador(), Perfil.ADMINISTRADOR);
            return new TokenResponse(token, Perfil.ADMINISTRADOR, a.getIdAdministrador().longValue());
        }

        // Mesma mensagem para e-mail inexistente e senha errada (não revela quais e-mails existem)
        throw new NaoAutenticadoException(MENSAGEM_LOGIN_INVALIDO);
    }
}
