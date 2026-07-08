# Auditoria de Segurança, QA e Banco de Dados — Bingo AI

**Data da análise:** 7 de julho de 2026  
**Pacote analisado:** `bingo-rev.zip`  
**Escopo:** revisão estática do backend Spring Boot/PostgreSQL, frontend Vue/Vite, WebSocket/STOMP, fluxo de cartelas/claims, migrations Flyway, dependências e qualidade de entrega.  
**Não foi realizado:** teste ativo contra produção, acesso SSH, varredura de portas, análise de firewall/WAF/TLS de um domínio público ou acesso a banco de dados real. Portanto, os itens de infraestrutura abaixo são checklist obrigatório de validação no servidor, não afirmações sobre o ambiente atualmente publicado.

---

## 1. Resumo executivo

O projeto possui uma boa base técnica — uso de PostgreSQL, Flyway, `BCryptPasswordEncoder`, validação em parte dos DTOs, UUIDs e geração criptograficamente segura das cartelas. Contudo, **não está pronto para produção pública na forma atual**.

Foram identificados riscos que permitem, ou podem permitir:

- Tomada de conta administrativa por credenciais padrão criadas automaticamente;
- Vazamento do token de sessão de jogadores para outros clientes conectados ao WebSocket;
- Reativação/assunção de uma cartela por qualquer pessoa que possua seu serial;
- Operações entre eventos de administradores diferentes por falhas de autorização por recurso (IDOR/BOLA);
- Início indevido de uma rodada por uma requisição pública de consulta;
- Spam e leitura indevida de tópicos WebSocket;
- Duplicação de claims e inconsistência do resultado do bingo em concorrência;
- Recuperação de senha vulnerável por código previsível, exibido no log e sem limitação de tentativas;
- Falhas reais de build/TypeScript e empacotamento inadequado do repositório;
- Uma dependência frontend com vulnerabilidade de severidade alta apontada pelo `npm audit`.

### Classificação geral

| Severidade | Quantidade | Ação |
|---|---:|---|
| Crítica | 4 | Corrigir antes de qualquer publicação/uso público |
| Alta | 8 | Corrigir na primeira janela de manutenção |
| Média | 11 | Corrigir durante a estabilização do produto |
| Baixa / melhoria | — | Recomendações complementares de hardening e evolução |

> **Recomendação:** manter o sistema fora de produção pública até concluir todos os itens críticos e executar novamente a suíte de segurança/QA.

---

## 2. Limites e método utilizado

### Revisado

- Configurações Spring Boot e Flyway;
- Rotas REST, `SecurityConfig`, filtros de autenticação e controladores;
- Autorização por evento, rodada, claim e cartela;
- Mensageria STOMP/SockJS e tópicos de assinatura;
- Sessões administrativas e sessões de cartelas;
- Fluxos de ativação de cartela, claim, aprovação e sorteio;
- Estrutura/constraints do PostgreSQL;
- Upload de imagem, geração de PDF e URLs externas;
- Frontend Vue, armazenamento de tokens, rotas e testes;
- Integridade de build e auditoria npm disponível localmente;
- Conteúdo indevido no pacote distribuído.

### Não validado por falta de acesso ao ambiente real

- Chaves SSH, usuários Linux, `sshd_config`, firewall, Fail2ban e logs;
- Exposição pública de PostgreSQL, Redis, portas de aplicação ou serviços auxiliares;
- Nginx/Apache, TLS, certificados, redirecionamento HTTPS, cabeçalhos na borda;
- Backups, restauração, monitoramento, WAF/CDN, DNS e proteção DDoS;
- Configuração real de variáveis de ambiente e secrets de produção;
- Rastreio de CVEs transitivas do backend Maven em tempo de execução.

---

## 3. Achados críticos — bloquear publicação até corrigir

### C-01 — Superadministrador padrão `admin/admin` é criado automaticamente

**Severidade:** Crítica  
**Evidência:** `bingo-backend/src/main/java/com/bingoai/backend/admin/AdminSeeder.java`, método `run()`.

O sistema cria um usuário `admin`, com senha `admin`, e papel `ROLE_SUPER_ADMIN` caso não o encontre no banco. Em um banco novo, backup restaurado incorretamente ou ambiente implantado sem seed manual, a conta administrativa fica previsível e pública.

**Impacto:** controle total da plataforma: criação de admins, eventos, cartelas, sorteios, aprovação/rejeição de claims e acesso aos dados de todos os eventos.

**Correção obrigatória:**

