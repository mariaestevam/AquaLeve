# AquaLeve

Sistema do TCC em **Java 17+, Spring Boot, Thymeleaf e PostgreSQL**. A interface foi integrada a partir do arquivo `aqualeve_5_interface_lovable`, usando somente a apresentação (HTML, CSS, JavaScript e imagens).

**Python não é utilizado nem necessário. Não é necessário Node.js, npm, React ou um servidor separado para executar o sistema.** As páginas e os arquivos estáticos são servidos pelo próprio Spring Boot.

## Distribuição revisada

Este pacote contém o código-fonte e a interface. Não inclui ZIP dentro do projeto nem scripts de download automático do Maven. Requer Maven instalado. A investigação do alerta do Microsoft Defender está em `docs/REVISAO-DE-SEGURANCA.md`; ausência de detecção por outro motor não garante ausência de alertas no Defender.

O resultado da verificação no Windows está em [GitHub Actions](https://github.com/mariaestevam/AquaLeve/actions/workflows/verificacao-windows.yml). O ZIP aprovado é disponibilizado em [Releases](https://github.com/mariaestevam/AquaLeve/releases). A aprovação descreve aquela execução; não garante ausência de novos alertas em outras configurações do antivírus.

## 1. Pré-requisitos

- JDK 17 ou superior, incluindo o compilador `javac` (não somente JRE).
- PostgreSQL 14 ou superior, iniciado na sua máquina.
- Apache Maven 3.9 ou superior instalado (confira `mvn -version`).
- Internet na primeira compilação para baixar as dependências pelo Maven.

Confira no terminal:

```text
java -version
javac -version
```

Se `javac` não for reconhecido, instale/configure um JDK e confira `JAVA_HOME`/`PATH` antes de executar o Maven.

## 2. Criar o banco

No PostgreSQL, crie o banco caso ele ainda não exista:

```sql
CREATE DATABASE bdaqualeve;
```

Se já possui o banco do AquaLeve, use-o sem apagar tabelas. O modelo e as regras do backend anterior foram preservados. `ddl-auto=update` permanece configurado; faça uma cópia de segurança antes de atualizar dados antigos.

## 3. Configurar sua máquina

1. Na pasta `config`, faça uma cópia de `application.properties.example`.
2. Renomeie a cópia para **`application.properties`** (sem `.example` e sem `.txt`).
3. Preencha a senha do seu PostgreSQL, uma chave JWT aleatória de pelo menos 32 caracteres e o e-mail/senha do administrador inicial. Confira também URL e usuário do banco.

O Spring Boot lê automaticamente `config/application.properties` quando iniciado na raiz do projeto. Esse arquivo local fica fora do Git; não publique suas credenciais. O arquivo `.example` está vazio nos campos de senha/chave.

Para gerar uma chave JWT no PowerShell:

```powershell
$bytes = New-Object byte[] 48
$rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
$rng.GetBytes($bytes)
[Convert]::ToBase64String($bytes)
$rng.Dispose()
```

Copie o resultado para `aqualeve.jwt.secret` no arquivo local. Guarde essa mesma chave para as próximas inicializações.

Como alternativa ao arquivo local, as variáveis `AQUALEVE_DB_URL`, `AQUALEVE_DB_USER`, `AQUALEVE_DB_PASSWORD`, `AQUALEVE_JWT_SECRET`, `AQUALEVE_ADMIN_EMAIL` e `AQUALEVE_ADMIN_SENHA` continuam disponíveis. Escolha um modo de configuração e mantenha os valores coerentes.

## 4. Compilar, testar e iniciar

Instale o Maven pelo site oficial https://maven.apache.org/install.html e confira `mvn -version`.

Abra o terminal **na pasta AquaLeve, onde está o `pom.xml`**.

Windows / PowerShell:

```powershell
mvn clean verify
mvn spring-boot:run
```

Linux/macOS:

```bash
mvn clean verify
mvn spring-boot:run
```

Abra **http://localhost:8080**. Para entrar como administrador, use o e-mail e a senha que configurou. Uma conta já existente no banco conserva sua senha: mudar a configuração inicial não redefine a senha de um cadastro anterior.

Também pode iniciar o JAR gerado após a compilação, na mesma pasta do projeto:

```text
java -jar target/aqualeve-0.0.1-SNAPSHOT.jar
```

Se a porta 8080 estiver ocupada, encerre a outra instância ou defina `server.port=8081` no arquivo local e acesse a nova porta.

## Funcionalidades preservadas

- Cadastro, busca por cidade, detalhes e edição de prainhas.
- Foto JPEG/PNG de até 5 MB, com substituição, remoção e preservação na edição; foto cadastrada tem prioridade na vitrine e nos detalhes.
- Administrador responsável selecionável e gestão compartilhada por perfil.
- Calendário com várias datas por prainha e eventos associados.
- Reserva pelo cliente ou administrador; edição de reservas confirmadas, conflito de espaço/data, cancelamento, entrada e saída.
- Avaliações, perfil, troca autenticada de senha e revogação de sessões anteriores.
- Gestão de clientes/administradores e relatórios HTML/CSV.
- Autenticação JWT em cookie HttpOnly e proteção CSRF dos formulários, inclusive upload.

## Interface

O desenho, as cores, os cartões, menus, ícones, filtros visuais, favoritos e calendário de apresentação vêm da interface enviada. As fotos padrão são ilustrativas; uma imagem enviada pelo administrador substitui a ilustração para aquela prainha. Todos os SVGs e fotos necessários estão incluídos.

Favoritos são uma preferência local do navegador. Datas disponíveis, reservas, clientes, avaliações e relatórios continuam vindo do backend Java e do banco. Filtros de serviços e ordenação dos cartões são apenas recursos visuais. Não foi importado o backend antigo do pacote de interface.

## Testes e documentação

`mvn test` (ou `mvn test`) usa um H2 isolado e não precisa do seu PostgreSQL. Senhas/chave JWT dos testes são geradas em execução, sem credenciais reais nos arquivos.

Consulte `VALIDACAO.md`, `docs/INTEGRACAO-DA-INTERFACE.md`, o dicionário de atributos e os diagramas em `docs/diagramas`. O gerador auxiliar Python foi removido; os diagramas editáveis `.puml` e `.dot`, SVG e PNG permanecem disponíveis. Graphviz/PlantUML são opcionais para editar as figuras e não fazem parte da execução do AquaLeve.
