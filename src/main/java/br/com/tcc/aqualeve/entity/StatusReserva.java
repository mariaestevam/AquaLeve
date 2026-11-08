package br.com.tcc.aqualeve.entity;

// Ciclo de vida da reserva (RN04: a disponibilidade acompanha este status)
public enum StatusReserva {
    CONFIRMADA,   // reservada, ocupa o espaço
    EM_USO,       // cliente entrou (RF09)
    FINALIZADA,   // cliente saiu (RF09)
    CANCELADA     // libera o espaço novamente
}
