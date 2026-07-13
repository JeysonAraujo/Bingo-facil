# Instructions

- Following Playwright test failed.
- Explain why, be concise, respect Playwright best practices.
- Provide a snippet of code with the fix, if possible.

# Test info

- Name: game-flow.spec.ts >> Bingo-AI E2E Game Flow >> should display the onboarding tour on first load
- Location: tests\game-flow.spec.ts:138:3

# Error details

```
Error: locator.check: Clicking the checkbox did not change its state
Call log:
  - waiting for locator('input[type="checkbox"]')
    - locator resolved to <input value="true" type="checkbox" id="checkbox-v-23" aria-disabled="false" aria-describedby="checkbox-v-23-messages"/>
  - attempting click action
    - scrolling into view if needed
    - done scrolling
    - forcing action
    - performing click action
    - click action done
    - waiting for scheduled navigations to finish
    - navigations have finished

```

# Page snapshot

```yaml
- generic [ref=e1]:
  - generic [ref=e4]:
    - banner [ref=e5]:
      - generic [ref=e6]:
        - generic [ref=e8]: bingo-ai
        - generic [ref=e10]: 🔴 Live - Sincronizado
    - main [ref=e11]:
      - generic [ref=e13]:
        - generic [ref=e16]:
          - generic [ref=e21]: BINGO AI LIVE
          - generic [ref=e22]:
            - 'status "Status da conexão: Desconectado" [ref=e23]':
              - generic [ref=e25]: Desconectado
            - button "Como Jogar" [ref=e26] [cursor=pointer]:
              - generic [ref=e28]: 󰘥
              - generic [ref=e29]: Como Jogar
            - button "Sair" [ref=e30] [cursor=pointer]:
              - generic [ref=e31]: Sair
        - main [ref=e32]:
          - generic [ref=e34]:
            - generic [ref=e35]:
              - generic [ref=e36]:
                - generic [ref=e39]: Aguardando a transmissão começar...
                - generic [ref=e40]:
                  - generic [ref=e41]:
                    - generic [ref=e42]: Sorteio de Prêmios - Bingo AI
                    - generic [ref=e43]:
                      - generic [ref=e44]: 󰈈
                      - text: 0 assistindo agora
                  - generic [ref=e46]: AO VIVO
              - generic [ref=e47]:
                - tablist [ref=e48]:
                  - generic [ref=e50]:
                    - tab "Chat da Sala" [selected] [ref=e51] [cursor=pointer]:
                      - generic [ref=e52]:
                        - generic [ref=e53]: 󱋊
                        - text: Chat da Sala
                    - tab "Padrão da Rodada" [ref=e54] [cursor=pointer]:
                      - generic [ref=e55]:
                        - generic [ref=e56]: 󰋁
                        - text: Padrão da Rodada
                - generic [ref=e59]:
                  - generic [ref=e62]:
                    - generic [ref=e63]: 󰂜
                    - generic [ref=e64]: Sala de jogo aberta! As cartelas ativadas receberão os sorteios automaticamente.
                  - generic [ref=e65]:
                    - textbox "Mensagem do chat" [ref=e66]:
                      - /placeholder: Envie uma mensagem no chat...
                    - button "Enviar mensagem" [ref=e67] [cursor=pointer]:
                      - generic [ref=e69]: 󰒊
            - generic [ref=e71]:
              - generic [ref=e72]:
                - generic [ref=e73]:
                  - heading "Suas Cartelas" [level=2] [ref=e74]
                  - generic [ref=e75]: "Serial: TEST12"
                - generic [ref=e76]:
                  - generic [ref=e77]: 󰚩
                  - text: Auto-Marcação Ativa
              - button "Gritar Bingo!" [disabled] [ref=e80]:
                - generic [ref=e82]:
                  - generic [ref=e83]: 󰔺
                  - text: GRITAR BINGO!
  - generic:
    - dialog:
      - generic [ref=e86]:
        - generic [ref=e87]:
          - generic [ref=e88]: 󰕥
          - heading "Identificação" [level=2] [ref=e89]
        - generic [ref=e90]:
          - paragraph [ref=e91]:
            - text: Para validar sua cartela (
            - strong [ref=e92]: TEST12
            - text: ) e participar, precisamos de algumas informações.
          - generic [ref=e93]:
            - generic [ref=e95]:
              - textbox "Seu Nome Completo" [ref=e97]: João da Silva
              - generic:
                - generic:
                  - generic: Seu Nome Completo
            - alert [ref=e98]
          - generic [ref=e100]:
            - generic [ref=e102]:
              - 'textbox "Telefone (ex: 11999999999)" [ref=e104]': "11988887777"
              - generic:
                - generic:
                  - generic: "Telefone (ex: 11999999999)"
            - alert [ref=e105]
          - generic [ref=e107]:
            - generic [ref=e109]:
              - generic [ref=e111]:
                - generic [ref=e112]: 󰄱
                - checkbox "Concordo em disponibilizar meus dados (Nome e Telefone) apenas para uso de consulta e verificação de autenticidade neste sistema de Bingo. Declaro ciência de que os dados não serão compartilhados com terceiros, de acordo com a LGPD." [ref=e113] [cursor=pointer]
              - generic [ref=e115] [cursor=pointer]: Concordo em disponibilizar meus dados (Nome e Telefone) apenas para uso de consulta e verificação de autenticidade neste sistema de Bingo. Declaro ciência de que os dados não serão compartilhados com terceiros, de acordo com a LGPD.
            - alert [ref=e116]
        - generic [ref=e118]:
          - button "Entrar na Sala" [disabled]:
            - generic: Entrar na Sala
          - button "Voltar" [ref=e119] [cursor=pointer]:
            - generic [ref=e120]: Voltar
```

