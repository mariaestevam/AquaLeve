package br.com.tcc.aqualeve.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.tcc.aqualeve.dto.DisponibilidadeResponse;
import br.com.tcc.aqualeve.dto.ReservaForm;
import br.com.tcc.aqualeve.entity.Cliente;
import br.com.tcc.aqualeve.entity.Prainha;
import br.com.tcc.aqualeve.entity.Reserva;
import br.com.tcc.aqualeve.entity.StatusReserva;
import br.com.tcc.aqualeve.exception.AcessoNegadoException;
import br.com.tcc.aqualeve.exception.RecursoNaoEncontradoException;
import br.com.tcc.aqualeve.exception.RegraNegocioException;
import br.com.tcc.aqualeve.repository.CalendarioRepository;
import br.com.tcc.aqualeve.repository.ClienteRepository;
import br.com.tcc.aqualeve.repository.PrainhaRepository;
import br.com.tcc.aqualeve.repository.ReservaRepository;
import br.com.tcc.aqualeve.security.UsuarioLogado;

@Service
public class ReservaService {

    // Tipos de espaço que podem ser reservados em uma prainha
    public static final List<String> TIPOS = List.of("QUIOSQUE", "CHALE", "CAMPING");

    // Status que ocupam o espaço (RN04): CONFIRMADA e EM_USO
    private static final List<StatusReserva> STATUS_ATIVOS = List.of(StatusReserva.CONFIRMADA, StatusReserva.EM_USO);

    private static final String OCUPADO = "OCUPADO";
    private static final String DISPONIVEL = "DISPONIVEL";

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private PrainhaRepository prainhaRepository;

    @Autowired
    private CalendarioRepository calendarioRepository;

    public Reserva save(Reserva reserva) {
        return reservaRepository.save(reserva);
    }

    @Transactional
    public Reserva reservar(ReservaForm form, Long idCliente) {
        return preencher(new Reserva(), form, idCliente.intValue());
    }

    @Transactional
    public Reserva cadastrarReserva(br.com.tcc.aqualeve.dto.ReservaAdministradorForm form) {
        return preencher(new Reserva(), form, form.getClienteId());
    }

    @Transactional
    public Reserva editarReserva(Integer numero, br.com.tcc.aqualeve.dto.ReservaAdministradorForm form) {
        Reserva reserva = buscar(numero);
        if (reserva.getStatus() != StatusReserva.CONFIRMADA)
            throw new RegraNegocioException("Somente reservas confirmadas podem ser editadas.");
        return preencher(reserva, form, form.getClienteId());
    }

    // Criação e edição compartilham as regras de calendário, conflito e dados do servidor.
    private Reserva preencher(Reserva reserva, ReservaForm form, Integer idCliente) {
        if (form.getPrainhaId() == null || idCliente == null || form.getDataHora() == null
            || form.getTipoReserva() == null || form.getCapacidadeReserva() == null || form.getCapacidadeReserva() < 1)
            throw new RegraNegocioException("Informe prainha, cliente, espaço, data e uma quantidade de pessoas maior que zero.");
        Prainha prainha = prainhaRepository.buscarParaReserva(form.getPrainhaId().intValue())
            .orElseThrow(() -> new RecursoNaoEncontradoException("Prainha não encontrada."));
        Cliente cliente = clienteRepository.findById(idCliente)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente não encontrado."));
        String tipo = form.getTipoReserva().trim().toUpperCase(java.util.Locale.ROOT);
        if (!TIPOS.contains(tipo)) throw new RegraNegocioException("Tipo de espaço inválido.");
        LocalDateTime dataHora = form.getDataHora();
        if (!dataHora.isAfter(LocalDateTime.now())) throw new RegraNegocioException("A data e o horário da reserva devem estar no futuro.");
        LocalDate dia = dataHora.toLocalDate();
        if (!calendarioRepository.existsByPrainhaIdPrainhaAndDataDisponivel(prainha.getIdPrainha(), dia))
            throw new RegraNegocioException("Esta data não está liberada para reservas nesta prainha.");
        long ocupadas = reservaRepository.contarAtivasExceto(prainha.getIdPrainha(), tipo,
            dia.atStartOfDay(), dia.plusDays(1).atStartOfDay(), STATUS_ATIVOS,
            reserva.getNumeroReserva() == null ? -1 : reserva.getNumeroReserva());
        if (ocupadas > 0) throw new RegraNegocioException("Este espaço já está reservado nessa data. Escolha outro espaço ou outra data.");
        if (reserva.getNumeroReserva() == null) {
            reserva.setValorReserva(0);
            reserva.setStatus(StatusReserva.CONFIRMADA);
            reserva.setDisponibilidadeReserva(OCUPADO);
        }
        reserva.setCliente(cliente); reserva.setPrainha(prainha); reserva.setTipoReserva(tipo);
        reserva.setCapacidadeReserva(form.getCapacidadeReserva()); reserva.setDataHora(dataHora);
        return reservaRepository.save(reserva);
    }

