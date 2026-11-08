# Revisão da distribuição

Nesta cópia, `mvn verify` foi executado novamente a partir dos fontes sem arquivos de compilação anteriores: 35 testes, zero falhas e zero erros, com H2. O comando `clean` não foi usado nesta validação local devido à indisponibilidade do plugin no cache; requer acesso à internet na máquina de destino. O restante deste documento registra também validações anteriores.

A distribuição não inclui ZIP aninhado nem Maven Wrapper. Consulte `docs/REVISAO-DE-SEGURANCA.md` para limitações da análise antivírus. O fluxo de verificação no Windows está preparado, mas não executado.

# Validação da entrega

Atualização: 7 de novembro de 2026.

## Integração da interface enviada

A versão atual usa somente os recursos de apresentação de `aqualeve_5_interface_lovable.zip`, sobre o backend de `AquaLeve-main.zip`. Nenhum arquivo Python é necessário ou incluído. Consulte `docs/INTEGRACAO-DA-INTERFACE.md`.

Os resultados HTTP anteriores abaixo são históricos. O relatório `relatorios/validacao-navegador.json` descreve a conferência da interface atual na aplicação empacotada.

## Navegador na interface atual

Chromium com aplicação JAR e PostgreSQL 17.6 em banco exclusivo de validação, resoluções 1440 × 1050 e 390 × 844. Fluxos aprovados: login administrativo, upload e edição de prainha preservando foto, calendário, evento, relatório, cadastro/login de cliente, vitrine/favoritos, reserva com data/hora, avaliação por estrelas, modal de confirmação e menu móvel. Nenhum erro de JavaScript ou resposta HTTP local >= 400 foi observado nesses fluxos. As capturas foram inspecionadas visualmente e não houve rolagem horizontal. Fontes externas foram substituídas pelas alternativas locais durante o teste.

O teste real do navegador também confirmou o funcionamento do CSRF após login e carregamento dos arquivos estáticos.

## Materiais considerados

- `aqualeve_4_completo.zip`: código-base, entidades, controllers, serviços, templates e configuração.
- `TCCAquaLeve 1.docx`: requisitos RF01–RF10 e RN01–RN04, dicionário de atributos e decisões do projeto.
- Arquivo Astah `.asta` e imagem do diagrama: classes, atributos e relacionamentos. O arquivo é um contêiner com modelo Java serializado; seus nomes de classes/atributos foram conferidos junto à imagem fornecida.
- Texto da conversa anterior: histórico do problema e das decisões de implementação. Não foi executado nenhum comando de exclusão de banco sugerido nesse histórico.

## Resultado automatizado

**35 testes, zero falhas e zero erros**, executados em cada configuração:

1. H2 2.4.240, modo PostgreSQL, banco de testes em memória.
2. PostgreSQL 17.6 real, banco `bdaqualeve` em `localhost:5432`, autenticação por senha e schema isolado `interface_v6_tests`.

Ambiente: JDK 21, compilação com `release 17`, Maven 3.9.16 e Spring Boot 4.1.1, mantendo a base recebida.

Os 34 cenários de integração mais a inicialização da aplicação cobrem:

- Cadastro multipart de prainha com PNG; gravação e leitura dos campos, busca por cidade, recuperação pública da imagem e página de detalhes.
- Edição preservando a foto, substituição e remoção da foto.
- Formulário inválido e arquivo que se apresenta como PNG mas não contém imagem: sem criação de registro.
- Edição com foto acima de 5 MB: transação revertida e nome/foto anteriores preservados.
- Login real de administrador e cliente, conferência de perfil, cookie HttpOnly, senha incorreta, entrada inválida e logout.
- Cadastro de cliente com validação de e-mail duplicado entre perfis, confirmação de senha e campos inválidos. O cadastro público não aceita elevação para administrador.
- Cadastro, edição e exclusão de administradores; bloqueio da exclusão da própria conta.
- Edição de perfil do cliente, edição pelo administrador, formulários inválidos e exclusão de cliente sem vínculos.
- Visitante sem acesso ao cadastro, cliente sem acesso administrativo e POST sem CSRF recusado. Matriz de acesso nas rotas de criação/edição/exclusão de cada módulo.
- JWT inválido recusado; clientes e administradores excluídos perdem acesso com os cookies antigos.
- Renderização das telas administrativas de prainhas, calendário, eventos, clientes, administradores, reservas e relatório.
- Reserva sem calendário recusada; data duplicada recusada; conflito de reserva recusado; cancelamento por outro cliente recusado; entrada, saída, cancelamento, avaliação e CSV.
- Duas tentativas de reserva simultâneas: somente uma confirmada.
- Reserva pela rota HTTP: cliente e valor enviados indevidamente pelo formulário não substituem os valores do servidor; cancelamento por outra conta recusado e listagem de reservas isolada por cliente.
- Todas as três categorias de espaço, datas passadas, disponibilidade por data e transições inválidas de entrada/saída/cancelamento.
- Avaliações via rotas: nota fora da escala recusada, cálculo da média, publicação, exclusão pelo autor e bloqueio de exclusão por outra conta.
- Relatório com filtros de data, contagens, período invertido recusado e exportação CSV com escape de aspas e proteção de fórmulas.
- Exclusão de prainha sem vínculos, bloqueio de exclusão com calendário e bloqueio de exclusão de cliente com reserva.
- JPEG aceito, GIF disfarçado de PNG recusado, imagens ausentes e páginas de edição inexistentes retornam 404.
- Data de evento incompatível recusada; exclusão de calendário com reserva ativa bloqueada; integridade com eventos vinculados preservada.

