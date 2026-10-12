# Relatório de Diagnóstico e Plano de Implementação (Atualizado)

## 1. Visão Geral do Cenário

Este documento apresenta a análise de causa raiz, os pontos de impacto e o plano detalhado de implementação para solucionar os apontamentos na aplicação Bingo Fácil:

1. **Aviso de violação de Permissions Policy no terminal/console:** `Permissions policy violation: unload is not allowed in this document. attachEvent @ websocket-...js:9`.
2. **Tratamento de eventos finalizados no painel (Filtro e Ocultação):** Manter a trava de integridade/auditoria que impede a exclusão física de eventos finalizados e implementar no painel do administrador filtros e opções para **ocultar eventos finalizados**, além de desabilitar a ação de exclusão para eventos encerrados.
3. **Bloqueio de sorteios em eventos finalizados:** Impedir que novos números sejam sorteados em eventos com status `FINALIZADO`.
4. **Desconexão do Broker WebSocket e restrição de acesso:** Fechar a conexão STOMP/WebSocket e restringir o acesso à sala quando o evento estiver finalizado.

---

## 2. Diagnóstico e Soluções Definidas

### 2.1. Violação de Permissions Policy no WebSocket (`unload`)
- **Arquivo de Origem:** `bingo-frontend/src/services/websocket.ts`
- **Causa Raiz:** O cliente STOMP utiliza o wrapper `sockjs-client/dist/sockjs.min.js`. Essa biblioteca legada registra ouvintes no evento de janela `unload` (`window.attachEvent('unload', ...)`). Os navegadores modernos baseados em Chromium (Chrome 115+) desativaram o evento `unload` por Permissions Policy em prol de `pagehide` e `beforeunload`, gerando o alerta no console.
- **Solução Arquitetural:** O cliente `@stomp/stompjs` (v7) possui suporte nativo à API RFC 6455 `WebSocket` através de `brokerURL` (ex.: `wss://<host>/ws/bingo/websocket`). Ao conectar diretamente via WebSocket nativo, a dependência do SockJS e o gatilho `unload` deixam de ser executados, eliminando a violação e aumentando o desempenho da conexão.

---

### 2.2. Eventos Finalizados no Painel: Manter Trava e Implementar Filtro/Ocultação
- **Decisão Alinhada:** Conforme solicitado, **manteremos a trava de negócio no backend** que impede a exclusão física de eventos finalizados (`400 Bad Request: Um evento finalizado não pode ser excluído`), preservando a integridade histórica, auditoria e bilhetes gerados.
- **Implementação do Filtro e Experiência do Administrador (`AdminDashboard.vue`):**
  1. **Barra de Filtros Dinâmicos no Dashboard:**
     - Filtro por abas/chips rápidos:
       - **Todos os Eventos**
       - **Ativos / Em Espera (Ocultar Finalizados)** *(padrão ou facilmente selecionável)*
       - **Finalizados**
     - Busca por título do evento.
     - Toggle rápido: "Ocultar eventos finalizados".
  2. **Ação de Ocultar Individualmente:**
     - No card de um evento finalizado, adicionar a ação **"Ocultar do Painel"** (salvando as preferências no armazenamento local do administrador), permitindo remover da visão imediata sem perder o registro.
     - Indicador com botão para "Restaurar eventos ocultos".
  3. **Ajuste no Botão de Excluir:**
     - Para eventos com status `FINALIZADO`, desabilitar o botão de exclusão (ou substituí-lo visualmente pelo botão **"Ocultar"**), exibindo tooltip explicativo: *"Eventos finalizados não podem ser excluídos para preservação de histórico. Use a opção Ocultar."* Isso evita que o administrador dispare uma requisição fadada a erro 400.

---

### 2.3. Sorteio de Bolas em Eventos Finalizados
- **Arquivos de Origem:**
  - Backend: `bingo-backend/src/main/java/com/bingoai/backend/game/GameService.java`
  - Frontend: `bingo-frontend/src/components/admin/EventGameConsole.vue`
- **Causa Raiz:**
  - O método `recordDraw` validava apenas se existia uma rodada com status `IN_PROGRESS`, sem checar `event.getStatus()`.
  - Ao alterar o status do evento para `FINALIZADO` no `EventService.updateEventStatus`, as rodadas vinculadas não eram finalizadas em lote no banco.
  - No frontend (`EventGameConsole.vue`), a interface e os atalhos de teclado de sorteio verificavam apenas `isRoundInProgress`, permitindo cliques caso a rodada anterior tivesse permanecido em aberto.
- **Solução Proposta:**
  - **Backend:** Adicionar checagem estrita de `EventStatus.FINALIZADO` em `GameService.recordDraw`, `GameService.retractLastDraw` e `GameService.getOrCreateActiveRound`.
  - **Backend:** Ao alterar o status do evento para `FINALIZADO`, marcar todas as rodadas pendentes/em andamento como `FINISHED`.
  - **Frontend:** Adicionar propriedade computada `isEventFinalized` em `EventGameConsole.vue`, bloqueando cliques nas bolas, atalhos de teclado (números, Enter), sorteio rápido e exibindo alerta visual de evento encerrado.