    //RF04: a data está liberada? quais espaços já estão reservados nela?
    public DisponibilidadeResponse consultarDisponibilidade(Integer idPrainha, LocalDate data) {
        boolean liberado = !data.isBefore(LocalDate.now()) && calendarioRepository.existsByPrainhaIdPrainhaAndDataDisponivel(idPrainha, data);
        List<String> ocupados = liberado
                ? reservaRepository.tiposOcupados(idPrainha, data.atStartOfDay(), data.plusDays(1).atStartOfDay(), STATUS_ATIVOS)
                : List.of();
        return new DisponibilidadeResponse(data, liberado, ocupados);
    }

    //RF09: o visitante chegou
    @Transactional
    public Reserva registrarEntrada(Integer numero) {
        Reserva reserva = buscar(numero);
        if (reserva.getStatus() != StatusReserva.CONFIRMADA) {
            throw new RegraNegocioException("Só é possível registrar a entrada de uma reserva confirmada.");
        }
        reserva.setStatus(StatusReserva.EM_USO);
        reserva.setEntradaEm(LocalDateTime.now());
        return reservaRepository.save(reserva);
    }

    //RF09: o visitante saiu (o espaço volta a ficar disponível)
    @Transactional
    public Reserva registrarSaida(Integer numero) {
        Reserva reserva = buscar(numero);
        if (reserva.getStatus() != StatusReserva.EM_USO) {
            throw new RegraNegocioException("Só é possível registrar a saída de uma reserva em uso.");
        }
        reserva.setStatus(StatusReserva.FINALIZADA);
        reserva.setSaidaEm(LocalDateTime.now());
        reserva.setDisponibilidadeReserva(DISPONIVEL);
        return reservaRepository.save(reserva);
    }

    //O cliente cancela a própria reserva; o administrador cancela qualquer uma (RN04: libera o espaço)
    @Transactional
    public Reserva cancelar(Integer numero, UsuarioLogado usuario) {
        Reserva reserva = buscar(numero);

        boolean dono = usuario.isCliente()
                && reserva.getCliente().getIdCliente().equals(usuario.getId().intValue());
        if (!usuario.isAdministrador() && !dono) {
            throw new AcessoNegadoException("Você não pode cancelar esta reserva.");
        }
        if (reserva.getStatus() != StatusReserva.CONFIRMADA) {
            throw new RegraNegocioException("Só é possível cancelar uma reserva confirmada.");
        }

        reserva.setStatus(StatusReserva.CANCELADA);
        reserva.setDisponibilidadeReserva(DISPONIVEL);
        return reservaRepository.save(reserva);
    }

    //Reservas de um cliente (mais recentes primeiro)
    public List<Reserva> findByCliente(Integer idCliente) {
        return reservaRepository.findByClienteIdClienteOrderByDataHoraDesc(idCliente);
    }

    //Método para listar todas as reservas
    public List<Reserva> findAll() {
        return reservaRepository.findAllByOrderByDataHoraDesc();
    }

    //Método para excluir uma reserva por ID
    public void deleteById(Integer id){
        reservaRepository.deleteById(id);
    }

    //Método para encontrar uma reserva por ID
    public Reserva findById(Integer id){
        return reservaRepository.findById(id).orElse(null);
    }

    private Reserva buscar(Integer numero) {
        return reservaRepository.buscarParaAtualizar(numero)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Reserva não encontrada."));
    }

}
