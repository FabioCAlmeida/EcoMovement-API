# EcoMovement API — Missões

link da api: https://github.com/zanfolim07/Projeto-EcoMovement
link do deploy: https://projetoecomovement.vercel.app

API Restful em **Java + Spring Boot** do EcoMovement (Sprint 04 — *Domain Driven Design Using Java*).
Ela é consumida pelo front-end em React e guarda os dados no banco **Oracle**, usando as tabelas do
MER do grupo (`T_ECMV_...`).

```
React (site)  ──HTTP/JSON──▶  API Spring Boot  ──JDBC──▶  Oracle (tabelas T_ECMV_)
```

O usuário escolhe uma missão no site, envia uma **foto ou vídeo** como comprovação, o EcoGuard
**aprova ou rejeita** no Painel `/admin` e, se aprovada, o usuário ganha os **EcoPoints**.

---

## Tecnologias

- Java 21
- Spring Boot 3.5 (Spring Web)
- JDBC com driver Oracle (`ojdbc11`) — padrão DAO + Connection Factory, sem JPA
- Oracle Database (servidor da FIAP)
- Swagger / OpenAPI (springdoc) para documentar e testar os endpoints
- Maven
- Docker e Render (deploy em nuvem)

## Estrutura do projeto

```
src/main/java/br/com/ecomovement
├── EcomovementApplication.java   classe principal (inicia a API)
├── factory    ConnectionFactory           conexão com o Oracle
├── config     CorsFilter                  libera o front-end (CORS)
├── br.com.ecomovement.model      Missao, Submissao, Evidencia, ValidacaoEcoguard, MovimentoEcopoints,
│              ProgressoMissao, Usuario, NovoUsuario, Contato, LoginAdmin,
│              PosicaoRanking, Arquivo
├── dao        um DAO por tabela, com o SQL (CRUD)
├── bo         MissaoBO, SubmissaoBO, UsuarioBO, ContatoBO (regras de negócio e validações)
├── resource   endpoints REST (@RestController)
└── exception  RegraNegocioException, NaoEncontradoException, NaoAutorizadoException,
               ConflitoException e o TratadorDeErros (respostas de erro em JSON)

database/
├── 01_criacao_tabelas.sql   cria as tabelas iniciais
├── 02_alteracoes_mer.sql    colunas e tabelas adicionadas ao MER da Sprint 3
├── 03_carga_dados.sql       carga inicial (5 ou mais registros por tabela)
└── 04_consultas.sql         consultas com JOIN, GROUP BY, funções de agregação e WHERE

Dockerfile                   empacota a API para o deploy (Render)
```

Caminho de uma requisição: **resource** recebe → **bo** valida as regras → **dao** executa o SQL → Oracle.

---

## Como rodar

### Ferramentas

- **JDK 21** ou mais novo
- **Maven 3.9+** (ou o Maven embutido do IntelliJ)
- IntelliJ IDEA ou VS Code
- Oracle SQL Developer

### 1. Banco de dados

No SQL Developer, abra e rode com **F5 (Executar Script)**, nesta ordem:

1. **`database/01_criacao_tabelas.sql`** — cria as tabelas do MER (T_ECMV_...). Rode uma vez, em um schema sem
   essas tabelas (se elas já existirem, dá erro ORA-00955).
2. **`database/02_alteracoes_mer.sql`** — rode uma vez depois do 01 (adiciona as colunas e tabelas novas).
3. **`database/03_carga_dados.sql`** — insere os dados. Pode rodar de novo: ele limpa e insere tudo outra vez
   (apaga também os envios e pontos que você tiver feito).
4. **`database/04_consultas.sql`** — só faz consultas nas tabelas e mostra os dados atuais do sistema.

O **`database/04_consultas.sql`** não altera nada: pode rodar à vontade.

Usuários criados pela carga (contas de demonstração, apenas para testes):

