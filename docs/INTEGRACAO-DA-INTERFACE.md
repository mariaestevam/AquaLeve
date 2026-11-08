# Integração da interface enviada

Entrega de 7 de novembro de 2026.

## Origem e escopo

A versão `AquaLeve-main.zip` forneceu o backend atual. Do pacote `aqualeve_5_interface_lovable.zip` foram aproveitados somente templates Thymeleaf, CSS, JavaScript, ícones e imagens. O backend antigo que acompanhava a interface não foi importado.

As 39 classes das pastas `entity`, `repository`, `service` e `dto` foram preservadas integralmente. Controllers receberam apenas os dados de apresentação necessários ao painel, formulários e detalhes, além da página Sobre. `VisualHelper` formata a apresentação; não grava registros nem calcula disponibilidade de reservas.

## Ajustes de compatibilidade

- Formulários continuam ligados aos DTOs e rotas do backend atual.
- Cadastro/edição de prainhas mantêm upload multipart, responsável, prévia da foto, substituição e remoção.
- Imagens cadastradas são lidas do banco e têm prioridade sobre as ilustrações incluídas.
- Referências a PNGs ausentes foram corrigidas para os SVGs/JPGs realmente fornecidos.
- Expressões de imagens, ícones e filtros foram adaptadas às restrições de segurança do Thymeleaf.
- O token CSRF é materializado no início do cabeçalho, antes do sprite SVG extenso, para que o cookie seja gravado antes de o envio da resposta começar. A proteção continua ativa, inclusive no upload.
- A rotação do CSRF ocorre no login bem-sucedido. O reconhecimento do JWT em arquivos estáticos não apaga o cookie dos formulários; logout continua limpando a autenticação.
- Foram mantidos os acessos a relatórios, reservas administrativas e troca de senha que a interface antiga não apresentava.
- Favoritos são locais ao navegador; filtros e ordenação dos cartões operam sobre os dados exibidos. Reservas, calendário e avaliações continuam no servidor Java.

## Execução sem Python

O script auxiliar de geração de diagramas e o ZIP antigo aninhado foram removidos. Nenhum arquivo Python integra esta entrega. O sistema executa com JDK, Maven e PostgreSQL, servindo a própria interface; não exige Node/npm ou outro servidor.

Veja o README para copiar `config/application.properties.example` e preencher as configurações locais. Credenciais pessoais e banco de dados não acompanham a entrega. O administrador inicial usa os dados configurados na máquina; contas existentes mantêm suas senhas.

## Validação

Consulte `VALIDACAO.md` e os relatórios em `relatorios`. Os diagramas editáveis foram preservados, assim como o alinhamento de requisitos documentado anteriormente.
