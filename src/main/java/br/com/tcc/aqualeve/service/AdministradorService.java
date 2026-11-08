package br.com.tcc.aqualeve.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import br.com.tcc.aqualeve.dto.CadastroAdministradorForm;
import br.com.tcc.aqualeve.dto.ContaForm;
import br.com.tcc.aqualeve.entity.Administrador;
import br.com.tcc.aqualeve.exception.RecursoNaoEncontradoException;
import br.com.tcc.aqualeve.exception.RegraNegocioException;
import br.com.tcc.aqualeve.repository.AdministradorRepository;

@Service
public class AdministradorService {
    
    @Autowired
    private AdministradorRepository administradorRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private VerificadorEmail verificadorEmail;

    //Cria o primeiro administrador (chamado ao subir a aplicação) se o e-mail ainda não existir
    public void criarInicialSeNaoExistir(String email, String senha) {
        String emailNormalizado = email.trim().toLowerCase();
        if (administradorRepository.existsByEmailAdministradorIgnoreCase(emailNormalizado)) {
            return;
        }

        verificadorEmail.garantirLivre(emailNormalizado);
        Administrador administrador = new Administrador();
        administrador.setNomeAdministrador("Administrador");
        administrador.setTelefoneAdministrador("00000000000");
        administrador.setEmailAdministrador(emailNormalizado);
        administrador.setSenhaAdministrador(passwordEncoder.encode(senha));
        administradorRepository.save(administrador);
    }

    //RN03: só um administrador já logado cadastra outro administrador
    public Administrador cadastrar(CadastroAdministradorForm form) {
        if (!form.getSenha().equals(form.getConfirmarSenha())) {
            throw new RegraNegocioException("As senhas não conferem.");
        }

        String email = form.getEmail().trim().toLowerCase();
        verificadorEmail.garantirLivre(email);

        Administrador administrador = new Administrador();
        administrador.setNomeAdministrador(form.getNome().trim());
        administrador.setTelefoneAdministrador(form.getTelefone().trim());
        administrador.setEmailAdministrador(email);
        administrador.setSenhaAdministrador(passwordEncoder.encode(form.getSenha()));
        return administradorRepository.save(administrador);
    }

    //Atualiza nome e telefone (o e-mail é o login e não muda)
    public Administrador atualizarConta(Integer id, ContaForm form) {
        Administrador administrador = administradorRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Administrador não encontrado."));
        administrador.setNomeAdministrador(form.getNome().trim());
        administrador.setTelefoneAdministrador(form.getTelefone().trim());
        return administradorRepository.save(administrador);
    }

    //Um administrador não pode excluir a própria conta
    public void excluir(Integer id, Long idLogado) {
        if (id.equals(idLogado.intValue())) {
            throw new RegraNegocioException("Você não pode excluir a sua própria conta.");
        }
        administradorRepository.deleteById(id);
    }

    public Administrador save(Administrador administrador) {
        return administradorRepository.save(administrador);
    }

    public List<Administrador>findAll(){
        return administradorRepository.findAll();
    }

    public void deleteById(Integer id){
        administradorRepository.deleteById(id);
    }

    public Administrador findById(Integer id){
        return administradorRepository.findById(id).orElse(null);
    }

}
