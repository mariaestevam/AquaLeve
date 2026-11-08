package br.com.tcc.aqualeve.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.tcc.aqualeve.dto.AvaliacaoForm;
import br.com.tcc.aqualeve.entity.Avaliacao;
import br.com.tcc.aqualeve.entity.Cliente;
import br.com.tcc.aqualeve.entity.Prainha;
import br.com.tcc.aqualeve.exception.AcessoNegadoException;
import br.com.tcc.aqualeve.exception.RecursoNaoEncontradoException;
import br.com.tcc.aqualeve.repository.AvaliacaoRepository;
import br.com.tcc.aqualeve.repository.ClienteRepository;
import br.com.tcc.aqualeve.repository.PrainhaRepository;
import br.com.tcc.aqualeve.security.UsuarioLogado;

@Service
public class AvaliacaoService {
    
    @Autowired
    private AvaliacaoRepository avaliacaoRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private PrainhaRepository prainhaRepository;

    public Avaliacao save(Avaliacao avaliacao) {
        return avaliacaoRepository.save(avaliacao);
    }

    //RF06: o cliente (vem do token) avalia uma prainha com nota e comentário
    public Avaliacao avaliar(AvaliacaoForm form, Long idCliente) {
        Cliente cliente = clienteRepository.findById(idCliente.intValue())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente não encontrado."));
        Prainha prainha = prainhaRepository.findById(form.getPrainhaId().intValue())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Prainha não encontrada."));

        Avaliacao avaliacao = new Avaliacao();
        avaliacao.setCliente(cliente);
        avaliacao.setPrainha(prainha);
        avaliacao.setNotaAvaliacao(form.getNota());
        avaliacao.setComentarioAvaliacao(form.getComentario() == null ? null : form.getComentario().trim());
        avaliacao.setDataAvaliacao(LocalDate.now());
        return avaliacaoRepository.save(avaliacao);
    }

    //Avaliações de uma prainha (mais recentes primeiro)
    public List<Avaliacao> findByPrainha(Integer idPrainha) {
        return avaliacaoRepository.findByPrainhaIdPrainhaOrderByDataAvaliacaoDescIdAvaliacaoDesc(idPrainha);
    }

    //Média das notas (null quando ainda não há avaliações)
    public Double mediaDaPrainha(Integer idPrainha) {
        return avaliacaoRepository.mediaPorPrainha(idPrainha);
    }

    //Só o administrador ou o próprio autor exclui; devolve o id da prainha para voltar à tela dela
    public Integer excluir(Integer id, UsuarioLogado usuario) {
        Avaliacao avaliacao = avaliacaoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Avaliação não encontrada."));

        boolean autor = usuario.isCliente()
                && avaliacao.getCliente().getIdCliente().equals(usuario.getId().intValue());
        if (!usuario.isAdministrador() && !autor) {
            throw new AcessoNegadoException("Você não pode excluir esta avaliação.");
        }

        Integer idPrainha = avaliacao.getPrainha().getIdPrainha();
        avaliacaoRepository.delete(avaliacao);
        return idPrainha;
    }

    public List<Avaliacao>findAll(){
        return avaliacaoRepository.findAll();
    }

    public void deleteById(Integer id){
        avaliacaoRepository.deleteById(id);
    }

    public Avaliacao findById(Integer id){
        return avaliacaoRepository.findById(id).orElse(null);
    }

}