| Usuário | E-mail | Senha | Perfil |
|---|---|---|---|
| Usuário Demo (id 1) | `demo@ecomovement.com` | `senha123` | USUARIO (usado pelo site) |
| Admin EcoGuard | `ecomovement@admin.com` | `1234` | ADMIN (login do `/admin`) |

### 2. Configurar a conexão

Copie o `.env.example` para `.env` e coloque o seu usuário e senha do Oracle:

```
DB_URL=jdbc:oracle:thin:@oracle.fiap.com.br:1521:ORCL
DB_USER=rm123456
DB_PASSWORD=sua_senha
```

> O `.env` não vai para o GitHub (está no `.gitignore`). Nunca coloque a senha real no repositório.

### 3. Iniciar a API

```bash
mvn spring-boot:run
```

No IntelliJ: abra a pasta do projeto (ele importa o `pom.xml` sozinho) e rode a classe `EcomovementApplication`.

Quando aparecer `Started EcomovementApplication`, abra:

- **http://localhost:8080** → `"status": "online"`
- **http://localhost:8080/swagger-ui.html** → documentação e teste de todos os endpoints
- **http://localhost:8080/missoes?usuarioId=1** → lista das missões em JSON

Deixe esse terminal aberto enquanto usa o site.

### 4. Iniciar o site (React)

Baixe o front-end: https://github.com/zanfolim07/Projeto-EcoMovement

Em **outro terminal**, na pasta do front:

```bash
npm install
cp .env.example .env.local
```

No `.env.local`, coloque o endereço da API (local ou a publicada):

```
VITE_API_URL=http://localhost:8080
VITE_USUARIO_ID=1
```

Para usar a API publicada, troque o endereço por `https://ecomovement-api.onrender.com`.

E rode `npm run dev`. Abra **http://localhost:5173**.

> **CORS:** por padrão, a API aceita apenas os sites em `http://localhost:5173` e `http://127.0.0.1:5173`.
> Para liberar outros endereços (outra porta, o site da Vercel etc.), defina a variável `CORS_ORIGINS` com a
> lista separada por vírgula, sem barra no final. Exemplo:
> `CORS_ORIGINS=http://localhost:5173,http://localhost:5174,https://seu-site.vercel.app`

### 5. Testar o fluxo completo

1. No site, em **Missões**, clique em **Fazer missão agora** e envie a foto ou o vídeo de uma missão.
2. Abra **http://localhost:5173/admin** e entre com `ecomovement@admin.com` / `1234`.
3. Em **Em análise**, veja a evidência e clique em **Aprovar**.
4. Volte em **Missões**: a missão aparece como concluída e os pontos aumentam.
5. No SQL Developer, rode o `database/04_consultas.sql` para ver os dados gravados no Oracle.
   (Não rode o `03_carga_dados.sql`: ele apaga os dados do teste.)

### Resumo: o que precisa ficar aberto

| O quê | Precisa ficar aberto? |
|---|---|
| Banco Oracle | Não, ele fica no servidor da FIAP (o SQL Developer pode ficar fechado) |
| API (`mvn spring-boot:run`) | **Sim**, em um terminal |
| Site (`npm run dev`) | **Sim**, em outro terminal |

Depois do deploy (API no Render e site na Vercel), nada precisa ficar aberto no seu computador.

---

## Endpoints

### Missões

| Verbo  | URI                      | Descrição                                              | Status |
|--------|--------------------------|--------------------------------------------------------|--------|
| GET    | `/missoes?usuarioId=1`   | Missões ativas com o status e o progresso do usuário   | 200, 400, 404 |
| GET    | `/missoes`               | Catálogo completo (inclui as inativas)                 | 200 |
| GET    | `/missoes/{id}?usuarioId=1` | Detalhe de uma missão                               | 200, 404 |
| POST   | `/missoes`               | Cadastra uma missão                                    | 201, 400 |
| PUT    | `/missoes/{id}`          | Atualiza uma missão                                    | 200, 400, 404 |
| DELETE | `/missoes/{id}`          | Exclui uma missão (só se ela não tiver envios)         | 204, 400, 404 |
| GET    | `/missoes/{id}/imagem`   | Devolve a imagem da missão                             | 200, 404 |
| PUT    | `/missoes/{id}/imagem`   | Envia a imagem (arquivo no corpo, `Content-Type: image/...`) | 204, 400, 404 |