1. Remover completamente a criação de admin padrão do runtime de produção.
2. Criar um comando administrativo de bootstrap de uso único, protegido por segredo de ambiente e executado manualmente.
3. Exigir senha forte no primeiro acesso e rotação forçada.
4. Invalidar todas as sessões administrativas existentes após a correção.
5. Remover a conta padrão de qualquer banco já criado, se ela existir.

**Critério de aceite:** uma instalação nova não cria nenhum admin sem ação explícita do operador.

---

### C-02 — Token de sessão de jogadores é transmitido publicamente em mensagens de claim

**Severidade:** Crítica  
**Evidência:**

- `game/ClaimController.java`, criação de `BingoClaimMessage` nas linhas aproximadas 112–121, 164–173 e 200–209;
- `game/GameService.java`, auto-claims nas linhas aproximadas 140–152;
- `game/dto/BingoClaimMessage.java`, payload contém `sessionToken`;
- `frontend/src/services/websocket.ts`, clientes se inscrevem em `/topic/events/{eventId}/claims`.

O campo `sessionToken` da cartela é incluído na mensagem STOMP enviada ao tópico de claims do evento. Esse mesmo token é aceito pelo endpoint público `POST /api/v1/claims` como prova de posse da cartela.

**Impacto:** qualquer cliente capaz de assinar ou observar o tópico pode obter a credencial de outro jogador e tentar enviar claims em seu nome. A falha é agravada porque não há autorização de assinatura por tópico no WebSocket.

**Correção obrigatória:**

1. Remover `sessionToken` de `BingoClaimMessage` e de todo payload de broadcast imediatamente.
2. Não reutilizar o token de sessão como identificador exibível ou dado de evento.
3. Alterar mensagens públicas para conter apenas dados mínimos, por exemplo: `claimId`, `serialMascarado`, `cardIndex`, `status`, `automatic`.
4. Criar canal administrativo separado para detalhes internos, protegido por autorização.
5. Revogar/rotacionar todos os tokens de cartela existentes após a correção.

**Critério de aceite:** nenhum payload REST, WebSocket, log, PDF ou endpoint público contém token de sessão de jogador.

---

### C-03 — Qualquer pessoa com o serial consegue reativar uma cartela e receber o token existente

**Severidade:** Crítica  
**Evidência:** `ticket/TicketActivationService.java`, método `activateTicket()`.

A ativação busca a cartela apenas pelo serial. Se ela já possui token, o método retorna o **mesmo token** para quem solicitar novamente o serial. O próprio teste atual valida esse comportamento (`TicketActivationControllerTest`, caso “already activated serial returns success and reuses token”).

O serial tem somente 6 caracteres, composto por 2 letras e 4 números. Mesmo que não seja trivial enumerar tudo em curto prazo, o código pode ser copiado, fotografado, vazado no PDF ou compartilhado. Não há segunda prova de posse, vínculo com dispositivo, expiração ou limitação de tentativas.

**Impacto:** sequestro de sessão de jogador e envio de claim como se fosse o dono da cartela.

**Correção obrigatória:**

1. Tratar o serial como **identificador**, não como autenticação permanente.
2. No primeiro uso, gerar uma credencial de jogador de alta entropia; armazenar somente seu hash no banco.
3. Para recuperar em novo dispositivo, exigir uma segunda prova de posse: QR assinado de uso único, PIN individual impresso, código de ativação secreto ou confirmação por contato cadastrado.
4. Não retornar token existente em reativações.
5. Adicionar expiração, rotação e revogação por evento encerrado.
6. Aplicar rate limiting severo por IP, serial e dispositivo na ativação.
7. Aumentar muito a entropia do serial/PIN caso ele continue sendo parte do fluxo.

**Critério de aceite:** repetir a ativação com apenas o serial não entrega credencial nem acessa uma sessão já vinculada.

---

### C-04 — Falhas de autorização por recurso permitem administração entre eventos de contas diferentes

**Severidade:** Crítica  
**Evidência principal:**

- `event/RoundController.java`: start, finish, cancel e delete de rodada não validam o dono do evento;
- `ticket/TicketController.java`: gerar cartelas e baixar PDF não validam propriedade do evento;
- `ticket/AdminTicketController.java`: busca de cartela por serial não valida a propriedade do evento;
- `game/ClaimController.java`: aprovar/rejeitar claim não verifica que o claim pertence a um evento do admin atual;
- `game/GameController.java`: alteração do delay não verifica que o evento pertence ao admin atual.

A camada `SecurityConfig` verifica somente que o usuário tem `ROLE_ADMIN` ou `ROLE_SUPER_ADMIN`. Porém, a maioria das rotas acima não compara o proprietário do recurso solicitado com o administrador autenticado. Assim, um administrador comum pode atuar em IDs de evento, rodada, claim ou cartela pertencentes a outro administrador.

