package br.com.tcc.aqualeve.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.tcc.aqualeve.dto.EventoForm;
import br.com.tcc.aqualeve.entity.Calendario;
import br.com.tcc.aqualeve.entity.Eventos;
import br.com.tcc.aqualeve.exception.RecursoNaoEncontradoException;
import br.com.tcc.aqualeve.repository.CalendarioRepository;
import br.com.tcc.aqualeve.repository.EventosRepository;

@Service
public class EventosService {
    @Autowired
    private EventosRepository eventoRepository;

    @Autowired
    private CalendarioRepository calendarioRepository;

    //Método para salvar um Evento
    public Eventos save(Eventos evento) {
        return eventoRepository.save(evento);
    }

    //Método para cadastrar um Evento a partir do formulário (vinculado a uma data liberada do calendário)
    public Eventos cadastrar(EventoForm form) {
        Calendario calendario = calendarioRepository.findById(form.getCalendarioId().intValue())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Data do calendário não encontrada."));

        if (!form.getDataEvento().equals(calendario.getDataDisponivel()) || form.getDataEvento().isBefore(LocalDate.now())) {
            throw new br.com.tcc.aqualeve.exception.RegraNegocioException("A data do evento deve coincidir com a data liberada no calendário e não pode estar no passado.");
        }
        Eventos evento = new Eventos();
        evento.setCalendario(calendario);
        evento.setDataEvento(form.getDataEvento());
        evento.setDescricaoEvento(form.getDescricaoEvento().trim());
        return eventoRepository.save(evento);
    }

    //Método para listar todos os Eventos
    public List<Eventos> findAll() {
        return eventoRepository.findAllByOrderByDataEventoAsc();
    }

    //Próximos eventos de uma prainha
    public List<Eventos> proximosDaPrainha(Integer idPrainha) {
        return eventoRepository
                .findByCalendarioPrainhaIdPrainhaAndDataEventoGreaterThanEqualOrderByDataEventoAsc(idPrainha, LocalDate.now());
    }

    //Método para excluir um Evento po ID
    public void deleteById(Integer id){
        eventoRepository.deleteById(id);
    }

    //Método para encontrar um Evento por ID
    public Eventos findById(Integer id){
        return eventoRepository.findById(id).orElse(null);
    }

}
