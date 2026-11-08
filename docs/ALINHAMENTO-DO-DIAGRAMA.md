# Alinhamento do AquaLeve ao diagrama revisado

A orientação escolhida foi preservar os recursos pedidos e corrigir as inconsistências do diagrama. Portanto, esta entrega implementa e documenta uma revisão do modelo; não reproduz literalmente os erros e omissões do desenho original.

## Arquivos do modelo

- `diagramas/classes-revisado.svg`: diagrama vetorial do modelo persistido, indicado para incluir no TCC sem perda de resolução.
- `diagramas/classes-revisado.png`: imagem do mesmo modelo.
- `diagramas/classes-revisado.puml`: fonte editável em PlantUML.
- `diagramas/classes-revisado.dot`: fonte editável em Graphviz.
- `diagramas/operacoes-spring.svg`, `.png`, `.puml` e `.dot`: visão das responsabilidades e operações de aplicação.
- `diagramas/modelo-extraido.json`: inventário de atributos extraído das entidades Java e usado na geração do diagrama.
- `referencia/diagrama-original.asta`: arquivo Astah recebido, preservado sem alterações como referência histórica.

**O arquivo `.asta` original não foi regravado.** O ambiente não dispõe do editor/API do Astah para salvar com fidelidade seu formato binário. Os diagramas revisados estão nos formatos editáveis PlantUML/Graphviz e nas imagens SVG/PNG; eles devem substituir a figura antiga da documentação. Não renomeie um arquivo `.puml` ou uma imagem para `.asta`.

## Decisões e implementação

| Ponto do original | Decisão revisada | Implementação |
|---|---|---|
| CPF `int` | CPF `String`, preservando tamanho e zeros iniciais | `Cliente.cpfCliente` e formulário de cadastro |
| Ausência de `id_cliente` em Cliente, mas referência em Avaliacao | Identificador técnico `Integer idCliente` | Chave primária de Cliente; FK `id_cliente` em Reserva e Avaliacao |
| Administrador—Prainha | Um administrador pode ser responsável por várias prainhas; uma prainha tem até um responsável | `Prainha.administrador`, `ManyToOne`, coluna `id_administrador`; seleção no cadastro/edição |
| Responsável em registros antigos | Vínculo opcional para preservar registros já cadastrados; novos cadastros recebem o administrador atual ou o padrão | Coluna nullable; dados antigos podem ser atribuídos manualmente pelo formulário |
| Autorização administrativa | Gestão compartilhada por perfil; responsabilidade não restringe o acesso dos outros administradores | Regras do Spring Security mantidas; não se presumiu propriedade exclusiva no diagrama |
| Calendário 1:1 | Uma prainha possui várias datas liberadas | `Calendario.prainha`, ManyToOne; unicidade por prainha/data |
| Eventos→Calendario como dependência | Associação persistente N:1 necessária à consulta por prainha/data | FK obrigatória em Eventos; data do evento precisa coincidir com a data liberada |
| Fotos ausentes | Foto faz parte dos requisitos aprovados | `Prainha.imagem: byte[]`, `imagemTipo: String`, validação JPEG/PNG e persistência bytea |
| Status/entrada/saída ausentes | Mantidos para gerenciar a reserva | Enum `StatusReserva`, `entradaEm`, `saidaEm`, disponibilidade sincronizada |
| `Date` genérico | `LocalDate` para dias e `LocalDateTime` para data/hora | Calendario, Eventos e Avaliacao usam LocalDate; Reserva usa LocalDateTime |
| `Double` em Reserva | Mantido `double` não nulo, já utilizado pelo código | Valor definido pelo servidor; continua zero, pois tarifas/pagamento não fazem parte do modelo implementado |
| Operações em entidades | Operações implementadas em serviços/controllers, conforme arquitetura Spring | Sem métodos vazios nem injeção de serviços nas entidades |

A cardinalidade 0..1 do responsável expressa a situação real de migração. Novos cadastros feitos pela interface recebem um responsável. Os demais relacionamentos obrigatórios têm cardinalidade 1 no lado da entidade referenciada. O diagrama não implica que existam coleções Java inversas: a navegação JPA permanece unidirecional, conforme o código.

Os atributos no diagrama usam os nomes Java camelCase. As colunas correspondentes usam snake_case no banco. Getters e setters gerados por Lombok foram omitidos da figura para facilitar a leitura.

## Correspondência das operações do desenho original

