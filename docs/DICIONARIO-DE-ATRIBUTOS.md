# Dicionário de atributos do modelo revisado

Os nomes Java abaixo correspondem às entidades desta entrega. As referências JPA são armazenadas como chaves estrangeiras; não se duplicam os ids das associações nos objetos. Senhas são hashes BCrypt, não texto simples.

## Administrador

| Atributo Java | Tipo Java | Coluna no banco | Tipo/representação |
|---|---|---|---|
| `idAdministrador` | `Integer` | `id_administrador` | integer |
| `nomeAdministrador` | `String` | `nome_administrador` | varchar (limite definido na entidade) |
| `telefoneAdministrador` | `String` | `telefone_administrador` | varchar (limite definido na entidade) |
| `emailAdministrador` | `String` | `email_administrador` | varchar (limite definido na entidade) |
| `senhaAdministrador` | `String` | `senha_administrador` | varchar (limite definido na entidade) |

## Avaliacao

| Atributo Java | Tipo Java | Coluna no banco | Tipo/representação |
|---|---|---|---|
| `idAvaliacao` | `Integer` | `id_avaliacao` | integer |
| `notaAvaliacao` | `Integer` | `nota_avaliacao` | integer |
| `comentarioAvaliacao` | `String` | `comentario_avaliacao` | varchar (limite definido na entidade) |
| `dataAvaliacao` | `LocalDate` | `data_avaliacao` | date |
| `cliente` | `Cliente` | `id_cliente` | FK integer para Cliente |
| `prainha` | `Prainha` | `id_prainha` | FK integer para Prainha |

## Calendario

| Atributo Java | Tipo Java | Coluna no banco | Tipo/representação |
|---|---|---|---|
| `idCalendario` | `Integer` | `id_calendario` | integer |
| `dataDisponivel` | `LocalDate` | `data_disponivel` | date |
| `prainha` | `Prainha` | `id_prainha` | FK integer para Prainha |

## Cliente

| Atributo Java | Tipo Java | Coluna no banco | Tipo/representação |
|---|---|---|---|
| `idCliente` | `Integer` | `id_cliente` | integer |
| `versaoSenha` | `Long` | `versao_senha` | bigint |
| `cpfCliente` | `String` | `cpf_cliente` | varchar (limite definido na entidade) |
| `nomeCliente` | `String` | `nome_cliente` | varchar (limite definido na entidade) |
| `emailCliente` | `String` | `email_cliente` | varchar (limite definido na entidade) |
| `telefoneCliente` | `String` | `telefone_cliente` | varchar (limite definido na entidade) |
| `senhaCliente` | `String` | `senha_cliente` | varchar (limite definido na entidade) |

## Eventos

| Atributo Java | Tipo Java | Coluna no banco | Tipo/representação |
|---|---|---|---|
| `idEvento` | `Integer` | `id_evento` | integer |
| `dataEvento` | `LocalDate` | `data_evento` | date |
| `descricaoEvento` | `String` | `descricao_evento` | varchar (limite definido na entidade) |
| `calendario` | `Calendario` | `id_calendario` | FK integer para Calendario |

## Prainha

| Atributo Java | Tipo Java | Coluna no banco | Tipo/representação |
|---|---|---|---|
| `idPrainha` | `Integer` | `id_prainha` | integer |
| `nomePrainha` | `String` | `nome_prainha` | varchar (limite definido na entidade) |
| `descricaoPrainha` | `String` | `descricao_prainha` | varchar (limite definido na entidade) |
| `cidadePrainha` | `String` | `cidade_prainha` | varchar (limite definido na entidade) |
| `servicosPrainha` | `String` | `servicos_prainha` | varchar (limite definido na entidade) |
| `administrador` | `Administrador` | `id_administrador` | FK integer para Administrador |
| `imagem` | `byte[]` | `imagem` | bytea |
| `imagemTipo` | `String` | `imagem_tipo` | varchar (limite definido na entidade) |

## Reserva

| Atributo Java | Tipo Java | Coluna no banco | Tipo/representação |
|---|---|---|---|
| `numeroReserva` | `Integer` | `numero_reserva` | integer |
| `capacidadeReserva` | `Integer` | `capacidade_reserva` | integer |
| `valorReserva` | `double` | `valor_reserva` | double precision |
| `tipoReserva` | `String` | `tipo_reserva` | varchar (limite definido na entidade) |
| `disponibilidadeReserva` | `String` | `disponibilidade_reserva` | varchar (limite definido na entidade) |
| `dataHora` | `LocalDateTime` | `data_hora` | timestamp |
| `status` | `StatusReserva` | `status` | varchar (enum como texto) |
| `entradaEm` | `LocalDateTime` | `entrada_em` | timestamp |
| `saidaEm` | `LocalDateTime` | `saida_em` | timestamp |
| `cliente` | `Cliente` | `id_cliente` | FK integer para Cliente |
| `prainha` | `Prainha` | `id_prainha` | FK integer para Prainha |

## StatusReserva

Valores: `CONFIRMADA`, `EM_USO`, `FINALIZADA` e `CANCELADA`.

## Restrições e significado

- IDs são gerados pelo banco via sequences.
- `cpfCliente` preserva zeros iniciais e permite a formatação existente (coluna de até 14 caracteres).
- `versaoSenha` é um contador técnico: nulo em cadastro anterior equivale a zero. Incrementa ao trocar a senha, invalidando tokens antigos.
- `Prainha.administrador` identifica o responsável; é opcional para compatibilidade com cadastros anteriores.
- `imagem` contém a imagem processada e `imagemTipo` seu tipo MIME validado.
- `Calendario` é único por `(id_prainha, data_disponivel)`.
- O tipo de espaço e o dia determinam o conflito de reservas ativas.
- `capacidadeReserva` é a quantidade de visitantes da reserva; o modelo não cadastra limite físico por espaço.
- `valorReserva` continua definido pelo servidor em zero, pois não há cadastro de tarifas/pagamento.
- `entradaEm` e `saidaEm` são preenchidos nas transições de status.
