package br.com.tcc.aqualeve.service;

import br.com.tcc.aqualeve.exception.RegraNegocioException;
import br.com.tcc.aqualeve.repository.AdministradorRepository;
import br.com.tcc.aqualeve.repository.ClienteRepository;
import org.springframework.stereotype.Component;

// O e-mail é o login: não pode repetir nem entre clientes e administradores
@Component
public class VerificadorEmail {

    private final ClienteRepository clienteRepository;
    private final AdministradorRepository administradorRepository;

    public VerificadorEmail(ClienteRepository clienteRepository,
                            AdministradorRepository administradorRepository) {
        this.clienteRepository = clienteRepository;
        this.administradorRepository = administradorRepository;
    }

    public void garantirLivre(String email) {
        if (clienteRepository.existsByEmailClienteIgnoreCase(email)
                || administradorRepository.existsByEmailAdministradorIgnoreCase(email)) {
            throw new RegraNegocioException("E-mail já cadastrado no sistema.");
        }
    }
}