**Impacto:** leitura, manipulação, cancelamento, geração de PDFs e alteração de resultados entre eventos de organizadores distintos.

**Correção obrigatória:**

1. Centralizar a autorização em serviços reutilizáveis, por exemplo `authorizationService.requireEventOwner(eventId, admin)`.
2. Para cada rota que recebe `roundId`, `ticketId` ou `claimId`, resolver o recurso, obter seu evento e validar o dono antes de qualquer operação.
3. Aplicar a mesma validação em REST e WebSocket.
4. Cobrir com testes de autorização negativa: admin A não pode acessar recurso do admin B.
5. Para reduzir erro humano, usar métodos de repositório já filtrados por `event.admin.id`/`admin.id`, em vez de buscar por ID global e validar apenas depois.

**Critério de aceite:** todo endpoint administrativo possui teste automatizado garantindo retorno `403` para recurso de outro administrador.

---

## 4. Achados de alta severidade

### A-01 — Consulta pública de draws pode iniciar uma rodada

**Severidade:** Alta  
**Evidência:** `game/GameController.java` expõe `GET /api/v1/events/{eventId}/draws` ao público; `game/GameService.java` chama `getOrCreateActiveRound()` em `getRoundDraws()`.

`getRoundDraws()` é uma operação de leitura pública, porém `getOrCreateActiveRound()` muda uma rodada `PENDING` para `IN_PROGRESS`. Logo, abrir a tela do jogador, atualizar a página ou chamar a API pode iniciar a rodada sem ação do administrador.

**Correção:** separar leitura de mutação. Criar `getActiveRoundOrThrow()` sem efeitos colaterais para consultas públicas e manter `startRound()` exclusivamente em endpoint administrativo, com transação e validação de proprietário.

---

### A-02 — Assinaturas WebSocket não têm autorização por evento, ticket ou tipo de tópico

**Severidade:** Alta  
**Evidência:** `common/config/WebSocketConfig.java` apenas tenta montar principal no `CONNECT`; não trata `SUBSCRIBE` nem limita destinos. O broker simples aceita `/topic/**`.

Um cliente pode tentar assinar tópicos de outros eventos, claims, status, conversas e especialmente `/topic/tickets/{ticketId}/overrides`. Este último é descrito como “direcionado”, mas é um tópico global previsível por UUID, não um canal privado de usuário.

**Correção:**

- Adicionar `ChannelInterceptor` para `CONNECT`, `SUBSCRIBE` e `SEND`;
- Validar se o usuário jogador tem acesso ao evento/ticket de cada destino;
- Para dados privados, usar `convertAndSendToUser()` e `/user/queue/...`, vinculados a uma principal de jogador real;
- Recusar assinaturas anônimas para dados de cartela, claims e console administrativo;
- Separar tópicos públicos mínimos (números sorteados) de tópicos privados/administrativos.

---

### A-03 — Chat WebSocket é anônimo, sem limite e sem validação de evento

**Severidade:** Alta  
**Evidência:** `game/GameWebSocketController.java`, método `handleChatMessage()`.

O método aceita mensagem sem `Principal`, sem validação de cartela/evento, sem validação Bean Validation, sem tamanho máximo, sem frequência e sem persistência/moderação. Qualquer cliente conectado pode gerar spam ou inundar canais de chat.

**Correção:** exigir sessão de jogador válida, validar que a cartela pertence ao evento, limitar texto (ex.: 300 caracteres), impor rate limit por sessão/IP, limitar conexões e aplicar bloqueio/mute administrativo. O filtro de palavrões não substitui autenticação, validação ou anti-spam.

---

### A-04 — Recuperação de senha usa código previsível, exibido nos logs e sem proteção contra força bruta

**Severidade:** Alta  
**Evidência:** `admin/AuthController.java`, linhas aproximadas 71–112.

Problemas combinados:

- Usa `new Random()` para gerar código de seis dígitos;
- Exibe o código de recuperação no console/log do servidor;
- Informa que o e-mail não foi encontrado, permitindo enumeração de contas;
- Não limita pedidos por e-mail/IP;
- Não limita tentativas de código;
- Senha mínima permitida é somente 4 caracteres;
- O código é armazenado em texto puro no banco.

**Correção:** usar token criptograficamente seguro de uso único, com hash no banco, expiração curta, limite de tentativas, rate limit, resposta genérica e envio por canal transacional real. Senha mínima recomendada: 12 caracteres, com verificação de senha comprometida e política de bloqueio gradual.