### Submissões (envio de evidências)

| Verbo  | URI                              | Descrição                                          | Status |
|--------|----------------------------------|----------------------------------------------------|--------|
| POST   | `/submissoes`                    | Envia a foto/vídeo (multipart: `usuarioId`, `missaoId`, `tipoEvidencia`, `duracaoSegundos`, `arquivo`) | 201, 400, 404 |
| GET    | `/submissoes?status=EM_ANALISE`  | Envios por status (EM_ANALISE, APROVADA, REJEITADA) | 200, 400 |
| GET    | `/submissoes/{id}`               | Detalhe de um envio                                | 200, 404 |
| GET    | `/submissoes/{id}/evidencia`     | Devolve a foto/vídeo enviado                       | 200, 404 |
| PUT    | `/submissoes/{id}/aprovar`       | EcoGuard aprova (credita os EcoPoints)             | 200, 400, 404 |
| PUT    | `/submissoes/{id}/rejeitar`      | EcoGuard rejeita (o usuário pode reenviar)         | 200, 400, 404 |
| DELETE | `/submissoes/{id}`               | Exclui o envio e os registros ligados a ele        | 204, 404 |

### Usuários, admin e contato

| Verbo  | URI                           | Descrição                                   | Status |
|--------|-------------------------------|---------------------------------------------|--------|
| POST   | `/usuarios`                   | Cadastra um usuário (`{ "nome", "email", "senha" }`) | 201, 400, 409 |
| GET    | `/usuarios/{id}`              | Detalhe de um usuário                       | 200, 404 |
| GET    | `/usuarios/{id}/submissoes`   | Histórico de envios do usuário              | 200, 404 |
| DELETE | `/usuarios/{id}/submissoes`   | Reinicia o teste: apaga os envios de foto/vídeo do usuário | 200, 404 |
| GET    | `/usuarios/{id}/pontos`       | Saldo de EcoPoints: `{ "total": 20 }`       | 200, 404 |
| GET    | `/ranking`                    | Ranking dos usuários por EcoPoints (`posicao`, `usuarioId`, `nome`, `pontos`) | 200 |
| POST   | `/admin/login`                | Login do Painel EcoGuard (`{ "email", "senha" }`) | 200, 400, 401 |
| POST   | `/contatos`                   | Salva a mensagem do formulário de contato   | 201, 400 |
| GET    | `/contatos`                   | Lista as mensagens recebidas                | 200 |
| GET    | `/contatos/{id}`              | Detalhe de uma mensagem de contato          | 200, 404 |
| DELETE | `/contatos/{id}`              | Exclui uma mensagem de contato              | 204, 404 |

### Códigos de erro

Todos os erros vêm em JSON, no mesmo formato:

```json
{ "status": 400, "mensagem": "Esta missão exige evidência do tipo VIDEO." }
```

| Código | Quando acontece |
|---|---|
| 400 | Regra de negócio violada ou requisição inválida (parâmetro ausente, JSON malformado, arquivo grande) |
| 401 | E-mail ou senha incorretos, ou usuário sem perfil ADMIN |
| 404 | Recurso ou rota não encontrada |
| 405 | Método HTTP não permitido para o endereço |
| 409 | Conflito: já existe um usuário com o e-mail informado |
| 415 | Tipo de conteúdo (`Content-Type`) não suportado |
| 500 | Erro de banco de dados ou erro inesperado no servidor (a mensagem é genérica; o detalhe fica no log) |

---

## Regras de negócio

