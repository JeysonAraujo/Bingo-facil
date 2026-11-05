# Bingo-AI

O Bingo-AI é uma plataforma completa para gerenciamento e execução de jogos de bingo de forma interativa e em tempo real. A aplicação permite a criação de eventos, gerenciamento de rodadas, acompanhamento em tempo real através de websockets, integração com transmissões ao vivo (YouTube), além de geração de cartelas em PDF com QR Codes.

## 🏗 Arquitetura do Projeto

A solução é dividida em três partes principais: **Frontend**, **Backend** e **Infraestrutura/Banco de Dados**.

### 💻 Frontend (Interface do Usuário)
A aplicação web (SPA - Single Page Application) foi construída com tecnologias modernas visando alta performance e reatividade.
- **Framework Principal:** Vue.js 3 (Composition API) + TypeScript
- **Build Tool:** Vite
- **Gerenciamento de Estado:** Pinia
- **Roteamento:** Vue Router
- **Design e Estilização:** 
  - Vuetify 3 (Componentes de UI)
  - TailwindCSS 4 (Utilitários de estilo CSS)
- **Comunicação em Tempo Real:** SockJS e STOMP (WebSockets)
- **Requisições HTTP:** Axios
- **Testes End-to-End (E2E):** Playwright

### ⚙️ Backend (Serviços e Regras de Negócio)
A API RESTful robusta responsável por gerenciar os eventos, segurança, websockets e geração de documentos.
- **Linguagem:** Java 21
- **Framework Principal:** Spring Boot 3.3.0
- **Persistência e Banco de Dados:**
  - Spring Data JPA
  - PostgreSQL (Banco de Dados Relacional principal)
  - Flyway (Controle de versionamento de banco de dados/Migrations)
  - H2 Database (Banco em memória utilizado para testes)
- **Segurança:** Spring Security
- **Comunicação em Tempo Real:** Spring Boot WebSocket
- **Geração de Arquivos:**
  - OpenPDF (Geração das cartelas de bingo em PDF)
  - ZXing (Geração de QR Codes para as cartelas)
- **Validações:** Spring Boot Validation

### 🐳 Infraestrutura
- **Docker Compose:** Utilizado para orquestrar rapidamente o ambiente de desenvolvimento, subindo a instância do banco de dados PostgreSQL 16 de maneira isolada.

---

## 🚀 Funcionalidades Principais

- **Gestão de Eventos (Dashboard Admin):**
  - Criação e edição de eventos de Bingo.
  - Definição de data/hora de agendamento (`scheduledAt`).
  - Configuração de integração com transmissões do YouTube (`youtubeUrl`), incluindo compensação de atraso de transmissão (`streamDelaySeconds`).
  - Imagem de capa (Header) personalizada para o evento.
  - Controle de status do evento (ex: "AGUARDANDO", "EM ANDAMENTO", "FINALIZADO").

- **Gestão de Rodadas:**
  - Suporte a múltiplas rodadas (`Rounds`) para cada evento.

- **Game Console em Tempo Real:**
  - Console de operação do jogo pelo administrador para sorteio de números.
  - Comunicação bidirecional e instantânea com os jogadores via WebSockets (utilizando mensageria de status).

- **Geração e Validação de Cartelas:**
  - Geração automatizada de cartelas em formato PDF, prontas para impressão ou distribuição digital.
  - Inclusão de QR Codes em cada cartela para fácil rastreio e validação de vencedores.

---

## 🛠 Como Executar o Projeto Localmente

### Pré-requisitos
- Docker e Docker Compose (para o banco de dados)
- Java 21 (JDK)
- Node.js e npm (ou yarn/pnpm)

### Passo 1: Subir o Banco de Dados
Na raiz do projeto, inicie o container do PostgreSQL com o Docker Compose:
```bash
docker-compose up -d
```
Isso iniciará o banco de dados `bingodb` na porta `5433` com as credenciais padrão de desenvolvimento configuradas no `docker-compose.yml`:
- **Host:** `localhost`
- **Porta:** `5433` (mapeada internamente para `5432`)
- **Database:** `bingodb`
- **Usuário:** `bingo_user`
- **Senha:** `bingo_password`

> **Nota de Segurança:** As credenciais acima são exclusivas para ambiente de testes e execução local. Para implantações em ambientes de produção/homologação, as credenciais devem ser injetadas por variáveis de ambiente (`DATABASE_URL`, `DATABASE_USER`, `DATABASE_PASSWORD`).

### Passo 2: Executar o Backend
Navegue até o diretório `bingo-backend` e inicie a aplicação Spring Boot:
```bash
cd bingo-backend
./mvnw spring-boot:run
```
*(As migrações do banco de dados serão aplicadas automaticamente pelo Flyway no momento da inicialização)*.

### Passo 3: Executar o Frontend
Em um novo terminal, navegue até o diretório `bingo-frontend`:

1. Configure as variáveis de ambiente com base no exemplo:
```bash
cd bingo-frontend
cp .env.example .env
```
*(No Windows PowerShell: `Copy-Item .env.example .env`)*

2. Instale as dependências e inicie o servidor de desenvolvimento:
```bash
npm install
npm run dev
```

Após esses passos, acesse a URL exibida no terminal do frontend (geralmente `http://localhost:5173`) para utilizar a aplicação.
