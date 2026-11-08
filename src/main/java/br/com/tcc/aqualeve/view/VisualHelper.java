package br.com.tcc.aqualeve.view;

import java.text.Normalizer;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import br.com.tcc.aqualeve.entity.Prainha;
import br.com.tcc.aqualeve.entity.StatusReserva;

// Ajudante SÓ de interface: escolhe fotos, ícones e textos exibidos nas telas.
// Nas telas é usado assim: ${@visual.foto(p)}, ${@visual.rotuloStatus(r.status)} ...
// Não altera nenhum dado do banco.
@Component("visual")
public class VisualHelper {

    // Fotos ilustrativas que já vêm no projeto (static/imagens)
    private static final String[] FOTOS_ILUSTRATIVAS = {
            "/imagens/prainha-quiosques.jpg",
            "/imagens/prainha-palmeiras.jpg",
            "/imagens/prainha-hero.jpg"
    };

    public String foto(Prainha p) {
        return fotoReal(p) ? "/prainhas/imagem/" + p.getIdPrainha() : fotoReserva(p);
    }
    public String fotoReserva(Prainha p) {
        int indice = p == null || p.getIdPrainha() == null ? 0 : Math.floorMod(p.getIdPrainha(), FOTOS_ILUSTRATIVAS.length);
        return FOTOS_ILUSTRATIVAS[indice];
    }
    public boolean fotoReal(Prainha p) { return p != null && p.isTemImagem(); }

    // "Quiosques, Banheiros, Estacionamento" -> lista para mostrar com ícones
    public List<String> servicos(Prainha p) {
        List<String> lista = new ArrayList<>();
        if (p == null || p.getServicosPrainha() == null) {
            return lista;
        }
        for (String s : p.getServicosPrainha().split("[,;]")) {
            String limpo = s.trim();
            if (!limpo.isEmpty()) {
                lista.add(Character.toUpperCase(limpo.charAt(0)) + limpo.substring(1));
            }
        }
        return lista;
    }

    // Nome do ícone (fragmentos/icones.html) para cada serviço
    public String iconeServico(String servico) {
        String s = semAcento(servico == null ? "" : servico.toLowerCase());
        if (s.contains("quiosque") || s.contains("chale")) return "house";
        if (s.contains("camping") || s.contains("barraca")) return "tent";
        if (s.contains("pesca")) return "fish";
        if (s.contains("estacion")) return "car";
        if (s.contains("churrasq")) return "flame";
        if (s.contains("banheir") || s.contains("ducha") || s.contains("vestiar")) return "bath";
        if (s.contains("restaur") || s.contains("lanch") || s.contains("bar") || s.contains("aliment")) return "utensils";
        if (s.contains("guarda") || s.contains("salva") || s.contains("seguran")) return "shield-check";
        return "waves";
    }

    // QUIOSQUE -> Quiosque, CHALE -> Chalé
    public String rotuloTipo(String tipo) {
        if (tipo == null) return "Espaço";
        switch (tipo.toUpperCase()) {
            case "QUIOSQUE": return "Quiosque";
            case "CHALE": return "Chalé";
            case "CAMPING": return "Camping";
            default: return tipo;
        }
    }

    public String iconeTipo(String tipo) {
        return "CAMPING".equalsIgnoreCase(tipo) ? "tent" : "house";
    }

    public String descricaoTipo(String tipo) {
        if (tipo == null) return "";
        switch (tipo.toUpperCase()) {
            case "QUIOSQUE": return "Sombra e mesa para reunir a turma";
            case "CHALE": return "Mais conforto para o dia todo";
            case "CAMPING": return "Área para barracas perto da água";
            default: return "";
        }
    }

    public String rotuloStatus(StatusReserva status) {
        if (status == null) return "";
        switch (status) {
            case CONFIRMADA: return "Confirmada";
            case EM_USO: return "No local";
            case FINALIZADA: return "Concluída";
            case CANCELADA: return "Cancelada";
            default: return status.name();
        }
    }

    // Datas em português, independente do idioma do navegador
    private static final String[] MESES = {"janeiro", "fevereiro", "março", "abril", "maio", "junho",
            "julho", "agosto", "setembro", "outubro", "novembro", "dezembro"};

    public String mesCurto(LocalDate data) {
        return data == null ? "" : MESES[data.getMonthValue() - 1].substring(0, 3);
    }

    public String dataExtenso(LocalDate data) {
        return data == null ? "" : data.getDayOfMonth() + " de " + MESES[data.getMonthValue() - 1];
    }

    // Código curto da reserva, ex.: 7 -> AQ-0007
    public String codigo(Integer numero) {
        return numero == null ? "" : String.format("AQ-%04d", numero);
    }

    private static String semAcento(String texto) {
        return Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
    }
}