---

### 2.4. Desconexão do Broker e Acesso ao Evento Finalizado
- **Arquivos de Origem:**
  - `bingo-frontend/src/components/player/GameRoom.vue`
  - `bingo-frontend/src/stores/game.ts`
  - `bingo-frontend/src/stores/admin.ts`
  - `bingo-frontend/src/components/admin/EventGameConsole.vue`
- **Causa Raiz:**
  - Se um jogador entra no `/room` após o evento já ter sido finalizado, o método `initializeGame` buscava os detalhes do evento, mas prosseguia abrindo o socket STOMP (`connectToGame`).
  - O console administrativo (`EventGameConsole.vue`) não se desconectava automaticamente se o evento estivesse ou passasse para `FINALIZADO`.
- **Solução Proposta:**
  - **No Player (`GameRoom.vue` e `stores/game.ts`):**
    - Se ao carregar `initializeGame` o evento constar como `FINALIZADO`, setar `gameStore.isBingoFinished = true`, chamar `gameStore.disconnectFromGame()` e abortar a abertura do WebSocket.
    - Se o evento for finalizado em tempo real (mensagem `/topic/events/{id}/status`), invocar `disconnectFromGame()` imediatamente, interromper streaming de vídeo e exibir o modal persistente "Bingo Encerrado!".
  - **No Admin (`EventGameConsole.vue` e `stores/admin.ts`):**
    - Ao abrir a tela de um evento `FINALIZADO`, não conectar o broker ou desconectá-lo imediatamente (`disconnectAdminWebSocket()`).
    - Subscrever o console administrativo ao canal de status para desligar o broker se for finalizado em outra aba.

---

## 3. Matriz de Alterações por Arquivo

| Componente | Arquivo | Alteração |
| :--- | :--- | :--- |
| **Backend** | `common/config/WebSocketConfig.java` | Registrar endpoint nativo `/ws/bingo` em conjunto com o endpoint SockJS para suporte a WebSocket puro sem aviso de `unload`. |
| **Backend** | `event/EventService.java` | **Manter a trava contra exclusão** de eventos `FINALIZADO`; ao mudar status para `FINALIZADO`, setar todas as rodadas do evento como `FINISHED`. |
| **Backend** | `game/GameService.java` | Lançar exceção ao tentar sortear (`recordDraw`), desfazer (`retractLastDraw`) ou criar rodada ativa se o evento for `FINALIZADO`. |
| **Frontend** | `services/websocket.ts` | Utilizar WebSocket nativo através de `brokerURL` no `@stomp/stompjs`, removendo a importação direta de `sockjs.min.js`. |
| **Frontend** | `stores/game.ts` | Garantir desconexão do broker e encerramento de timers caso o evento esteja ou passe a ser `FINALIZADO`. |
| **Frontend** | `components/player/GameRoom.vue` | No `initializeGame`, verificar se status é `FINALIZADO`; se sim, desconectar o broker e exibir tela de encerramento sem abrir WebSocket. |
| **Frontend** | `stores/admin.ts` | Adicionar escuta de status em `connectAdminWebSocket` para desconectar o broker admin em caso de evento finalizado; impedir envio de sorteio quando finalizado. |
| **Frontend** | `components/admin/EventGameConsole.vue` | Bloquear grade de sorteio e atalhos se o evento estiver `FINALIZADO`; desconectar broker admin. |
| **Frontend** | `components/admin/AdminDashboard.vue` | Adicionar filtros por status ("Todos", "Ocultar Finalizados", "Finalizados"), opção de ocultar evento finalizado e desabilitar botão de excluir em eventos encerrados. |

---

## 4. Plano de Testes e Validação

1. **Validação do Filtro e Painel:**
   - No `AdminDashboard.vue`, testar o filtro "Ocultar Finalizados" e verificar que a lista exibe apenas eventos em andamento/aguardando.
   - Verificar que no card de eventos finalizados o botão "Excluir" está desativado com explicação ou substituído por "Ocultar", evitando disparos de erro 400.
2. **Validação do Bloqueio de Exclusão (Backend):**
   - Confirmar que chamadas diretas a `DELETE /api/v1/events/{id}` para eventos `FINALIZADO` continuam bloqueadas com mensagem clara de integridade.
3. **Teste de Permissões / Console:**
   - Conectar ao WebSocket e verificar que o aviso `Permissions policy violation: unload is not allowed in this document` não é emitido.
4. **Teste de Bloqueio de Sorteio:**
   - Tentar sortear bola em um evento finalizado (via teclado, clique e requisição direta).
   - Validar que a interface bloqueia os cliques e o backend rejeita com mensagem explícita de erro.
5. **Teste de Desconexão do Broker:**
   - Finalizar o evento e validar se o broker WebSocket do jogador e do console admin mudam para `disconnected`.
   - Acessar a URL da sala `/room` de um evento finalizado e verificar que o modal de encerramento aparece imediatamente sem abrir canal de socket.