- Missão de **FOTO** só aceita imagem, e missão de **VIDEO** só aceita vídeo (até 50 MB).
- O vídeo não pode passar do tempo máximo da missão (ex.: 60 segundos).
- Não dá para enviar de novo uma missão que está em análise ou já foi aprovada.
- Missões **AUTOMATICAS** não aceitam envio: o progresso vem da plataforma SoulUp.
- Ao **aprovar**, a API grava a validação (`T_ECMV_VALIDACAO_ECOGUARD`) e o crédito de pontos
  (`T_ECMV_MOVIMENTO_ECOPOINTS`). Os pontos do usuário são a soma desses movimentos.
- O **ranking** soma os movimentos de cada usuário (menos o ADMIN) e a API define a posição:
  maior pontuação primeiro e, em caso de empate, os usuários dividem a mesma posição.
- Na carga, o Usuário Demo (id 1) começa em 5º lugar com 20 pontos. Os usuários 3 a 6 já têm
  120, 90, 60 e 30 pontos. Em qualquer ordem, cada missão aprovada no `/admin` faz o usuário de teste passar
  uma pessoa, e ao concluir as 4 missões (130 pontos) ele chega ao 1º lugar.
- Missão com envios não pode ser excluída (perderia o histórico): ela deve ser **desativada**.
- **Cadastro de usuário:** nome de 2 a 150 caracteres, e-mail válido e único (repetido responde 409) e senha de
  6 a 72 caracteres, salva em hash SHA-256 com o perfil USUARIO.
- O login do admin confere a senha pelo hash **SHA-256** salvo no banco e exige o perfil **ADMIN**.

## Ajustes no MER

Para atender o site, o `02_alteracoes_mer.sql` adiciona ao modelo da Sprint 3:

| Tabela | O que foi adicionado |
|---|---|
| `T_ECMV_MISSAO` | `INSTRUCOES_MISSAO`, `PONTOS_MISSAO`, `TP_EVIDENCIA_MISSAO`, `TEMPO_MAXIMO_SEG_MISSAO`, `META_MISSAO`, `IMAGEM_MISSAO` (BLOB), `TP_IMAGEM_MISSAO` |
| `T_ECMV_EVIDENCIA` | `ARQUIVO_EVIDENCIA` (BLOB), `TP_ARQUIVO_EVIDENCIA`, `DURACAO_SEG_EVIDENCIA` |
| `T_ECMV_VALIDACAO_ECOGUARD` | `DT_VALIDACAO_ECOGUARD` |
| `T_ECMV_SUBMISSAO` | regra (CHECK) dos status: EM_ANALISE, APROVADA, REJEITADA |
| **nova** `T_ECMV_PROGRESSO_MISSAO` | progresso das missões automáticas por usuário |
| **nova** `T_ECMV_CONTATO` | mensagens do formulário de contato |

## Deploy

A API está publicada no **Render** (Web Service com Docker, usando o `Dockerfile` da raiz).

| Item | Endereço |
|---|---|
| URL da API | https://ecomovement-api.onrender.com |
| Swagger (documentação) | https://ecomovement-api.onrender.com/swagger-ui.html |
| Repositório da API | https://github.com/FabioCAlmeida/EcoMovement-API |
| Repositório do front-end | https://github.com/zanfolim07/Projeto-EcoMovement |
| Site na Vercel | _(preencher: URL do site na Vercel)_ |

> O plano gratuito do Render hiberna depois de alguns minutos sem uso: a primeira requisição pode levar cerca de 1 minuto.

Variáveis de ambiente configuradas no servidor (os valores ficam só no painel do Render):

| Variável | Descrição |
|---|---|
| `DB_URL`, `DB_USER`, `DB_PASSWORD` | Conexão com o Oracle |
| `API_URL` | Endereço público da API (usado para montar as URLs de imagens e evidências) |
| `CORS_ORIGINS` | Endereços dos sites autorizados a chamar a API, separados por vírgula |