---

### A-05 — Perfil de desenvolvimento e credenciais padrão podem ir para produção; Flyway pode limpar banco em caso de erro

**Severidade:** Alta  
**Evidência:**

- `src/main/resources/application.yml`: `spring.profiles.active: dev`, credenciais padrão e `flyway.clean-on-validation-error: true`;
- `application-dev.yml`: credenciais literais;
- `application-prod.yml`: fallback para `bingo_user` / `bingo_password`.

O arquivo base ativa `dev` por padrão, ativa log SQL e contém credenciais conhecidas. O parâmetro `clean-on-validation-error: true` representa risco destrutivo se esse perfil/configuração for usado em ambiente com dados reais.

**Correção obrigatória:**

1. Remover `spring.profiles.active` do artefato; definir perfil apenas por variável de ambiente/argumento de deploy.
2. Eliminar senhas e URLs padrão de todos os arquivos versionados; usar variáveis obrigatórias sem fallback inseguro.
3. Garantir `spring.flyway.clean-disabled=true` em produção e remover qualquer limpeza automática por erro de validação.
4. Desabilitar SQL detalhado em produção.
5. Bloquear inicialização de produção se secretos essenciais estiverem ausentes.

---

### A-06 — Claims duplicados e disputa de vencedor não estão protegidos no banco

**Severidade:** Alta  
**Evidência:**

- `claims` não possui constraint única em `V5__create_claims_table.sql`;
- `ClaimController.submitClaim()` grava um novo claim sem verificar duplicidade;
- `GameService.checkAndGenerateAutoClaims()` só consulta `exists...` em memória, vulnerável a concorrência.

Dois requests simultâneos podem criar claims duplicados para a mesma cartela/rodada/índice. Não há mecanismo transacional claro que escolha um único vencedor, bloqueie a rodada de forma concorrente ou mantenha audit trail imutável do resultado.

**Correção:**

```sql
ALTER TABLE claims
  ADD CONSTRAINT uq_claim_ticket_round_card
  UNIQUE (ticket_id, round_id, card_index);
```

Além disso, usar lock transacional de rodada (`PESSIMISTIC_WRITE` ou controle otimista com versão), tornar aprovação idempotente e definir regra explícita para empate/múltiplos vencedores.

---

### A-07 — Upload de imagem aceita conteúdo não validado e grava caminho absoluto no banco

**Severidade:** Alta  
**Evidência:** `event/EventService.java`, método `saveHeaderImage()`.

A rotina verifica tamanho, mas não verifica tipo MIME real, assinatura/binário da imagem, dimensões, extensão permitida, decodificação segura ou conteúdo malicioso. O arquivo é salvo com nome derivado do original e o banco recebe `destFile.getAbsolutePath()`.

Consequências:

- Arquivos não-imagem podem ser persistidos;
- Se a pasta for exposta pelo servidor web de forma incorreta, pode haver entrega de conteúdo perigoso;
- Caminho absoluto do servidor é exposto ao frontend por `headerImageUrl`;
- A URL não é uma URL pública válida, portanto o recurso tende a falhar no cliente;
- Não há exclusão do arquivo antigo, antivírus, limite de dimensões ou storage separado.

**Correção:** aceitar apenas JPEG/PNG/WebP após decodificar a imagem com biblioteca segura, reencodar o arquivo, gerar nome aleatório independente do original, armazenar URL relativa/objeto de storage, servir por domínio próprio com `Content-Type` correto e `X-Content-Type-Options: nosniff`, e remover arquivos órfãos.

---

### A-08 — Tokens de admin e jogador ficam em texto puro e no `localStorage`

**Severidade:** Alta  
**Evidência:**

- `AdminSession.token` e `Ticket.sessionToken` persistidos em texto puro;
- `frontend/src/stores/auth.ts` e `frontend/src/stores/game.ts` usam `localStorage`.

Qualquer XSS, extensão maliciosa, computador compartilhado ou vazamento do banco pode resultar em reutilização direta de sessão. O token administrativo expira em 7 dias, mas não há controle de dispositivo, rotação, limite de sessões ou gerenciamento centralizado adequado.

**Correção:**

- Armazenar hash dos tokens no banco, nunca o token original;
- Para sessão administrativa web, priorizar cookie `HttpOnly`, `Secure`, `SameSite` adequado, com proteção CSRF se usar cookies;
- Para jogador, usar token curto e rotacionável, não divulgar em payloads, com expiração vinculada ao evento;
- Criar limpeza de sessões expiradas e revogação por usuário/evento;
- Adicionar cabeçalhos CSP para reduzir superfície de XSS.

