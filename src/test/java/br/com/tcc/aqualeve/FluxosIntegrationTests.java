package br.com.tcc.aqualeve;

import br.com.tcc.aqualeve.dto.*;
import br.com.tcc.aqualeve.entity.*;
import br.com.tcc.aqualeve.repository.*;
import br.com.tcc.aqualeve.service.*;
import br.com.tcc.aqualeve.security.UsuarioLogado;
import br.com.tcc.aqualeve.exception.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class FluxosIntegrationTests extends ConfiguracaoDeTeste {
    @Autowired MockMvc mvc;
    @Autowired AdministradorRepository administradorRepository;
    @Autowired AdministradorService administradores;
    @Autowired AuthService autenticacao;
    @Autowired org.springframework.security.oauth2.jwt.JwtDecoder decoder;
    @Autowired org.springframework.security.crypto.password.PasswordEncoder senhas;
    @Autowired br.com.tcc.aqualeve.security.JwtService tokens;
    @Autowired PrainhaService prainhas;
    @Autowired PrainhaRepository prainhaRepository;
    @Autowired ClienteService clientes;
    @Autowired ReservaService reservas;
    @Autowired ReservaRepository reservaRepository;
    @Autowired CalendarioService calendarios;
    @Autowired CalendarioRepository calendarioRepository;
    @Autowired EventosRepository eventosRepository;
    @Autowired EventosService eventos;
    @Autowired AvaliacaoRepository avaliacaoRepository;
    @Autowired AvaliacaoService avaliacoes;
    @Autowired ClienteRepository clienteRepository;
    Prainha p;
    Cliente c;

    @BeforeEach void dados() {
        avaliacaoRepository.deleteAll(); eventosRepository.deleteAll(); reservaRepository.deleteAll();
        calendarioRepository.deleteAll(); prainhaRepository.deleteAll(); clienteRepository.deleteAll();
        for (Administrador admin : administradorRepository.findAll()) {
            if (!admin.getEmailAdministrador().equals("admin@aqualeve.test")) administradorRepository.deleteById(admin.getIdAdministrador());
        }
        p = prainhas.save(form());
        CadastroClienteForm f = new CadastroClienteForm(); f.setNome("Cliente Teste"); f.setCpf(identificadorSimulado());
        f.setEmail("cliente@aqualeve.test"); f.setTelefone(identificadorSimulado()); f.setSenha(SENHA_CENARIO_5); f.setConfirmarSenha(SENHA_CENARIO_5);
        c = clientes.cadastrar(f);
    }
    org.springframework.test.web.servlet.request.RequestPostProcessor perfil(String perfil) {
        return tokenCookie(perfil.equals("ADMINISTRADOR")
            ? tokens.gerarToken(administradorRepository.findByEmailAdministradorIgnoreCase("admin@aqualeve.test").orElseThrow().getIdAdministrador().longValue(), "admin@aqualeve.test", "Administrador", perfil)
            : tokens.gerarToken(c.getIdCliente().longValue(), c.getEmailCliente(), "Teste", perfil));
    }
    org.springframework.test.web.servlet.request.RequestPostProcessor tokenCookie(String token) {
        return request -> {request.setCookies(new jakarta.servlet.http.Cookie("AQUALEVE_TOKEN", token));return request;};
    }
    PrainhaForm form() {
        PrainhaForm f = new PrainhaForm(); f.setNomePrainha("Prainha Teste"); f.setCidadePrainha("Mira Estrela");
        f.setDescricaoPrainha("Descrição da prainha"); f.setServicosPrainha("Quiosques e camping"); return f;
    }
    MockMultipartFile foto() throws Exception {
        ByteArrayOutputStream s = new ByteArrayOutputStream(); ImageIO.write(new BufferedImage(12,8,BufferedImage.TYPE_INT_RGB),"png",s);
        return new MockMultipartFile("imagem","foto.png","image/png",s.toByteArray());
    }
    ReservaForm pedido() {
        ReservaForm f = new ReservaForm(); f.setPrainhaId(p.getIdPrainha().longValue()); f.setTipoReserva("QUIOSQUE");
        f.setCapacidadeReserva(4); f.setDataHora(LocalDate.now().plusDays(3).atTime(10,0)); return f;
    }
    Calendario liberar() {
        CalendarioForm f = new CalendarioForm(); f.setPrainhaId(p.getIdPrainha().longValue()); f.setDataDisponivel(pedido().getDataHora().toLocalDate());
        return calendarios.liberar(f);
    }

    @Test void salvarConsultarEditarFotoERemover() throws Exception {
        mvc.perform(multipart("/prainhas/salvar").file(foto()).with(perfil("ADMINISTRADOR")).with(csrf())
            .param("nomePrainha","Prainha com foto").param("cidadePrainha","Ilha Solteira")
            .param("descricaoPrainha","Descrição salva").param("servicosPrainha","Banheiros"))
            .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/prainhas"));
        Prainha salva = prainhas.findByCidade("ilha").get(0);
        assertThat(salva.getDescricaoPrainha()).isEqualTo("Descrição salva"); assertThat(salva.isTemImagem()).isTrue();
        mvc.perform(get("/prainhas/imagem/"+salva.getIdPrainha())).andExpect(status().isOk()).andExpect(content().contentType("image/png"));
        mvc.perform(get("/prainhas/"+salva.getIdPrainha())).andExpect(status().isOk()).andExpect(content().string(org.hamcrest.Matchers.containsString("Descrição salva")));
        PrainhaForm f = form(); f.setIdPrainha(salva.getIdPrainha()); f.setNomePrainha("Nome editado");
        byte[] antes = salva.getImagem(); prainhas.save(f);
        assertThat(prainhas.findById(salva.getIdPrainha()).getImagem()).isEqualTo(antes);
        f.setImagem(foto()); prainhas.save(f);
        f.setImagem(null); f.setRemoverImagem(true); prainhas.save(f);
        mvc.perform(get("/prainhas/imagem/"+salva.getIdPrainha())).andExpect(status().isNotFound());
    }
    @Test void formulariosInvalidosNaoPersistem() throws Exception {
        mvc.perform(multipart("/prainhas/salvar").with(perfil("ADMINISTRADOR")).with(csrf())
            .param("nomePrainha","").param("cidadePrainha","Cidade").param("descricaoPrainha","D").param("servicosPrainha","S"))
            .andExpect(status().isOk()).andExpect(model().attributeHasFieldErrors("prainha","nomePrainha"));
        mvc.perform(multipart("/prainhas/salvar").file(new MockMultipartFile("imagem","x.png","image/png","nao e imagem".getBytes()))
            .with(perfil("ADMINISTRADOR")).with(csrf()).param("nomePrainha","Teste").param("cidadePrainha","Cidade")
            .param("descricaoPrainha","D").param("servicosPrainha","S"))
            .andExpect(status().isOk()).andExpect(model().attributeExists("erro"));
        assertThat(prainhas.findAll()).hasSize(1);
    }
    @Test void permissoesECsrf() throws Exception {
        mvc.perform(get("/prainhas/listar")).andExpect(status().isOk());
        mvc.perform(get("/prainhas/criar")).andExpect(status().is3xxRedirection());
        mvc.perform(post("/prainhas/salvar").with(perfil("CLIENTE")).with(csrf())).andExpect(redirectedUrl("/acesso-negado"));
        mvc.perform(post("/prainhas/salvar").with(perfil("ADMINISTRADOR"))).andExpect(redirectedUrl("/acesso-negado"));
        mvc.perform(get("/admin/relatorios").with(perfil("CLIENTE"))).andExpect(redirectedUrl("/acesso-negado"));
    }
    @Test void paginasAdministrativasRenderizam() throws Exception {
        for (String url : List.of("/admin","/prainhas/criar","/prainhas/editar/"+p.getIdPrainha(),"/calendarios","/eventos","/clientes","/clientes/editar/"+c.getIdCliente(),"/administradores","/administradores/novo","/reservas/listar","/admin/relatorios")) {
            mvc.perform(get(url).with(perfil("ADMINISTRADOR"))).andExpect(status().isOk());
        }
    }
    @Test void reservaCalendarioConflitoEntradaSaidaEAvaliacao() throws Exception {
        assertThatThrownBy(() -> reservas.reservar(pedido(),c.getIdCliente().longValue())).isInstanceOf(RegraNegocioException.class);
        liberar(); assertThatThrownBy(this::liberar).isInstanceOf(RegraNegocioException.class);
        Reserva r = reservas.reservar(pedido(),c.getIdCliente().longValue());
        assertThat(reservas.consultarDisponibilidade(p.getIdPrainha(),pedido().getDataHora().toLocalDate()).getTiposOcupados()).contains("QUIOSQUE");
        assertThatThrownBy(() -> reservas.reservar(pedido(),c.getIdCliente().longValue())).isInstanceOf(RegraNegocioException.class);
        assertThatThrownBy(() -> reservas.cancelar(r.getNumeroReserva(),new UsuarioLogado(999L,"Outro","CLIENTE"))).isInstanceOf(AcessoNegadoException.class);
        reservas.registrarEntrada(r.getNumeroReserva()); reservas.registrarSaida(r.getNumeroReserva());
        assertThat(reservas.consultarDisponibilidade(p.getIdPrainha(),pedido().getDataHora().toLocalDate()).getTiposOcupados()).isEmpty();
        Reserva outra = reservas.reservar(pedido(),c.getIdCliente().longValue());
        reservas.cancelar(outra.getNumeroReserva(),new UsuarioLogado(c.getIdCliente().longValue(),"Cliente","CLIENTE"));
        AvaliacaoForm a = new AvaliacaoForm(); a.setPrainhaId(p.getIdPrainha().longValue());a.setNota(5);a.setComentario("Muito bom");
        avaliacoes.avaliar(a,c.getIdCliente().longValue());
        mvc.perform(get("/prainhas/"+p.getIdPrainha())).andExpect(status().isOk()).andExpect(content().string(org.hamcrest.Matchers.containsString("Muito bom")));
        mvc.perform(get("/admin/relatorios/csv").with(perfil("ADMINISTRADOR"))).andExpect(status().isOk())
            .andExpect(content().string(org.hamcrest.Matchers.containsString("FINALIZADA")));
    }
    @Test void reservasConcorrentesNaoDuplicam() throws Exception {
        liberar(); ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch inicio = new CountDownLatch(1);
        Callable<Boolean> tarefa = () -> {inicio.await(); try { reservas.reservar(pedido(),c.getIdCliente().longValue());return true; }
            catch (RegraNegocioException e) {return false;} };
        try {
            Future<Boolean> a = executor.submit(tarefa), b = executor.submit(tarefa); inicio.countDown();
            assertThat(List.of(a.get(20,TimeUnit.SECONDS),b.get(20,TimeUnit.SECONDS))).containsExactlyInAnyOrder(true,false);
            assertThat(reservaRepository.count()).isEqualTo(1);
        } finally {executor.shutdownNow();}
    }
    @Test void calendarioComReservaEEventoIncompativelSaoProtegidos() {
        Calendario data = liberar();
        EventoForm evento = new EventoForm(); evento.setCalendarioId(data.getIdCalendario().longValue());
        evento.setDescricaoEvento("Evento"); evento.setDataEvento(data.getDataDisponivel().plusDays(1));
        assertThatThrownBy(() -> eventos.cadastrar(evento)).isInstanceOf(RegraNegocioException.class);
        evento.setDataEvento(data.getDataDisponivel()); eventos.cadastrar(evento);
        Reserva reserva = reservas.reservar(pedido(),c.getIdCliente().longValue());
        assertThatThrownBy(() -> calendarios.deleteById(data.getIdCalendario())).isInstanceOf(RegraNegocioException.class);
        reservas.cancelar(reserva.getNumeroReserva(),new UsuarioLogado(c.getIdCliente().longValue(), "Cliente", "CLIENTE"));
        assertThatThrownBy(() -> calendarios.deleteById(data.getIdCalendario())).isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);
    }
    @Test void fotoInvalidaNaoAlteraRegistroExistente() throws Exception {
        PrainhaForm f = form(); f.setIdPrainha(p.getIdPrainha()); f.setImagem(foto()); prainhas.save(f);
        byte[] original = prainhas.findById(p.getIdPrainha()).getImagem();
        f.setNomePrainha("Não deve salvar"); f.setImagem(new MockMultipartFile("imagem", "x.png", "image/png", new byte[5*1024*1024+1]));
        assertThatThrownBy(() -> prainhas.save(f)).isInstanceOf(RegraNegocioException.class);
        Prainha salva = prainhas.findById(p.getIdPrainha()); assertThat(salva.getNomePrainha()).isEqualTo("Prainha Teste");
        assertThat(salva.getImagem()).isEqualTo(original);
    }

    CadastroClienteForm cadastro(String email) {
        CadastroClienteForm f = new CadastroClienteForm(); f.setNome("Cliente adicional"); f.setCpf(identificadorSimulado());
        f.setEmail(email); f.setTelefone(identificadorSimulado()); f.setSenha(SENHA_CENARIO_5); f.setConfirmarSenha(SENHA_CENARIO_5); return f;
    }
    @Test void loginRealPorSenhaDefinePerfilECookie() throws Exception {
        for (String[] dados : new String[][]{{"admin@aqualeve.test",SENHA_CENARIO_6,"/admin","ADMINISTRADOR"},{c.getEmailCliente(),SENHA_CENARIO_5,"/","CLIENTE"}}) {
            var resultado = mvc.perform(post("/login").with(csrf()).param("email",dados[0]).param("senha",dados[1]).param("perfil","ADMINISTRADOR"))
                .andExpect(redirectedUrl(dados[2])).andReturn();
            var cookie = resultado.getResponse().getCookie("AQUALEVE_TOKEN");
            assertThat(cookie).isNotNull(); assertThat(cookie.isHttpOnly()).isTrue();
            assertThat(decoder.decode(cookie.getValue()).getClaimAsString("perfil")).isEqualTo(dados[3]);
        }
        mvc.perform(post("/login").with(csrf()).param("email",c.getEmailCliente()).param("senha",SENHA_CENARIO_2))
            .andExpect(status().isOk()).andExpect(model().attributeExists("erro")).andExpect(cookie().doesNotExist("AQUALEVE_TOKEN"));
        mvc.perform(post("/login").with(csrf()).param("email","invalido").param("senha",""))
            .andExpect(model().attributeHasFieldErrors("loginForm","email","senha"));
        assertThat(senhas.matches(SENHA_CENARIO_5,clienteRepository.findById(c.getIdCliente()).orElseThrow().getSenhaCliente())).isTrue();
        mvc.perform(post("/logout").with(perfil("CLIENTE")).with(csrf())).andExpect(redirectedUrl("/login?logout"))
            .andExpect(cookie().maxAge("AQUALEVE_TOKEN",0));
    }
    @Test void cadastroClienteValidaDuplicidadeConfirmacaoEPerfil() throws Exception {
        mvc.perform(post("/cadastro").with(csrf()).param("nome","Novo cliente").param("cpf",identificadorSimulado())
            .param("email","novo@aqualeve.test").param("telefone",identificadorSimulado()).param("senha",SENHA_CENARIO_4).param("confirmarSenha",SENHA_CENARIO_4)
            .param("perfil","ADMINISTRADOR")).andExpect(redirectedUrl("/login"));
        assertThat(clienteRepository.findByEmailClienteIgnoreCase("novo@aqualeve.test")).isPresent();
        assertThat(administradorRepository.findByEmailAdministradorIgnoreCase("novo@aqualeve.test")).isEmpty();
        assertThatThrownBy(() -> clientes.cadastrar(cadastro("ADMIN@AQUALEVE.TEST"))).isInstanceOf(RegraNegocioException.class);
        assertThatThrownBy(() -> clientes.cadastrar(cadastro(c.getEmailCliente()))).isInstanceOf(RegraNegocioException.class);
        var f = cadastro("outro@aqualeve.test");f.setConfirmarSenha(SENHA_CENARIO_1);
        assertThatThrownBy(() -> clientes.cadastrar(f)).isInstanceOf(RegraNegocioException.class);
        mvc.perform(post("/cadastro").with(csrf()).param("nome","x".repeat(41)).param("cpf","abc").param("email","invalido"))
            .andExpect(status().isOk()).andExpect(model().attributeHasFieldErrors("cadastroForm","nome","cpf","email"));
    }
    @Test void administradoresCadastroEdicaoExclusaoEProtecaoPropriaConta() throws Exception {
        mvc.perform(post("/administradores/salvar").with(perfil("ADMINISTRADOR")).with(csrf())
            .param("nome","Segundo admin").param("telefone",identificadorSimulado()).param("email","segundo@aqualeve.test")
            .param("senha",SENHA_CENARIO_5).param("confirmarSenha",SENHA_CENARIO_5)).andExpect(redirectedUrl("/administradores"));
        var a = administradorRepository.findByEmailAdministradorIgnoreCase("segundo@aqualeve.test").orElseThrow();
        mvc.perform(get("/administradores/editar/"+a.getIdAdministrador()).with(perfil("ADMINISTRADOR"))).andExpect(status().isOk());
        mvc.perform(post("/administradores/editar/"+a.getIdAdministrador()).with(perfil("ADMINISTRADOR")).with(csrf())
            .param("nome","Admin editado").param("telefone",identificadorSimulado())).andExpect(redirectedUrl("/administradores"));
        assertThat(administradorRepository.findById(a.getIdAdministrador()).orElseThrow().getNomeAdministrador()).isEqualTo("Admin editado");
        mvc.perform(post("/administradores/editar/"+a.getIdAdministrador()).with(perfil("ADMINISTRADOR")).with(csrf())
            .param("nome","").param("telefone","")).andExpect(model().attributeHasFieldErrors("contaForm","nome","telefone"));
        mvc.perform(post("/administradores/excluir/"+a.getIdAdministrador()).with(perfil("ADMINISTRADOR")).with(csrf())).andExpect(flash().attributeExists("sucesso"));
        assertThat(administradorRepository.existsById(a.getIdAdministrador())).isFalse();
        var inicial = administradorRepository.findByEmailAdministradorIgnoreCase("admin@aqualeve.test").orElseThrow();
        mvc.perform(post("/administradores/excluir/"+inicial.getIdAdministrador()).with(perfil("ADMINISTRADOR")).with(csrf())).andExpect(flash().attributeExists("erro"));
        assertThat(administradorRepository.existsById(inicial.getIdAdministrador())).isTrue();
        mvc.perform(post("/administradores/salvar").with(perfil("ADMINISTRADOR")).with(csrf()).param("nome",""))
            .andExpect(model().attributeHasFieldErrors("administradorForm","nome"));
    }
    @Test void clientePerfilEdicaoAdministrativaEExclusao() throws Exception {
        for (String url : List.of("/clientes/perfil","/reservas/nova","/reservas/minhas","/avaliacoes/nova?prainhaId="+p.getIdPrainha()))
            mvc.perform(get(url).with(perfil("CLIENTE"))).andExpect(status().isOk());
        mvc.perform(post("/clientes/perfil").with(perfil("CLIENTE")).with(csrf()).param("nome","Nome do perfil").param("telefone",identificadorSimulado())
            .param("idCliente","9999")).andExpect(redirectedUrl("/clientes/perfil"));
        assertThat(clientes.findById(c.getIdCliente()).getNomeCliente()).isEqualTo("Nome do perfil");
        mvc.perform(post("/clientes/perfil").with(perfil("CLIENTE")).with(csrf()).param("nome","").param("telefone",""))
            .andExpect(model().attributeHasFieldErrors("contaForm","nome","telefone"));
        mvc.perform(post("/clientes/editar/"+c.getIdCliente()).with(perfil("ADMINISTRADOR")).with(csrf())
            .param("nome","Editado pelo admin").param("telefone",identificadorSimulado())).andExpect(flash().attributeExists("sucesso"));
        mvc.perform(post("/clientes/editar/"+c.getIdCliente()).with(perfil("ADMINISTRADOR")).with(csrf())
            .param("nome","").param("telefone","")).andExpect(model().attributeHasFieldErrors("contaForm","nome"));
        mvc.perform(post("/clientes/excluir/"+c.getIdCliente()).with(perfil("ADMINISTRADOR")).with(csrf())).andExpect(flash().attributeExists("sucesso"));
        assertThat(clienteRepository.existsById(c.getIdCliente())).isFalse();
    }
    @Test void calendarioEEventosCrudPelasRotas() throws Exception {
        String dia = pedido().getDataHora().toLocalDate().toString();
        mvc.perform(post("/calendarios/salvar").with(perfil("ADMINISTRADOR")).with(csrf()).param("prainhaId",p.getIdPrainha().toString()).param("dataDisponivel",dia))
            .andExpect(redirectedUrl("/calendarios"));
        mvc.perform(post("/calendarios/salvar").with(perfil("ADMINISTRADOR")).with(csrf()).param("prainhaId",p.getIdPrainha().toString()).param("dataDisponivel",dia))
            .andExpect(status().isOk()).andExpect(model().attributeExists("erro"));
        mvc.perform(post("/calendarios/salvar").with(perfil("ADMINISTRADOR")).with(csrf()).param("prainhaId",p.getIdPrainha().toString()).param("dataDisponivel",LocalDate.now().minusDays(1).toString()))
            .andExpect(status().isOk()).andExpect(model().attributeExists("erro"));
        mvc.perform(post("/calendarios/salvar").with(perfil("ADMINISTRADOR")).with(csrf())).andExpect(model().attributeHasFieldErrors("calendarioForm","prainhaId","dataDisponivel"));
        var data = calendarioRepository.findAll().get(0);
        mvc.perform(post("/eventos/salvar").with(perfil("ADMINISTRADOR")).with(csrf()).param("calendarioId",data.getIdCalendario().toString()).param("dataEvento",data.getDataDisponivel().plusDays(1).toString()).param("descricaoEvento","Incompatível"))
            .andExpect(status().isOk()).andExpect(model().attributeExists("erro"));
        mvc.perform(post("/eventos/salvar").with(perfil("ADMINISTRADOR")).with(csrf())).andExpect(model().attributeHasFieldErrors("eventoForm","calendarioId","dataEvento","descricaoEvento"));
        mvc.perform(post("/eventos/salvar").with(perfil("ADMINISTRADOR")).with(csrf()).param("calendarioId",data.getIdCalendario().toString()).param("dataEvento",dia).param("descricaoEvento","Evento CRUD"))
            .andExpect(redirectedUrl("/eventos"));
        mvc.perform(get("/eventos")).andExpect(content().string(org.hamcrest.Matchers.containsString("Evento CRUD")));
        mvc.perform(post("/calendarios/excluir/"+data.getIdCalendario()).with(perfil("ADMINISTRADOR")).with(csrf())).andExpect(flash().attributeExists("erro"));
        mvc.perform(post("/eventos/excluir/"+eventosRepository.findAll().get(0).getIdEvento()).with(perfil("ADMINISTRADOR")).with(csrf())).andExpect(flash().attributeExists("sucesso"));
        mvc.perform(post("/calendarios/excluir/"+data.getIdCalendario()).with(perfil("ADMINISTRADOR")).with(csrf())).andExpect(flash().attributeExists("sucesso"));
        assertThat(calendarioRepository.count()).isZero(); assertThat(eventosRepository.count()).isZero();
    }
    @Test void reservasCriacaoTransicoesEIsolamentoPelasRotas() throws Exception {
        liberar();
        mvc.perform(post("/reservas/salvar").with(perfil("CLIENTE")).with(csrf()).param("prainhaId",p.getIdPrainha().toString()).param("tipoReserva","QUIOSQUE")
            .param("capacidadeReserva","3").param("dataHora",pedido().getDataHora().toString()).param("valorReserva","9999").param("clienteId","9999"))
            .andExpect(redirectedUrl("/reservas/minhas"));
        var r = reservaRepository.findAll().get(0);assertThat(r.getValorReserva()).isZero();assertThat(r.getCliente().getIdCliente()).isEqualTo(c.getIdCliente());
        Cliente outro = clientes.cadastrar(cadastro("outra-conta@aqualeve.test"));
        String outroToken = tokens.gerarToken(outro.getIdCliente().longValue(),outro.getEmailCliente(),outro.getNomeCliente(),"CLIENTE");
        mvc.perform(get("/reservas/minhas").with(tokenCookie(outroToken))).andExpect(model().attribute("reservas",org.hamcrest.Matchers.empty()));
        mvc.perform(post("/reservas/cancelar/"+r.getNumeroReserva()).with(tokenCookie(outroToken)).with(csrf())).andExpect(status().isForbidden());
        mvc.perform(post("/reservas/saida/"+r.getNumeroReserva()).with(perfil("ADMINISTRADOR")).with(csrf())).andExpect(flash().attributeExists("erro"));
        mvc.perform(post("/reservas/entrada/"+r.getNumeroReserva()).with(perfil("ADMINISTRADOR")).with(csrf())).andExpect(flash().attributeExists("sucesso"));
        assertThat(reservas.findById(r.getNumeroReserva()).getStatus()).isEqualTo(StatusReserva.EM_USO);
        mvc.perform(post("/reservas/entrada/"+r.getNumeroReserva()).with(perfil("ADMINISTRADOR")).with(csrf())).andExpect(flash().attributeExists("erro"));
        mvc.perform(post("/reservas/saida/"+r.getNumeroReserva()).with(perfil("ADMINISTRADOR")).with(csrf())).andExpect(flash().attributeExists("sucesso"));
        assertThat(reservas.findById(r.getNumeroReserva()).getStatus()).isEqualTo(StatusReserva.FINALIZADA);
        assertThat(reservas.findById(r.getNumeroReserva()).getSaidaEm()).isNotNull();
        mvc.perform(post("/reservas/cancelar/"+r.getNumeroReserva()).with(perfil("ADMINISTRADOR")).with(csrf())).andExpect(flash().attributeExists("erro"));
        mvc.perform(post("/reservas/salvar").with(perfil("CLIENTE")).with(csrf()).param("capacidadeReserva","0"))
            .andExpect(model().attributeHasFieldErrors("reservaForm","capacidadeReserva","prainhaId","dataHora","tipoReserva"));
    }
    @Test void tiposDatasDisponibilidadeEConflitos() throws Exception {
        liberar();
        var f = pedido();f.setTipoReserva("INVALIDO");assertThatThrownBy(() -> reservas.reservar(f,c.getIdCliente().longValue())).isInstanceOf(RegraNegocioException.class);
        f.setTipoReserva("QUIOSQUE");f.setDataHora(LocalDateTime.now().minusDays(1));assertThatThrownBy(() -> reservas.reservar(f,c.getIdCliente().longValue())).isInstanceOf(RegraNegocioException.class);
        for (String tipo : ReservaService.TIPOS) {var pedido=pedido();pedido.setTipoReserva(tipo);reservas.reservar(pedido,c.getIdCliente().longValue());}
        assertThat(reservas.consultarDisponibilidade(p.getIdPrainha(),pedido().getDataHora().toLocalDate()).getTiposOcupados()).containsExactlyInAnyOrderElementsOf(ReservaService.TIPOS);
        assertThat(reservas.consultarDisponibilidade(p.getIdPrainha(),LocalDate.now().minusDays(1)).isDiaLiberado()).isFalse();
        mvc.perform(get("/prainhas/"+p.getIdPrainha()).param("data",pedido().getDataHora().toLocalDate().toString())).andExpect(status().isOk()).andExpect(model().attributeExists("disponibilidade"));
    }
    @Test void avaliacaoCriacaoValidacaoExclusaoEPosse() throws Exception {
        mvc.perform(post("/avaliacoes/salvar").with(perfil("CLIENTE")).with(csrf()).param("prainhaId",p.getIdPrainha().toString()).param("nota","6"))
            .andExpect(model().attributeHasFieldErrors("avaliacaoForm","nota"));
        mvc.perform(post("/avaliacoes/salvar").with(perfil("CLIENTE")).with(csrf()).param("prainhaId",p.getIdPrainha().toString()).param("nota","4").param("comentario","Avaliação pela rota"))
            .andExpect(redirectedUrl("/prainhas/"+p.getIdPrainha()));
        var a=avaliacaoRepository.findAll().get(0); assertThat(avaliacoes.mediaDaPrainha(p.getIdPrainha())).isEqualTo(4.0);
        Cliente outro=clientes.cadastrar(cadastro("outro-avaliador@aqualeve.test"));
        mvc.perform(post("/avaliacoes/excluir/"+a.getIdAvaliacao()).with(tokenCookie(tokens.gerarToken(outro.getIdCliente().longValue(),outro.getEmailCliente(),"Outro","CLIENTE"))).with(csrf()))
            .andExpect(status().isForbidden());
        mvc.perform(post("/avaliacoes/excluir/"+a.getIdAvaliacao()).with(perfil("CLIENTE")).with(csrf())).andExpect(flash().attributeExists("sucesso"));
        assertThat(avaliacaoRepository.count()).isZero();
    }
    @Test void prainhasExclusaoOrfaVinculosENaoEncontradas() throws Exception {
        mvc.perform(get("/prainhas/2147483647")).andExpect(status().isNotFound());
        mvc.perform(get("/prainhas/editar/2147483647").with(perfil("ADMINISTRADOR"))).andExpect(status().isNotFound());
        mvc.perform(get("/clientes/editar/2147483647").with(perfil("ADMINISTRADOR"))).andExpect(status().isNotFound());
        mvc.perform(get("/administradores/editar/2147483647").with(perfil("ADMINISTRADOR"))).andExpect(status().isNotFound());
        var calendario=liberar();
        mvc.perform(post("/prainhas/excluir/"+p.getIdPrainha()).with(perfil("ADMINISTRADOR")).with(csrf())).andExpect(flash().attributeExists("erro"));
        calendarios.deleteById(calendario.getIdCalendario());
        mvc.perform(post("/prainhas/excluir/"+p.getIdPrainha()).with(perfil("ADMINISTRADOR")).with(csrf())).andExpect(flash().attributeExists("sucesso"));
        assertThat(prainhaRepository.existsById(p.getIdPrainha())).isFalse();
    }
    @Test void clienteVinculadoNaoPodeSerExcluido() throws Exception {
        liberar();reservas.reservar(pedido(),c.getIdCliente().longValue());
        mvc.perform(post("/clientes/excluir/"+c.getIdCliente()).with(perfil("ADMINISTRADOR")).with(csrf())).andExpect(flash().attributeExists("erro"));
        assertThat(clienteRepository.existsById(c.getIdCliente())).isTrue();
    }
    @Test void relatoriosFiltrosDatasECsv() throws Exception {
        liberar();reservas.reservar(pedido(),c.getIdCliente().longValue());
        mvc.perform(get("/admin/relatorios").with(perfil("ADMINISTRADOR"))).andExpect(model().attribute("total",1)).andExpect(model().attribute("confirmadas",1L));
        mvc.perform(get("/admin/relatorios").with(perfil("ADMINISTRADOR")).param("inicio",LocalDate.now().plusDays(10).toString())).andExpect(model().attribute("total",0));
        mvc.perform(get("/admin/relatorios").with(perfil("ADMINISTRADOR")).param("inicio","2030-02-02").param("fim","2030-01-01")).andExpect(status().isBadRequest());
        var f=form();f.setIdPrainha(p.getIdPrainha());f.setNomePrainha(" =1+1;\"teste\"");prainhas.save(f);
        mvc.perform(get("/admin/relatorios/csv").with(perfil("ADMINISTRADOR"))).andExpect(status().isOk())
            .andExpect(header().string("Content-Disposition","attachment; filename=relatorio-aqualeve.csv"))
            .andExpect(content().string(org.hamcrest.Matchers.containsString("\"'=1+1;\"\"teste\"\"\"")));
    }
    @Test void matrizDePerfisEmTodasAsOperacoesAdministrativas() throws Exception {
        for (String url : List.of("/admin","/admin/relatorios","/admin/relatorios/csv","/prainhas/criar","/prainhas/editar/1","/calendarios","/clientes","/administradores","/administradores/novo","/reservas/listar")) {
            mvc.perform(get(url).with(perfil("CLIENTE"))).andExpect(redirectedUrl("/acesso-negado"));
            mvc.perform(get(url)).andExpect(redirectedUrl("/login"));
        }
        for (String url : List.of("/prainhas/salvar","/prainhas/excluir/1","/calendarios/salvar","/calendarios/excluir/1","/eventos/salvar","/eventos/excluir/1","/clientes/editar/1","/clientes/excluir/1","/administradores/salvar","/administradores/editar/1","/administradores/excluir/1","/reservas/entrada/1","/reservas/saida/1")) {
            mvc.perform(post(url).with(perfil("CLIENTE")).with(csrf())).andExpect(redirectedUrl("/acesso-negado"));
        }
        for (String url : List.of("/reservas/nova","/reservas/minhas","/clientes/perfil","/avaliacoes/nova?prainhaId="+p.getIdPrainha()))
            mvc.perform(get(url).with(perfil("ADMINISTRADOR"))).andExpect(redirectedUrl("/acesso-negado"));
    }
    @Test void jpegAceitoGifRecusadoEImagemDesconhecida404() throws Exception {
        var f=form();f.setIdPrainha(p.getIdPrainha());
        var s=new ByteArrayOutputStream();ImageIO.write(new BufferedImage(20,10,BufferedImage.TYPE_INT_RGB),"jpeg",s);
        f.setImagem(new MockMultipartFile("imagem","foto.jpg","image/jpeg",s.toByteArray()));prainhas.save(f);
        mvc.perform(get("/prainhas/imagem/"+p.getIdPrainha())).andExpect(content().contentType("image/jpeg"));
        s.reset();ImageIO.write(new BufferedImage(20,10,BufferedImage.TYPE_INT_RGB),"gif",s);
        f.setImagem(new MockMultipartFile("imagem","foto.png","image/png",s.toByteArray()));
        assertThatThrownBy(() -> prainhas.save(f)).isInstanceOf(RegraNegocioException.class);
        mvc.perform(get("/prainhas/imagem/2147483647")).andExpect(status().isNotFound());
    }
    @Test void tokenInvalidoEContaExcluidaPerdemAcesso() throws Exception {
        mvc.perform(get("/clientes/perfil").with(tokenCookie("invalido"))).andExpect(redirectedUrl("/login"));
        String token=tokens.gerarToken(c.getIdCliente().longValue(),c.getEmailCliente(),"Cliente","CLIENTE");
        clientes.deleteById(c.getIdCliente());
        mvc.perform(get("/clientes/perfil").with(tokenCookie(token))).andExpect(redirectedUrl("/login"));
    }

    @Test void administradorExcluidoNaoMantemAcessoComCookieAntigo() throws Exception {
        var f=new CadastroAdministradorForm();f.setNome("Administrador removido");f.setTelefone(identificadorSimulado());f.setEmail("removido@aqualeve.test");f.setSenha(SENHA_CENARIO_5);f.setConfirmarSenha(SENHA_CENARIO_5);
        var admin=administradores.cadastrar(f);
        String token=tokens.gerarToken(admin.getIdAdministrador().longValue(),admin.getEmailAdministrador(),admin.getNomeAdministrador(),"ADMINISTRADOR");
        mvc.perform(get("/admin").with(tokenCookie(token))).andExpect(status().isOk());
        administradorRepository.deleteById(admin.getIdAdministrador());
        mvc.perform(get("/admin").with(tokenCookie(token))).andExpect(redirectedUrl("/login"));
        mvc.perform(post("/prainhas/excluir/"+p.getIdPrainha()).with(tokenCookie(token)).with(csrf())).andExpect(redirectedUrl("/login"));
        assertThat(prainhaRepository.existsById(p.getIdPrainha())).isTrue();
    }

    ReservaAdministradorForm pedidoAdministrativo() {
        ReservaForm base=pedido();var f=new ReservaAdministradorForm();f.setClienteId(c.getIdCliente());
        f.setPrainhaId(base.getPrainhaId());f.setTipoReserva(base.getTipoReserva());f.setCapacidadeReserva(base.getCapacidadeReserva());f.setDataHora(base.getDataHora());return f;
    }
    @Test void administradorCadastraEEditaReservaSemConflitarComElaMesma() throws Exception {
        liberar();
        mvc.perform(get("/admin/reservas/nova").with(perfil("ADMINISTRADOR"))).andExpect(status().isOk());
        mvc.perform(post("/admin/reservas/salvar").with(perfil("ADMINISTRADOR")).with(csrf())
            .param("clienteId",c.getIdCliente().toString()).param("prainhaId",p.getIdPrainha().toString()).param("tipoReserva","QUIOSQUE")
            .param("capacidadeReserva","4").param("dataHora",pedido().getDataHora().toString()))
            .andExpect(redirectedUrl("/reservas/listar"));
        Reserva r=reservaRepository.findAll().get(0);
        mvc.perform(get("/admin/reservas/editar/"+r.getNumeroReserva()).with(perfil("ADMINISTRADOR"))).andExpect(status().isOk());
        Cliente outro=clientes.cadastrar(cadastro("titular-editado@aqualeve.test"));
        mvc.perform(post("/admin/reservas/editar/"+r.getNumeroReserva()).with(perfil("ADMINISTRADOR")).with(csrf())
            .param("clienteId",outro.getIdCliente().toString()).param("prainhaId",p.getIdPrainha().toString()).param("tipoReserva","QUIOSQUE")
            .param("capacidadeReserva","7").param("dataHora",pedido().getDataHora().toString()))
            .andExpect(redirectedUrl("/reservas/listar"));
        r=reservas.findById(r.getNumeroReserva());assertThat(r.getCapacidadeReserva()).isEqualTo(7);
        assertThat(r.getCliente().getIdCliente()).isEqualTo(outro.getIdCliente());assertThat(r.getValorReserva()).isZero();
        mvc.perform(post("/admin/reservas/salvar").with(perfil("ADMINISTRADOR")).with(csrf()))
            .andExpect(model().attributeHasFieldErrors("reservaForm","clienteId","prainhaId","dataHora"));
    }
    @Test void edicaoRecusaConflitoEPreservaReservaOriginal() throws Exception {
        liberar();Reserva r=reservas.cadastrarReserva(pedidoAdministrativo());
        var outra=pedidoAdministrativo();outra.setTipoReserva("CHALE");reservas.cadastrarReserva(outra);
        var edicao=pedidoAdministrativo();edicao.setTipoReserva("CHALE");edicao.setCapacidadeReserva(10);
        assertThatThrownBy(() -> reservas.editarReserva(r.getNumeroReserva(),edicao)).isInstanceOf(RegraNegocioException.class);
        assertThat(reservas.findById(r.getNumeroReserva()).getTipoReserva()).isEqualTo("QUIOSQUE");
        assertThat(reservas.findById(r.getNumeroReserva()).getCapacidadeReserva()).isEqualTo(4);
        mvc.perform(post("/admin/reservas/editar/"+r.getNumeroReserva()).with(perfil("ADMINISTRADOR")).with(csrf())
            .param("clienteId",c.getIdCliente().toString()).param("prainhaId",p.getIdPrainha().toString()).param("tipoReserva","CHALE")
            .param("capacidadeReserva","10").param("dataHora",pedido().getDataHora().toString()))
            .andExpect(status().isOk()).andExpect(model().attributeExists("erro"));
    }
    @Test void edicaoMudaDataELiberaEspacoAnterior() {
        liberar();Reserva r=reservas.cadastrarReserva(pedidoAdministrativo());
        var calendario=new CalendarioForm();calendario.setPrainhaId(p.getIdPrainha().longValue());calendario.setDataDisponivel(pedido().getDataHora().toLocalDate().plusDays(1));calendarios.liberar(calendario);
        var edicao=pedidoAdministrativo();edicao.setDataHora(pedido().getDataHora().plusDays(1));reservas.editarReserva(r.getNumeroReserva(),edicao);
        assertThat(reservas.consultarDisponibilidade(p.getIdPrainha(),pedido().getDataHora().toLocalDate()).getTiposOcupados()).isEmpty();
        assertThat(reservas.consultarDisponibilidade(p.getIdPrainha(),edicao.getDataHora().toLocalDate()).getTiposOcupados()).containsExactly("QUIOSQUE");
    }
    @Test void edicaoDeReservaEmUsoOuCanceladaEPermissoes() throws Exception {
        liberar();Reserva r=reservas.cadastrarReserva(pedidoAdministrativo());reservas.registrarEntrada(r.getNumeroReserva());
        assertThatThrownBy(() -> reservas.editarReserva(r.getNumeroReserva(),pedidoAdministrativo())).isInstanceOf(RegraNegocioException.class);
        mvc.perform(get("/admin/reservas/editar/"+r.getNumeroReserva()).with(perfil("ADMINISTRADOR"))).andExpect(status().isBadRequest());
        for(String url:List.of("/admin/reservas/nova","/admin/reservas/editar/"+r.getNumeroReserva()))
            mvc.perform(get(url).with(perfil("CLIENTE"))).andExpect(redirectedUrl("/acesso-negado"));
        for(String url:List.of("/admin/reservas/salvar","/admin/reservas/editar/"+r.getNumeroReserva()))
            mvc.perform(post(url).with(perfil("CLIENTE")).with(csrf())).andExpect(redirectedUrl("/acesso-negado"));
        mvc.perform(post("/admin/reservas/salvar").with(perfil("ADMINISTRADOR"))).andExpect(redirectedUrl("/acesso-negado"));
        mvc.perform(get("/admin/reservas/editar/2147483647").with(perfil("ADMINISTRADOR"))).andExpect(status().isNotFound());
    }
    @Test void duasEdicoesConcorrentesNaoOcupamMesmoDestino() throws Exception {
        liberar();var f1=pedidoAdministrativo();f1.setTipoReserva("CHALE");var f2=pedidoAdministrativo();f2.setTipoReserva("CAMPING");
        int a=reservas.cadastrarReserva(f1).getNumeroReserva(),b=reservas.cadastrarReserva(f2).getNumeroReserva();
        var executor=Executors.newFixedThreadPool(2);var inicio=new CountDownLatch(1);
        try {
            List<Future<Boolean>> resultados=new ArrayList<>();
            for(Integer id:List.of(a,b)) resultados.add(executor.submit(() -> {inicio.await();try{reservas.editarReserva(id,pedidoAdministrativo());return true;}catch(RegraNegocioException e){return false;}}));
            inicio.countDown();assertThat(List.of(resultados.get(0).get(20,TimeUnit.SECONDS),resultados.get(1).get(20,TimeUnit.SECONDS))).containsExactlyInAnyOrder(true,false);
            assertThat(reservaRepository.findAll().stream().filter(r -> r.getTipoReserva().equals("QUIOSQUE")).count()).isEqualTo(1);
        }finally{executor.shutdownNow();}
    }
    @Test void prainhaTemResponsavelEditavelESuaExclusaoEProtegida() throws Exception {
        assertThat(prainhas.findById(p.getIdPrainha()).getAdministrador()).isNotNull();
        var f=new CadastroAdministradorForm();f.setNome("Responsável");f.setTelefone(identificadorSimulado());f.setEmail("responsavel@aqualeve.test");f.setSenha(SENHA_CENARIO_5);f.setConfirmarSenha(SENHA_CENARIO_5);
        var admin=administradores.cadastrar(f);var prainha=form();prainha.setIdPrainha(p.getIdPrainha());prainha.setAdministradorId(admin.getIdAdministrador());prainhas.save(prainha);
        assertThat(prainhas.findById(p.getIdPrainha()).getAdministrador().getIdAdministrador()).isEqualTo(admin.getIdAdministrador());
        mvc.perform(get("/prainhas/editar/"+p.getIdPrainha()).with(perfil("ADMINISTRADOR"))).andExpect(status().isOk());
        mvc.perform(post("/administradores/excluir/"+admin.getIdAdministrador()).with(perfil("ADMINISTRADOR")).with(csrf())).andExpect(flash().attributeExists("erro"));
        assertThat(administradorRepository.existsById(admin.getIdAdministrador())).isTrue();
        prainha.setAdministradorId(2147483647);assertThatThrownBy(() -> prainhas.save(prainha)).isInstanceOf(RegraNegocioException.class);
    }
    @Test void redefinirSenhaConfereAtualEConfirmacaoSemAlterarDadosEmFalhas() throws Exception {
        mvc.perform(get("/clientes/senha").with(perfil("CLIENTE"))).andExpect(status().isOk());
        mvc.perform(post("/clientes/senha").with(perfil("CLIENTE")).with(csrf()).param("senhaAtual",SENHA_CENARIO_2).param("novaSenha",SENHA_CENARIO_3).param("confirmarSenha",SENHA_CENARIO_3))
            .andExpect(status().isOk()).andExpect(model().attributeExists("erro"));
        mvc.perform(post("/clientes/senha").with(perfil("CLIENTE")).with(csrf()).param("senhaAtual",SENHA_CENARIO_5).param("novaSenha",SENHA_CENARIO_3).param("confirmarSenha",SENHA_CENARIO_1))
            .andExpect(model().attributeExists("erro"));
        mvc.perform(post("/clientes/senha").with(perfil("CLIENTE")).with(csrf()).param("senhaAtual",SENHA_CENARIO_5).param("novaSenha","curta").param("confirmarSenha","curta"))
            .andExpect(model().attributeHasFieldErrors("senhaForm","novaSenha"));
        mvc.perform(post("/clientes/senha").with(perfil("CLIENTE")).with(csrf()).param("senhaAtual","á".repeat(40)).param("novaSenha",SENHA_CENARIO_3).param("confirmarSenha",SENHA_CENARIO_3))
            .andExpect(status().isOk()).andExpect(model().attributeExists("erro"));
        assertThat(senhas.matches(SENHA_CENARIO_5,clientes.findById(c.getIdCliente()).getSenhaCliente())).isTrue();
        mvc.perform(get("/clientes/senha").with(perfil("ADMINISTRADOR"))).andExpect(redirectedUrl("/acesso-negado"));
        mvc.perform(post("/clientes/senha").with(perfil("CLIENTE"))).andExpect(redirectedUrl("/acesso-negado"));
    }
    @Test void senhaNovaRevogaTodosOsTokensAnterioresEPermiteNovoLogin() throws Exception {
        String antigo=tokens.gerarToken(c.getIdCliente().longValue(),c.getEmailCliente(),c.getNomeCliente(),"CLIENTE");
        mvc.perform(post("/clientes/senha").with(tokenCookie(antigo)).with(csrf()).param("senhaAtual",SENHA_CENARIO_5).param("novaSenha",SENHA_CENARIO_3).param("confirmarSenha",SENHA_CENARIO_3))
            .andExpect(redirectedUrl("/login")).andExpect(cookie().maxAge("AQUALEVE_TOKEN",0));
        mvc.perform(get("/clientes/perfil").with(tokenCookie(antigo))).andExpect(redirectedUrl("/login"));
        mvc.perform(post("/login").with(csrf()).param("email",c.getEmailCliente()).param("senha",SENHA_CENARIO_5)).andExpect(model().attributeExists("erro"));
        var result=mvc.perform(post("/login").with(csrf()).param("email",c.getEmailCliente()).param("senha",SENHA_CENARIO_3))
            .andExpect(redirectedUrl("/")).andReturn();
        mvc.perform(get("/clientes/perfil").with(tokenCookie(result.getResponse().getCookie("AQUALEVE_TOKEN").getValue()))).andExpect(status().isOk());
        assertThat(clientes.findById(c.getIdCliente()).getVersaoSenha()).isEqualTo(1L);
    }

    @Test void interfacePublicaMostraFotoCadastradaEEncaminhaEscolhas() throws Exception {
        var f=form();f.setIdPrainha(p.getIdPrainha());f.setImagem(foto());prainhas.save(f);
        String imagem="/prainhas/imagem/"+p.getIdPrainha();
        mvc.perform(get("/")).andExpect(status().isOk()).andExpect(content().string(org.hamcrest.Matchers.containsString(imagem)))
            .andExpect(content().string(org.hamcrest.Matchers.containsString("Foto cadastrada")));
        mvc.perform(get("/prainhas").param("data",LocalDate.now().plusDays(3).toString()).param("pessoas","6"))
            .andExpect(status().isOk()).andExpect(content().string(org.hamcrest.Matchers.containsString("pessoas=6")));
        liberar();
        mvc.perform(get("/prainhas/"+p.getIdPrainha()).with(perfil("CLIENTE")).param("data",pedido().getDataHora().toLocalDate().toString()).param("pessoas","6"))
            .andExpect(status().isOk()).andExpect(content().string(org.hamcrest.Matchers.containsString("name=\"dataHora\"")))
            .andExpect(content().string(org.hamcrest.Matchers.containsString("name=\"capacidadeReserva\"")));
    }
    @Test void paginasComReservasEAvaliacoesRenderizamNoNovoVisual() throws Exception {
        liberar();reservas.cadastrarReserva(pedidoAdministrativo());
        var a=new AvaliacaoForm();a.setPrainhaId(p.getIdPrainha().longValue());a.setNota(4);a.setComentario("Experiência validada na nova interface");avaliacoes.avaliar(a,c.getIdCliente().longValue());
        for(String url:List.of("/admin","/reservas/listar","/admin/relatorios"))
            mvc.perform(get(url).with(perfil("ADMINISTRADOR"))).andExpect(status().isOk()).andExpect(content().string(org.hamcrest.Matchers.containsString("Administração AquaLeve")));
        mvc.perform(get("/reservas/minhas").with(perfil("CLIENTE"))).andExpect(status().isOk()).andExpect(content().string(org.hamcrest.Matchers.containsString("Prainha Teste")));
        mvc.perform(get("/prainhas/"+p.getIdPrainha())).andExpect(status().isOk()).andExpect(content().string(org.hamcrest.Matchers.containsString(a.getComentario())));
    }
    @Test void paginasPublicasEArquivosVisuaisExistem() throws Exception {
        mvc.perform(get("/login")).andExpect(content().string(org.hamcrest.Matchers.containsString("name=\"_csrf\"")));
        for(String url:List.of("/sobre","/css/aqualeve.css","/js/aqualeve.js","/favicon.png","/imagens/aqualeve-logo.svg","/imagens/aqualeve-icone.svg","/imagens/prainha-hero.jpg","/imagens/prainha-quiosques.jpg","/imagens/prainha-palmeiras.jpg"))
            mvc.perform(get(url)).andExpect(status().isOk());
        mvc.perform(get("/prainhas/criar").with(perfil("ADMINISTRADOR"))).andExpect(content().string(org.hamcrest.Matchers.containsString("enctype=\"multipart/form-data\"")))
            .andExpect(content().string(org.hamcrest.Matchers.containsString("name=\"imagem\"")))
            .andExpect(content().string(org.hamcrest.Matchers.containsString("name=\"administradorId\"")));
    }

}
