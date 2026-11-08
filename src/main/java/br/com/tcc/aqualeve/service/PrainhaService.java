package br.com.tcc.aqualeve.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.tcc.aqualeve.entity.Prainha;
import br.com.tcc.aqualeve.repository.PrainhaRepository;

@Service
public class PrainhaService {
    @Autowired
    private PrainhaRepository prainhaRepository;

    @Autowired private ImagemService imagemService;
    @Autowired private br.com.tcc.aqualeve.repository.AdministradorRepository administradores;

    @org.springframework.transaction.annotation.Transactional
    public Prainha save(br.com.tcc.aqualeve.dto.PrainhaForm form) {
        Prainha p = form.getIdPrainha() == null ? new Prainha() : prainhaRepository.findById(form.getIdPrainha())
            .orElseThrow(() -> new br.com.tcc.aqualeve.exception.RecursoNaoEncontradoException("Prainha não encontrada."));
        if (form.getAdministradorId() != null) {
            p.setAdministrador(administradores.findById(form.getAdministradorId()).orElseThrow(() ->
                new br.com.tcc.aqualeve.exception.RegraNegocioException("Administrador responsável não encontrado.")));
        } else if (p.getIdPrainha() == null) {
            p.setAdministrador(administradores.findAll(org.springframework.data.domain.Sort.by("idAdministrador")).stream().findFirst().orElse(null));
        }
        p.setNomePrainha(form.getNomePrainha().trim());
        p.setCidadePrainha(form.getCidadePrainha().trim());
        p.setDescricaoPrainha(form.getDescricaoPrainha().trim());
        p.setServicosPrainha(form.getServicosPrainha().trim());
        if (form.isRemoverImagem()) { p.setImagem(null); p.setImagemTipo(null); }
        if (form.getImagem() != null && !form.getImagem().isEmpty()) {
            var foto = imagemService.validar(form.getImagem());
            p.setImagem(foto.dados()); p.setImagemTipo(foto.tipo());
        }
        return prainhaRepository.saveAndFlush(p);
    }

    //Método para listar todas as prainhas
    public List<Prainha> findAll() {
        return prainhaRepository.findAllByOrderByNomePrainhaAsc();
    }

    //Método para buscar prainhas pela cidade (vazio = todas)
    public List<Prainha> findByCidade(String cidade) {
        if (cidade == null || cidade.isBlank()) {
            return findAll();
        }
        return prainhaRepository.findByCidadePrainhaContainingIgnoreCaseOrderByNomePrainhaAsc(cidade.trim());
    }

    //Método para excluir uma prainha por ID
    public void deleteById(Integer id){
        prainhaRepository.deleteById(id);
    }

    //Método para encontrar uma prainha por ID
    public Prainha findById(Integer id){
        return prainhaRepository.findById(id).orElse(null);
    }

}