---

## 5. Achados de severidade média

### M-01 — Login sem rate limit, bloqueio progressivo ou observabilidade de tentativa

**Evidência:** `AuthController.login()` não limita tentativas.  
**Risco:** força bruta de conta administrativa e credential stuffing.  
**Correção:** rate limit por IP/usuário, atraso progressivo, bloqueio temporário, alerta de login suspeito, logs estruturados sem senha/token.

### M-02 — Controle de estado do evento e rodada aceita valores arbitrários

**Evidência:** `EventService.updateEventStatus()` usa `status.toUpperCase()` sem enum/allowlist; rotas de rodada permitem mudar estado sem máquina de estados.  
**Risco:** evento pode receber estados inválidos ou transições indevidas, inclusive iniciar/finalizar fora da ordem.  
**Correção:** enums, validação explícita e transições permitidas: `AGUARDANDO -> ATIVO -> FINALIZADO`, `PENDING -> IN_PROGRESS -> FINISHED`, com trilha de auditoria.

### M-03 — Alteração de delay não possui limites e não valida proprietário

**Evidência:** `GameController.updateStreamDelay()`.  
**Risco:** atraso negativo/extremo ou manipulação de evento de outro admin.  
**Correção:** validar proprietário e restringir faixa, por exemplo `0..120` segundos.

### M-04 — Exposição pública de `adminUsername` e detalhes completos de evento

**Evidência:** `EventService.mapToResponseDto()` inclui `event.getAdmin().getUsername()` também no endpoint público `GET /events/{id}`.  
**Risco:** privacidade e enumeração de usuários organizadores.  
**Correção:** criar DTO público separado, contendo apenas o necessário ao jogador.

### M-05 — Cabeçalhos de segurança não são configurados

**Evidência:** `SecurityConfig` não define cabeçalhos de hardening.  
**Correção:** configurar pelo menos HSTS em HTTPS, `X-Content-Type-Options: nosniff`, `Referrer-Policy`, `Permissions-Policy`, `frame-ancestors` via CSP e `Cache-Control: no-store` para respostas de autenticação.

### M-06 — Exceções internas são devolvidas ao cliente

**Evidência:** `GlobalExceptionHandler.handleGeneralException()` retorna `ex.getMessage()` e chama `printStackTrace()`; `GameController` também retorna `e.getMessage()`.  
**Risco:** vazamento de estrutura interna, SQL, caminhos e detalhes de implementação.  
**Correção:** registrar erro com ID correlacionado no servidor e responder com mensagem genérica. Nunca retornar stack trace ou mensagem técnica em produção.

### M-07 — Sem validação recursiva de coleções internas dos DTOs

**Evidência:** `CreateEventRequest` possui lista de rodadas, mas sem `@Valid`; o mesmo ocorre com rodadas/prêmios.  
**Risco:** payload pode passar com itens internos inválidos (nomes vazios, padrões inválidos, número de rodada inconsistente).  
**Correção:** adicionar `@Valid` nas coleções e constraints de tamanho/faixa/allowlist.

### M-08 — Serial de cartela tem entropia limitada para um sistema público

**Evidência:** `TicketGenerator.generateSerial()` gera 2 letras e 4 dígitos.  
**Risco:** enumeração fica mais viável sem rate limiting; o serial é também usado como chave de ativação.  
**Correção:** serial maior e semântica apenas de identificador; usar credencial separada com mais de 128 bits de entropia.

### M-09 — Geração de lote não valida existência/propriedade do evento antes de usar referência JPA

**Evidência:** `TicketService.generateBatch()` usa `entityManager.getReference()` e não verifica proprietário.  
**Risco:** erro tardio e, combinado com C-04, emissão de cartelas para evento de terceiro.  
**Correção:** carregar evento por `id + adminId`, validar estado `AGUARDANDO` e impor máximo de quantidade.

### M-10 — Não há constraint de unicidade de número de rodada por evento

**Evidência:** migration de `rounds` não possui `UNIQUE(event_id, round_number)`.  
**Risco:** rodadas duplicadas e comportamento imprevisível do “primeiro” round.  
**Correção:** adicionar constraint única e validar sequência no serviço.

### M-11 — Ausência de checks de domínio no banco

**Evidência:** tabelas usam `VARCHAR` para status, padrão, tipo e campos numéricos sem `CHECK`.  
**Risco:** dados inconsistentes inseridos por bug, script operacional ou integração futura.  
**Correção:** constraints para status, número de bola `1..75`, delay não negativo, `card_index >= 0`, e valores permitidos de padrões.