# Test source

```ts
  47  |               ]
  48  |             }
  49  |           }
  50  |         })
  51  |       });
  52  |     });
  53  | 
  54  |     // Intercept event details API
  55  |     await page.route('**/api/v1/events/mock-event-uuid', async (route) => {
  56  |       await route.fulfill({
  57  |         status: 200,
  58  |         contentType: 'application/json',
  59  |         body: JSON.stringify({
  60  |           success: true,
  61  |           data: {
  62  |             id: 'mock-event-uuid',
  63  |             title: 'Sorteio Especial de Teste E2E',
  64  |             streamDelaySeconds: 0,
  65  |             scheduledAt: new Date(Date.now() + 3600000).toISOString()
  66  |           }
  67  |         })
  68  |       });
  69  |     });
  70  | 
  71  |     // Intercept initial draws
  72  |     await page.route('**/api/v1/events/mock-event-uuid/draws', async (route) => {
  73  |       await route.fulfill({
  74  |         status: 200,
  75  |         contentType: 'application/json',
  76  |         body: JSON.stringify({
  77  |           success: true,
  78  |           data: []
  79  |         })
  80  |       });
  81  |     });
  82  | 
  83  |     // Intercept ticket overrides
  84  |     await page.route('**/api/v1/tickets/search?serial=TEST12', async (route) => {
  85  |       await route.fulfill({
  86  |         status: 200,
  87  |         contentType: 'application/json',
  88  |         body: JSON.stringify({
  89  |           success: true,
  90  |           data: {
  91  |             id: 'mock-ticket-uuid',
  92  |             serial: 'TEST12',
  93  |             markedOverrides: []
  94  |           }
  95  |         })
  96  |       });
  97  |     });
  98  |   });
  99  | 
  100 |   test('should redirect from root to activate page', async ({ page }) => {
  101 |     await page.goto('/');
  102 |     await expect(page).toHaveURL(/.*\/activate/);
  103 |     await expect(page.locator('h1.activation-title')).toHaveText('Ativar Cartela');
  104 |   });
  105 | 
  106 |   test('should activate ticket and redirect to game room, then fill LGPD modal', async ({ page }) => {
  107 |     await page.goto('/activate');
  108 | 
  109 |     // Fill the serial input field
  110 |     const serialInput = page.locator('#serial-input');
  111 |     await serialInput.fill('TEST12');
  112 | 
  113 |     // Click Continuar
  114 |     await page.getByRole('button', { name: /continuar/i }).click();
  115 | 
  116 |     // Wait for redirect to /game
  117 |     await expect(page).toHaveURL(/.*\/game/);
  118 | 
  119 |     // Now we are in GameRoom, the LGPD modal should be visible
  120 |     const nameInput = page.getByLabel('Seu Nome Completo');
  121 |     await expect(nameInput).toBeVisible();
  122 |     await nameInput.fill('João da Silva');
  123 | 
  124 |     const phoneInput = page.getByLabel('Telefone (ex: 11999999999)');
  125 |     await phoneInput.fill('11988887777');
  126 | 
  127 |     const checkbox = page.locator('input[type="checkbox"]');
  128 |     await checkbox.check({ force: true });
  129 | 
  130 |     const submitBtn = page.getByRole('button', { name: /entrar na sala/i }).first();
  131 |     await submitBtn.click();
  132 | 
  133 |     // Verify main landmarks are visible
  134 |     await expect(page.locator('#youtube-stream-container')).toBeVisible();
  135 |     await expect(page.locator('.navbar-title')).toContainText('BINGO AI LIVE');
  136 |   });
  137 | 
  138 |   test('should display the onboarding tour on first load', async ({ page }) => {
  139 |     await page.goto('/activate');
  140 |     await page.locator('#serial-input').fill('TEST12');
  141 |     await page.getByRole('button', { name: /continuar/i }).click();
  142 |     await expect(page).toHaveURL(/.*\/game/);
  143 | 
  144 |     // Fill LGPD Modal because it blocks everything
  145 |     await page.getByLabel('Seu Nome Completo').fill('João da Silva');
  146 |     await page.getByLabel('Telefone (ex: 11999999999)').fill('11988887777');
> 147 |     await page.locator('input[type="checkbox"]').check({ force: true });
      |                                                  ^ Error: locator.check: Clicking the checkbox did not change its state
  148 |     await page.getByRole('button', { name: /entrar na sala/i }).first().click();
  149 | 
  150 |     // Check if the onboarding tour overlay is present
  151 |     const tourOverlay = page.locator('.v-overlay:has-text("Tutorial")');
  152 |     // If onboarding tour exists, verify it or verify step transitions
  153 |     const onboardDialog = page.locator('.onboarding-card, .v-card:has-text("Bem-vindo")');
  154 |     if (await onboardDialog.count() > 0) {
  155 |       await expect(onboardDialog).toBeVisible();
  156 |       // Click next step button if present
  157 |       const nextBtn = onboardDialog.locator('button:has-text("Próximo"), button:has-text("Começar")');
  158 |       if (await nextBtn.count() > 0) {
  159 |         await nextBtn.first().click();
  160 |       }
  161 |     }
  162 |   });
  163 | 
  164 |   test('should respect accessibility standards (WCAG AA)', async ({ page }) => {
  165 |     await page.goto('/activate');
  166 |     await page.locator('#serial-input').fill('TEST12');
  167 |     await page.getByRole('button', { name: /continuar/i }).click();
  168 |     await expect(page).toHaveURL(/.*\/game/);
  169 | 
  170 |     // Verify presence of aria-labels on interactive regions
  171 |     const serialInput = page.locator('#serial-input');
  172 |     if (await serialInput.count() > 0) {
  173 |       await expect(serialInput).toHaveAttribute('aria-label');
  174 |     }
  175 |   });
  176 | });
  177 | 
```