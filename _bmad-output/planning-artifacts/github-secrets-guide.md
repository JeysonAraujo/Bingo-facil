# 🔐 Guia de Configuração de Secrets no GitHub Actions — Bingo Fácil (Beta)

Este documento orienta a equipe sobre os **Secrets** necessários no repositório GitHub (`Settings -> Secrets and variables -> Actions`) para a execução automática do pipeline [`.github/workflows/deploy-beta.yml`](file:///d:/Users/bingo-ai/.github/workflows/deploy-beta.yml).

---

## 🔑 Lista de Secrets Necessários

| Nome do Secret | Descrição | Exemplo de Valor |
| :--- | :--- | :--- |
| `GCP_SA_KEY` | Chave JSON da Service Account do GCP com permissões no Artifact Registry, Cloud Run e Cloud SQL | `{"type": "service_account", ...}` |
| `GCP_PROJECT_ID` | ID do projeto no Google Cloud Platform | `bingo-facil-beta` |
| `GCP_REGION` | Região dos serviços Cloud Run e Artifact Registry | `southamerica-east1` |
| `CLOUD_SQL_INSTANCE` | Connection Name da instância do PostgreSQL no Cloud SQL | `bingo-facil-beta:southamerica-east1:bingo-db` |
| `FIREBASE_SERVICE_ACCOUNT_BINGO_FACIL` | Chave da Service Account para deploy no Firebase Hosting | `{"type": "service_account", ...}` |
| `VITE_API_BASE_URL` | URL do backend publicado no Cloud Run para injeção no frontend | `https://bingo-backend-beta-southamerica-east1.a.run.app` |
| `FRONTEND_BETA_URL` | URL pública do frontend hospedado no Firebase Hosting | `https://bingo-facil-beta.web.app` |

---

## 🚀 Estrutura dos Jobs do Pipeline (`deploy-beta.yml`)

1. **☕ `deploy-backend` (Cloud Run)**:
   - Checkout do código + Setup Java 21 (Temurin, Maven cache).
   - Execução dos testes unitários e de integração (`./mvnw test`).
   - Compilação e empacotamento (`./mvnw package`).
   - Autenticação no GCP + Build da Imagem Docker multi-stage.
   - Push para o Google Artifact Registry (`southamerica-east1-docker.pkg.dev`).
   - Deploy automático no Google Cloud Run configurado com conexão via socket no Cloud SQL e profile `beta`.

2. **⚡ `deploy-frontend` (Firebase Hosting)**:
   - Checkout do código + Setup Node.js 20 (NPM cache).
   - Instalação limpa de dependências (`npm ci`).
   - Validação de tipos Vue 3 + TypeScript (`npm run type-check`).
   - Build de produção via Vite (`npm run build-only`) com injeção das variáveis de ambiente.
   - Deploy automático no Firebase Hosting canal `live` (`bingo-facil-beta`).

3. **🧪 `smoke-test` (Validação Automatizada Pós-Deploy)**:
   - Requisição HTTP para o endpoint Actuator (`/actuator/health`).
   - Requisição HTTP para a raiz do Firebase Hosting.
