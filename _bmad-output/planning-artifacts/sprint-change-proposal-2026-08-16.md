# Sprint Change Proposal: Correções de Empate e Melhorias no Painel do Administrador (Versão 2)

Este documento propõe alterações corretivas na implementação das regras de desempate e segurança contra envios duplicados, incluindo notificações em tempo real para os jogadores em caso de empate detectado.

---

## 1. Sumário do Problema

Durante a simulação de empate (utilizando o endpoint `/api/test-scenario/tie`), foram identificados os seguintes problemas críticos:
1.  **Erro de Validação de Marcação no Jogador:** O cenário de teste gera o prêmio com o padrão `"[]"`. A switch-case do validador do backend não reconhece `"[]"` como um padrão válido, rejeitando os claims dos jogadores com Bad Request (HTTP 400), o que resulta em erros de interface ("verificar a regra de marcação").
2.  **Envio de Claims Duplicados (Spam):** O jogador pode clicar várias vezes no botão de BINGO, enviando requisições paralelas que sobrecarregam o banco e geram múltiplos claims redundantes, gerando falhas de integridade na aprovação.
3.  **Falta de Indicação Visual de Empate para o Admin:** O console do administrador lista as reivindicações de forma individual, sem explicitar que ocorreu um empate ou qual regra será aplicada.
4.  **Falta de Indicação Visual de Empate para o Jogador:** Quando o jogador bate Bingo, a modal diz apenas que a banca analisará o resultado. O jogador não é notificado se houver empate em tempo real com outras cartelas.
5.  **Impossibilidade de Editar as Regras Pós-criação:** O modal de edição de rodadas no Console de Jogo ignora os campos `tieBreakerRule` e `divisible`, impossibilitando a alteração pelo admin após o bingo ser criado.

---

## 2. Análise de Impacto

*   **Impacto nos Epics:** Sem alteração de escopo macro ou cronograma. São ajustes finos na segurança do BINGO (Story 5.x) e no Admin Console (Story 3.3).
*   **Impacto Técnico:**
    *   Prevenção de spam de claims via verificação de existência no backend (HTTP 409 Conflict) e desabilitação preventiva no frontend.
    *   Monitoramento em tempo real dos eventos de reivindicação (`BINGO_CLAIM`) no player frontend para detectar empates.
    *   Melhoria no painel administrativo para expor a regra ativa e alertar sobre empates pendentes.
    *   Correção de dados no criador de cenário de teste para utilizar `"FULL_CARD"`.

---

## 3. Caminho Recomendado

*   **Abordagem Escolhida:** Ajuste Direto (Direct Adjustment).
*   **Justificativa:** É a opção mais segura e rápida para garantir a integridade do sistema, sem necessidade de alterações estruturais na arquitetura ou banco de dados.

---

## 4. Propostas Detalhadas de Alteração

### Backend (`bingo-backend`)

#### [MODIFY] [TestScenarioController.java](file:///d:/Users/bingo-ai/bingo-backend/src/main/java/com/bingoai/backend/game/TestScenarioController.java)
- Corrigir o padrão de marcação de `"[]"` para `"FULL_CARD"` na linha 39.

#### [MODIFY] [ClaimController.java](file:///d:/Users/bingo-ai/bingo-backend/src/main/java/com/bingoai/backend/game/ClaimController.java)
- Adicionar verificação antes de salvar o claim para evitar duplicidades:
  ```java
  boolean claimExists = claimRepository.existsByRoundIdAndTicketIdAndCardIndex(round.getId(), ticket.getId(), request.getCardIndex());
  if (claimExists) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Você já enviou uma reivindicação para esta cartela nesta rodada.");
  }
  ```

---

### Frontend (`bingo-frontend`)

#### [MODIFY] [game.ts](file:///d:/Users/bingo-ai/bingo-frontend/src/stores/game.ts)
- Adicionar `hasClaimedThisRound` (boolean) e `claimsList` (any[]) no estado.
- Atualizar a ação `claimBingo` para definir `hasClaimedThisRound = true` em caso de sucesso.
- Escutar mensagens `BINGO_CLAIM` no WebSocket e adicionar os claims recebidos ao `claimsList` em tempo real.
- Resetar `hasClaimedThisRound = false` e `claimsList = []` ao conectar ou iniciar nova rodada.

#### [MODIFY] [GameRoom.vue](file:///d:/Users/bingo-ai/bingo-frontend/src/components/player/GameRoom.vue)
- Atualizar a propriedade `:disabled` do botão de BINGO para incluir `gameStore.hasClaimedThisRound`.
- Adicionar um aviso de alerta na modal de reivindicação enviada (`showClaimDialog`), que aparece dinamicamente se `gameStore.claimsList.length > 1`.

#### [MODIFY] [EventGameConsole.vue](file:///d:/Users/bingo-ai/bingo-frontend/src/components/admin/EventGameConsole.vue)
- Atualizar o formulário e modal de rodada para incluir as regras de desempate e campo "Prêmio Divisível".
- Adicionar alertas visuais de empate pendente no topo da fila de claims e na modal de auditoria.

---

## 5. Plano de Handoff

*   **Classificação:** Minor (Execução direta pelo agente Developer).
*   **Critérios de Sucesso:**
    *   Simular o cenário de empate sem erros de validação de marcação.
    *   Garantir que cliques duplicados no botão de BINGO sejam mitigados e rejeitados no backend.
    *   Exibir alertas de empate em tempo real na tela do jogador e na tela do admin.
    *   Permitir edição pós-criação das regras no painel.
