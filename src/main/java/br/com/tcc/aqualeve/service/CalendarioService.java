package br.com.tcc.aqualeve.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.tcc.aqualeve.dto.CalendarioForm;
import br.com.tcc.aqualeve.entity.Calendario;
import br.com.tcc.aqualeve.entity.Prainha;
import br.com.tcc.aqualeve.exception.RecursoNaoEncontradoException;
import br.com.tcc.aqualeve.exception.RegraNegocioException;
import br.com.tcc.aqualeve.repository.CalendarioRepository;
import br.com.tcc.aqualeve.repository.PrainhaRepository;

@Service
public class CalendarioService {
    @Autowired
    private CalendarioRepository calendarioRepository;

    @Autowired
    private br.com.tcc.aqualeve.repository.ReservaRepository reservaRepository;

    @Autowired
    private PrainhaRepository prainhaRepository;

    //Método para salvar um Calendario
    public Calendario save(Calendario calendario) {
        return calendarioRepository.save(calendario);
    }

    //Método para liberar uma data para reservas em uma prainha
    public Calendario liberar(CalendarioForm form) {
        Prainha prainha = prainhaRepository.findById(form.getPrainhaId().intValue())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Prainha não encontrada."));

        if (form.getDataDisponivel().isBefore(LocalDate.now())) {
            throw new RegraNegocioException("Não é possível liberar uma data que já passou.");
        }
        if (calendarioRepository.existsByPrainhaIdPrainhaAndDataDisponivel(prainha.getIdPrainha(), form.getDataDisponivel())) {
            throw new RegraNegocioException("Esta data já está liberada para a prainha.");
        }

        Calendario calendario = new Calendario();
        calendario.setPrainha(prainha);
        calendario.setDataDisponivel(form.getDataDisponivel());
        return calendarioRepository.save(calendario);
    }

    //Método para listar todos os Calendarios
    public List<Calendario> findAll() {
        return calendarioRepository.findAllByOrderByDataDisponivelAsc();
    }

    //Datas liberadas de uma prainha, de hoje em diante
    public List<Calendario> datasFuturas(Integer idPrainha) {
        return calendarioRepository
                .findByPrainhaIdPrainhaAndDataDisponivelGreaterThanEqualOrderByDataDisponivelAsc(idPrainha, LocalDate.now());
    }

    // Usa o mesmo bloqueio da reserva para não fechar uma data durante o agendamento.
    @org.springframework.transaction.annotation.Transactional
    public void deleteById(Integer id) {
        Calendario c = calendarioRepository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Data não encontrada."));
        prainhaRepository.buscarParaReserva(c.getPrainha().getIdPrainha());
        var dia = c.getDataDisponivel();
        for (String tipo : ReservaService.TIPOS) {
            if (reservaRepository.contarAtivas(c.getPrainha().getIdPrainha(), tipo, dia.atStartOfDay(), dia.plusDays(1).atStartOfDay(),
                    java.util.List.of(br.com.tcc.aqualeve.entity.StatusReserva.CONFIRMADA, br.com.tcc.aqualeve.entity.StatusReserva.EM_USO)) > 0)
                throw new RegraNegocioException("Cancele ou finalize as reservas desta data antes de removê-la.");
        }
        calendarioRepository.delete(c);
        calendarioRepository.flush();
    }

    //Método para encontrar um Calendario por ID
    public Calendario findById(Integer id){
        return calendarioRepository.findById(id).orElse(null);
    }

}