---

## 6. Qualidade, build e testes

### Resultado das verificações executadas

| Verificação | Resultado | Observação |
|---|---|---|
| `mvn test` | Não executado neste ambiente | Maven não está instalado e o projeto não inclui Maven Wrapper (`mvnw`) |
| Testes Java já presentes em `target/surefire-reports` | 17 testes anteriores sem falhas registradas | Não substitui execução limpa; não cobre auth, IDOR, WebSocket e concorrência de maneira suficiente |
| `vue-tsc --build --force` | **Falhou** | `AdminDashboard.vue` usa `adminUsername`, mas o tipo `EventResponseDto` do frontend não declara a propriedade |
| `vite build` | **Falhou** neste pacote | `node_modules` traz dependência nativa de Windows e não a variante Linux do Rollup; pacote não é reprodutível entre ambientes |
| `npm audit --package-lock-only --omit=dev` | **1 vulnerabilidade alta** | `form-data@4.0.5`, dependência transitiva de `axios@1.16.1`, com correção disponível |

### Falha TypeScript confirmada

`src/components/admin/AdminDashboard.vue` referencia `adminUsername`, enquanto `src/services/api.ts` não inclui essa propriedade na interface `EventResponseDto`.

**Correção:** declarar `adminUsername?: string` no tipo do frontend ou remover o uso do campo caso ele não deva ser entregue à tela. Em seguida, manter backend e frontend sob contrato de API documentado/OpenAPI.

### Problemas no pacote de frontend

- `node_modules` foi enviado junto no ZIP e contém binários específicos de sistema operacional;
- os binários em `node_modules/.bin` vieram sem permissão executável no ambiente analisado;
- o build falha porque a dependência nativa de Rollup para Linux não está presente;
- a entrega inclui `dist`, `node_modules`, `target`, relatórios de teste e upload de imagem.

**Correção:** excluir artefatos gerados do repositório e sempre reconstruir com `npm ci` e `./mvnw test` no ambiente de destino/CI.

### Cobertura de testes insuficiente

Existem cinco arquivos de testes Java e um único arquivo Playwright. Faltam cenários essenciais:

1. Login: credencial inválida, rate limit, logout, expiração e revogação;
2. Recuperação de senha: enumeração, expiração, múltiplas tentativas e reuso;
3. IDOR: admin A tentando operar evento/rodada/cartela/claim do admin B;
4. WebSocket: `CONNECT`, `SUBSCRIBE` e `SEND` não autorizados;
5. Ativação da cartela já ativada por outro cliente;
6. Claim paralelo e duplicado;
7. Consulta pública de draws sem alterar estado;
8. Máquina de estados do evento/rodada;
9. Upload de arquivo não-imagem, MIME falsificado, arquivo grande e imagem com dimensão excessiva;
10. Regressão de contrato frontend/backend e fluxo completo de jogo real.

---

## 7. Dependências e cadeia de suprimentos

### Vulnerabilidade npm identificada

A auditoria local apontou uma vulnerabilidade de severidade **alta** em `form-data@4.0.5`, dependência transitiva de `axios@1.16.1`, relacionada a injeção CRLF em nomes de campos/arquivos multipart.

**Ação:** atualizar Axios e lockfile para uma versão que resolva a dependência transitiva; validar com `npm audit --omit=dev` após `npm ci` limpo. Não aplicar `npm audit fix --force` sem revisão de diff e testes.

### Dependências backend

O projeto usa Spring Boot `3.3.0`, OpenPDF e ZXing. É necessário executar ferramenta de SCA no pipeline em ambiente com Maven disponível, por exemplo OWASP Dependency-Check, Snyk, Dependabot ou GitHub Advanced Security, e gerar SBOM (`CycloneDX`) para acompanhamento contínuo.

---

## 8. Banco de dados — correções recomendadas

### Migração prioritária sugerida

> Ajustar os nomes e validar em staging antes de rodar em produção.

```sql
-- 1) Evita claims duplicados da mesma cartela/rodada/card.
ALTER TABLE claims
  ADD CONSTRAINT uq_claim_ticket_round_card
  UNIQUE (ticket_id, round_id, card_index);

-- 2) Garante número único de rodada no evento.
ALTER TABLE rounds
  ADD CONSTRAINT uq_round_event_number
  UNIQUE (event_id, round_number);

-- 3) Garante faixas de domínio básicas.
ALTER TABLE draws
  ADD CONSTRAINT chk_draw_ball_range CHECK (ball_number BETWEEN 1 AND 75),
  ADD CONSTRAINT chk_draw_sequence_positive CHECK (drawn_sequence > 0);

ALTER TABLE events
  ADD CONSTRAINT chk_stream_delay_nonnegative CHECK (stream_delay_seconds >= 0);

ALTER TABLE claims
  ADD CONSTRAINT chk_claim_card_index_nonnegative CHECK (card_index >= 0);

-- 4) Acelera limpeza/consulta de sessões expiradas.
CREATE INDEX IF NOT EXISTS idx_admin_sessions_expires_at
  ON admin_sessions(expires_at);
```