| Operação original | Implementação revisada |
|---|---|
| Administrador.excluir_cliente | ClienteController → ClienteService.deleteById; vínculos impedem exclusão |
| Administrador.editar_cliente | ClienteController → ClienteService.atualizarConta |
| Administrador.verificar_reserva | ReservaController.listar → ReservaService.findAll |
| Administrador.cadastrar_reserva | **Adicionada:** ReservaAdministradorController.salvar → ReservaService.cadastrarReserva |
| Administrador.editar_reserva | **Adicionada:** ReservaAdministradorController.atualizar → ReservaService.editarReserva |
| Administrador.logar | AuthController.entrar → AuthService.login |
| Administrador.gerenciar_entrada | ReservaController.entrada/saida → ReservaService.registrarEntrada/registrarSaida |
| Administrador.gerar_relatorio | RelatorioController.relatorio/csv |
| Cliente.cadastrar | AuthController.cadastrar → ClienteService.cadastrar |
| Cliente.logar | AuthController.entrar → AuthService.login |
| Cliente.redefinir_senha | **Adicionada:** ClienteController.redefinirSenha → ClienteService.redefinirSenha |
| Cliente.consultar_prainha | PrainhaController.listar/detalhe → PrainhaService |
| Cliente.avaliar_prainha | AvaliacaoController.salvar → AvaliacaoService.avaliar |
| Cliente.realizar_reserva | ReservaController.salvar → ReservaService.reservar |
| Cliente.consultar_reserva | ReservaController.minhas → ReservaService.findByCliente |
| Cliente.visualizar_calendario | PrainhaController.detalhe → CalendarioService.datasFuturas e ReservaService.consultarDisponibilidade |

A visão de operações mostra os métodos relevantes aos casos de uso; helpers e CRUD interno dos repositórios foram omitidos. Os parâmetros e retornos refletem os métodos reais, em vez das assinaturas genéricas sem argumentos/retorno `void` do desenho inicial.

## Como usar os recursos adicionados

### Reservas pelo administrador

Em **Painel → Reservas**, use **Nova reserva para cliente**. Selecione um cliente cadastrado, prainha, espaço, quantidade de pessoas e data/hora. O cliente selecionado passa a visualizar a reserva em “Minhas reservas”.

O botão **Editar** aparece nas reservas confirmadas. Permite alterar cliente, prainha, tipo de espaço, quantidade e data/hora. Reservas em uso, finalizadas ou canceladas não podem ser editadas. Valor e status não são aceitos do formulário.

Criação e edição compartilham as regras de data futura, calendário liberado e ausência de conflito. Ao editar, a reserva atual é excluída da contagem de conflitos, evitando que conflite consigo mesma. O destino é bloqueado na transação, protegendo contra criação/edição simultânea no mesmo espaço/dia. Ao mudar data/espaço, a ocupação anterior é liberada após confirmar a alteração.

### Redefinir senha do cliente

Em **Meu perfil → Redefinir minha senha**, informe a senha atual, a nova senha e sua confirmação. A nova senha deve ter pelo menos 8 caracteres e no máximo 72 bytes UTF-8, conforme o BCrypt.

Após salvar, todos os JWT anteriores desse cliente deixam de ser aceitos, e o usuário entra novamente com a nova senha. O contador técnico `Cliente.versaoSenha` permite essa revogação; registros antigos sem contador equivalem à versão zero.

O fluxo implementado é **troca de senha autenticada**, com confirmação da senha atual. Não é recuperação de senha esquecida por e-mail. Não existe configuração de entrega de e-mail nos materiais recebidos; por isso não foi criado um fluxo público que permitisse trocar senha apenas sabendo CPF/e-mail.

### Administrador responsável

No cadastro/edição de prainha, escolha o responsável. Outros administradores continuam podendo gerenciar os registros. Um administrador com prainhas vinculadas não pode ser excluído: transfira antes suas prainhas para outro responsável.

## Atualização do banco

O `ddl-auto=update` permanece ativo. Foram adicionadas somente as colunas opcionais `prainha.id_administrador` e `cliente.versao_senha`, além da FK para Administrador. Nenhum registro antigo é removido ou reduzido a uma única data de calendário. A senha armazenada continua como hash BCrypt.

A atualização foi testada em banco PostgreSQL isolado e em esquema gerado pelos testes. Antes de atualizar dados pessoais/antigos, valide uma cópia do banco. Não há script de DROP SCHEMA nesta entrega.

## Ajustes na documentação do TCC

1. Substitua a figura antiga pelo SVG/PNG do modelo revisado.
2. Atualize o dicionário de atributos com os campos de foto, responsável, estado da reserva, entrada/saída e versão de credencial.
3. Descreva o calendário como várias datas por prainha.
4. Descreva a troca de senha como autenticada; não afirme recuperação por e-mail.
5. Use a tabela acima para explicar a separação de responsabilidades no Spring Boot.
6. A arquitetura continua Spring Boot/Thymeleaf/PostgreSQL. Referências a React/Supabase no texto acadêmico devem ser ajustadas para refletir esta implementação.