Foram acrescentadas verificações de imagem e parâmetros na vitrine, renderização com reservas/avaliações e existência dos arquivos visuais.

Os testes de autenticação de integração usam cookies com JWT assinado pelo serviço real da aplicação. O teste HTTP separado também usa o formulário real de login, com senha e CSRF.

Para executar os testes padrão:

```bash
mvn test
```

No Windows: `mvn test`.

Os testes fazem limpeza dos registros de teste e usam `create-drop`. Uma execução manual com PostgreSQL deve apontar somente para um banco/schema descartável de testes, nunca para dados que se deseja manter. Não há necessidade de PostgreSQL para o comando padrão acima.

## Verificação HTTP anterior com aplicação empacotada

Aplicação iniciada com `java -jar`, sem argumentos ou variáveis de sobrescrita, usando o `application.properties` local enviado, Tomcat e PostgreSQL real:

- Páginas públicas e login do administrador.
- Envio do formulário multipart com CSRF e PNG válido de 800 × 450 pixels e aproximadamente 1,03 MiB.
- Consulta dos detalhes e download da imagem gravada.
- Cadastro de data de calendário e evento.
- Navegação administrativa e exportação CSV.
- Cadastro e login de cliente, criação de reserva, avaliação e cancelamento.
- Edição de prainha sem enviar nova foto preserva seus bytes.
- Envio de arquivo maior que 5 MB retorna HTTP 413 e mantém a foto anterior.
- Reinicialização da aplicação preserva a prainha e a foto; os bytes da imagem foram comparados por SHA-256 antes e depois.

## Validação adicional do alinhamento ao diagrama

- Cadastro de reserva para um cliente pelo administrador, com valor e status definidos no servidor.
- Edição sem falso conflito com a própria reserva; mudança de titular/quantidade; recusa de conflito com outra reserva sem alterar a original.
- Mudança de data libera a ocupação anterior e ocupa o novo destino.
- Duas edições concorrentes para o mesmo destino: somente uma aceita.
- Bloqueio de edição de reserva em uso; rotas administrativas recusadas a clientes e sem CSRF.
- Responsável da prainha persistido e editável; exclusão de administrador vinculado bloqueada.
- Troca de senha exige a senha atual e confirmação correta; validações preservam o hash anterior em caso de falha.
- Senha atual que exceda o limite de bytes do BCrypt é recusada com mensagem, sem erro interno.
- Troca bem-sucedida invalida cookies anteriores; senha antiga deixa de autenticar e nova senha permite login.

Os diagramas foram gerados a partir das entidades e as operações documentadas foram conferidas contra os métodos reais. Veja `docs/ALINHAMENTO-DO-DIAGRAMA.md` para decisões de modelagem, migração e formatos editáveis.

## Correções e acréscimos

- Banco `bdaqualeve`, usuário `postgres` e porta 8080 preservados. Na cópia para GitHub, senhas e chave JWT exigem configuração no ambiente local.
- Mapeamento explícito de `imagem` como `bytea`, sem o limite de 255 bytes do tipo binário padrão no H2. JPEG/PNG são armazenados integralmente.
- Autenticação confirma existência, perfil e e-mail da conta em cada requisição; a exclusão da conta encerra seu acesso mesmo antes da expiração do JWT.
- Na cópia para GitHub, senhas locais e chave JWT foram removidas; são configuradas por variáveis de ambiente.
- Formulário próprio de prainha, validação dos dados e atualização explícita do registro existente, sem aceitar bytes/tipo de imagem diretamente do formulário.
- Persistência transacional da foto no banco, com validação de formato, tamanho e dimensões, e preservação na edição.
- Exibição da foto na página inicial, lista e detalhes; endpoint público somente de leitura.
- Proteção CSRF preservada no formulário multipart.
- Aliases públicos de listagens coerentes com seus controllers.
- Limites de nomes alinhados ao banco para impedir falhas de persistência após validação da tela.
- Bloqueios transacionais na criação e alteração de reservas.
- Proteção do calendário com reservas ativas e coerência entre evento e data liberada.
- Relatório administrativo com período, contagens por status e CSV.
- Guia de execução atualizado e testes reproduzíveis sem depender do banco pessoal.
- Associação Administrador—Prainha, cadastro/edição de reservas administrativas e troca autenticada de senha, com diagramas revisados e dicionário de atributos.

## Limites da validação

O banco instalado na máquina do usuário não foi fornecido. Seu esquema e dados históricos não foram migrados nem testados diretamente. Os testes confirmam o funcionamento no esquema gerado pelo projeto, inclusive no PostgreSQL real; não permitem atribuir com certeza a mensagem de erro anterior a uma única causa local.

A interface enviada foi integrada. Não foram feitos teste de carga, implantação pública nem integração de pagamento. Tipos de espaço, quantidade de unidades, valor zero e relacionamento do calendário são decisões herdadas, descritas no README. A documentação acadêmica deve ser ajustada para refletir Spring Boot/Thymeleaf/PostgreSQL e os novos campos de imagem.

## Preparação para compartilhamento — 7 de novembro de 2026

A cópia do GitHub remove as credenciais locais. Senhas e chave JWT dos testes agora são geradas em `ConfiguracaoDeTeste`; identificadores usados nos cenários são simulados. Após essa alteração, os 32 testes foram executados novamente em H2, com zero falhas e zero erros. A implementação principal permanece a mesma validada anteriormente em PostgreSQL.