### Ajustes adicionais de modelagem

- Criar tabela de auditoria imutável de ações administrativas: sorteio, desfazer, início/fim, aprovação/rejeição de claim, override e alteração de evento;
- Armazenar hashes de tokens, não tokens brutos;
- Considerar colunas `created_by`, `updated_by`, `updated_at`, `version` para concorrência otimista;
- Tratar status/padrões como enum ou constraints de banco;
- Definir política de retenção: cartelas, sessões, chats, logs de IP e auditoria;
- Criar role PostgreSQL específica da aplicação, sem privilégios de superusuário, sem `CREATEDB`, `CREATEROLE` ou acesso a outros schemas;
- Não expor PostgreSQL na internet; permitir apenas aplicação e bastion/rede administrativa controlada.

---

## 9. WebSocket e jogo em tempo real — arquitetura recomendada

1. **Separar identidades:** administrador e jogador devem possuir tipos de principal distintos.
2. **Autenticar no handshake/CONNECT:** validar token e carregar usuário/jogador antes de permitir operação.
3. **Autorizar `SUBSCRIBE`:** todo destino deve ser validado por evento/ticket/role.
4. **Autorizar `SEND`:** sorteio, undo, announcement e override apenas para owner/superadmin; chat e claim apenas para jogador da cartela do evento.
5. **Não usar tópico global para dado privado:** trocar `/topic/tickets/{ticketId}/overrides` por `/user/queue/ticket-overrides`.
6. **Evitar broker simples para escala real:** o simple broker é local à instância. Em múltiplas réplicas, as mensagens e assinaturas podem divergir. Avaliar broker relay (RabbitMQ/ActiveMQ) ou infraestrutura gerenciada adequada.
7. **Aplicar limites:** tamanho máximo de frame, limite de conexões/IP, heartbeat, rate limits de `SEND`, limites de assinatura e proteção contra reconexão agressiva.
8. **Auditar cada ação administrativa:** registrar ator, recurso, horário, IP, correlação e antes/depois quando aplicável.

---

## 10. Infraestrutura, SSH e publicação segura

Não há Dockerfile, Nginx/Apache, compose, IaC, script de deploy, `.gitignore` ou configuração SSH no pacote. A configuração real precisa ser validada no servidor antes da publicação.

### Checklist SSH

- Desabilitar login direto de `root`;
- Usar chaves SSH modernas e senha de chave protegida;
- Desabilitar `PasswordAuthentication` após confirmar acesso por chaves;
- Desabilitar autenticação vazia e encaminhamentos que não sejam necessários;
- Restringir acesso SSH a IPs administrativos quando possível;
- Usar usuário de deploy sem privilégios totais; elevação via `sudo` apenas para comandos necessários;
- Atualizações automáticas de segurança ou rotina documentada de patching;
- Fail2ban ou proteção equivalente;
- Logs de autenticação centralizados e alertas de login suspeito;
- Backup testado de chaves e procedimento de recuperação de acesso.

### Checklist servidor/rede

- Expor somente 80/443 publicamente; SSH apenas de origem confiável;
- Manter PostgreSQL somente em `127.0.0.1` ou rede privada; nunca publicar a porta 5432 na internet;
- Proxy reverso com TLS moderno, redirecionamento HTTP -> HTTPS e certificados renováveis;
- Limite de upload no proxy e na aplicação;
- Cabeçalhos de segurança na borda e na aplicação;
- Rotação de logs e monitoramento de disco; uploads podem esgotar armazenamento;
- Backups criptografados e teste regular de restauração;
- Secrets fora do código e fora das imagens/artefatos: variáveis protegidas, secret manager ou cofre;
- Observabilidade: health check, métricas, alertas de erro, auditoria de segurança e dashboard de disponibilidade.

---

## 11. `.gitignore` mínimo recomendado

Criar um `.gitignore` na raiz e também regras específicas quando necessário:

```gitignore
# Secrets / ambiente
.env
.env.*
!.env.example
*.pem
*.key
*.p12
*.pfx
*.jks

# Java / Maven
bingo-backend/target/
bingo-backend/uploads/
*.log

# Node / Vite
bingo-frontend/node_modules/
bingo-frontend/dist/
bingo-frontend/coverage/
bingo-frontend/test-results/
bingo-frontend/playwright-report/

# IDE e SO
.idea/
.vscode/
*.iml
.DS_Store
Thumbs.db
```

> Não versionar `uploads/`, `target/`, `node_modules/`, builds compilados, relatórios de execução ou credenciais. O arquivo `package-lock.json` deve permanecer versionado; `node_modules` não.

---

## 12. Plano de correção priorizado

### Fase P0 — bloqueio imediato (antes de produção)

1. Remover seed de `admin/admin` e rotacionar administradores/sessões já existentes;
2. Remover tokens de jogador de todo WebSocket/REST/log e invalidar tokens atuais;
3. Corrigir reativação de cartela para não devolver token existente;
4. Aplicar validação de dono em todos os recursos de eventos/rodadas/cartelas/claims/delay;
5. Separar leitura pública de draws da lógica que inicia rodada;
6. Bloquear assinaturas e envios WebSocket não autorizados;
7. Desativar `dev` como perfil padrão, remover credenciais default e impedir Flyway clean em produção;
8. Substituir recuperação de senha de mock/log por fluxo seguro;
9. Aplicar constraint de unicidade nos claims e controle transacional de vencedor;
10. Corrigir o contrato TypeScript e fazer build limpo em CI.

### Fase P1 — estabilização (primeira semana)

1. Hardening completo de tokens/sessões e armazenamento seguro no cliente;
2. Validar upload por conteúdo e migrar para storage seguro;
3. Implementar rate limits de login, reset, ativação, claim, chat e STOMP;
4. Criar máquina de estados, enums e constraints do banco;
5. Separar DTOs públicos, administrativos e internos;
6. Adicionar auditoria de sorteios/overrides/claims;
7. Atualizar dependência `form-data` via atualização controlada de Axios/lockfile;
8. Instalar Maven Wrapper e limpar dependências/binaries distribuídos.

### Fase P2 — qualidade e operação (até 30 dias)

1. Pipeline CI com `npm ci`, type-check, build, testes backend, testes E2E e SCA;
2. Testes de segurança de autorização, concorrência e WebSocket;
3. SBOM e dependabot/renovate;
4. Deploy automatizado com rollback, health check e migrações controladas;
5. Revisão de SSH, firewall, TLS, backups e monitoramento;
6. Teste de carga para número esperado de jogadores conectados;
7. Política LGPD: base legal, retenção, privacidade, resposta a incidente e minimização de dados.

---

## 13. Critérios mínimos de liberação

O sistema só deve ser liberado quando todos os pontos abaixo estiverem comprovados em staging:

- [ ] Não existe conta padrão ou credencial padrão;
- [ ] Nenhum token aparece em WebSocket, logs, respostas indevidas ou banco em texto puro;
- [ ] Reativar uma cartela somente pelo serial não concede acesso a sessão existente;
- [ ] Admin A recebe `403` ao operar qualquer recurso do admin B;
- [ ] Jogador não pode iniciar rodada por `GET` e só vê tópicos autorizados;
- [ ] Chat/claims/ativação possuem rate limit e validação de identidade;
- [ ] Claims paralelos não duplicam e a definição do vencedor é consistente;
- [ ] Upload aceita apenas imagens seguras e não expõe caminho absoluto do servidor;
- [ ] `npm ci && npm run build` funciona em ambiente limpo;
- [ ] `./mvnw test` funciona em ambiente limpo;
- [ ] `npm audit --omit=dev` não apresenta vulnerabilidade alta/crítica não tratada;
- [ ] Secrets são obrigatórios, externos ao repositório e não possuem fallback inseguro;
- [ ] PostgreSQL não está exposto publicamente e SSH está endurecido;
- [ ] Backup e restauração foram testados.

---

## 14. Conclusão

O risco principal não está em SQL injection clássico — as consultas observadas são predominantemente JPA/derivadas — e sim em **controle de acesso por recurso, segurança de sessão, WebSocket permissivo e integridade do jogo**. Para um projeto de bingo, esses itens são especialmente sensíveis porque afetam diretamente a confiança no resultado, a privacidade dos participantes e a possibilidade de fraude operacional.

A ordem correta é: **corrigir identidade e autorização**, depois **proteger integridade do jogo e banco**, então **resolver build/testes/dependências**, e somente após isso validar o **servidor real, SSH, TLS, firewall e observabilidade**.
