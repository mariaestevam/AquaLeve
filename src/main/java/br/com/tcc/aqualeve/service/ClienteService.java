package br.com.tcc.aqualeve.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import br.com.tcc.aqualeve.dto.CadastroClienteForm;
import br.com.tcc.aqualeve.dto.ContaForm;
import br.com.tcc.aqualeve.entity.Cliente;
import br.com.tcc.aqualeve.exception.RecursoNaoEncontradoException;
import br.com.tcc.aqualeve.exception.RegraNegocioException;
import br.com.tcc.aqualeve.repository.ClienteRepository;

@Service
public class ClienteService {
    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private VerificadorEmail verificadorEmail;

    //Método para o cadastro público: cria SOMENTE cliente, com a senha criptografada
    public Cliente cadastrar(CadastroClienteForm form) {
        if (!form.getSenha().equals(form.getConfirmarSenha())) {
            throw new RegraNegocioException("As senhas não conferem.");
        }

        String email = form.getEmail().trim().toLowerCase();
        verificadorEmail.garantirLivre(email);

        Cliente cliente = new Cliente();
        cliente.setNomeCliente(form.getNome().trim());
        cliente.setCpfCliente(form.getCpf());
        cliente.setEmailCliente(email);
        cliente.setTelefoneCliente(form.getTelefone());
        cliente.setSenhaCliente(passwordEncoder.encode(form.getSenha()));
        return clienteRepository.save(cliente);
    }

    //Método para atualizar nome e telefone (e-mail, CPF e senha não mudam por aqui)
    public Cliente atualizarConta(Integer id, ContaForm form) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente não encontrado."));
        cliente.setNomeCliente(form.getNome().trim());
        cliente.setTelefoneCliente(form.getTelefone().trim());
        return clienteRepository.save(cliente);
    }

    @org.springframework.transaction.annotation.Transactional
    public void redefinirSenha(Integer id, br.com.tcc.aqualeve.dto.RedefinirSenhaForm form) {
        Cliente cliente = clienteRepository.buscarParaAtualizar(id).orElseThrow(() -> new RecursoNaoEncontradoException("Cliente não encontrado."));
        if (form.getSenhaAtual() == null || form.getSenhaAtual().getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72
            || !passwordEncoder.matches(form.getSenhaAtual(), cliente.getSenhaCliente()))
            throw new RegraNegocioException("Senha atual incorreta.");
        if (!form.getNovaSenha().equals(form.getConfirmarSenha()))
            throw new RegraNegocioException("As senhas não conferem.");
        int tamanho = form.getNovaSenha().getBytes(java.nio.charset.StandardCharsets.UTF_8).length;
        if (form.getNovaSenha().length() < 8 || tamanho > 72)
            throw new RegraNegocioException("A nova senha deve ter ao menos 8 caracteres e no máximo 72 bytes em UTF-8.");
        if (passwordEncoder.matches(form.getNovaSenha(), cliente.getSenhaCliente()))
            throw new RegraNegocioException("Escolha uma senha diferente da atual.");
        cliente.setSenhaCliente(passwordEncoder.encode(form.getNovaSenha()));
        cliente.setVersaoSenha((cliente.getVersaoSenha() == null ? 0L : cliente.getVersaoSenha()) + 1);
        clienteRepository.save(cliente);
    }

    //Método para salvar um cliente
    public Cliente save(Cliente cliente) {
        return clienteRepository.save(cliente);
    }

    //Método para listar todos os clientes
    public List<Cliente> findAll() {
        return clienteRepository.findAll();
    }

    //Método para excluir um cliente po ID
    public void deleteById(Integer id){
        clienteRepository.deleteById(id);
    }

    //Método para encontrar um cliente por ID
    public Cliente findById(Integer id){
        return clienteRepository.findById(id).orElse(null);
    }

}
