package br.com.tcc.aqualeve;

import java.util.UUID;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/** Dados efêmeros para testes isolados; não usa credenciais ou pessoas reais. */
abstract class ConfiguracaoDeTeste {
    protected static final String SENHA_CENARIO_1 = UUID.randomUUID().toString();
    protected static final String SENHA_CENARIO_2 = UUID.randomUUID().toString();
    protected static final String SENHA_CENARIO_3 = UUID.randomUUID().toString();
    protected static final String SENHA_CENARIO_4 = UUID.randomUUID().toString();
    protected static final String SENHA_CENARIO_5 = UUID.randomUUID().toString();
    protected static final String SENHA_CENARIO_6 = UUID.randomUUID().toString();
    private static final String CHAVE_JWT_EFEMERA = UUID.randomUUID().toString() + UUID.randomUUID();
    protected static String identificadorSimulado() { return "0".repeat(11); }

    @DynamicPropertySource
    static void configurar(DynamicPropertyRegistry props) {
        props.add("aqualeve.jwt.secret", () -> CHAVE_JWT_EFEMERA);
        props.add("aqualeve.admin.email", () -> "admin@aqualeve.test");
        props.add("aqualeve.admin.senha", () -> SENHA_CENARIO_6);
    }
}
