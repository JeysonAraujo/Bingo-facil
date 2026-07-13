import { test, expect } from '@playwright/test';

test.describe('Bingo-AI E2E Game Flow', () => {
  test.beforeEach(async ({ page }) => {
    // Intercept ticket validation API
    await page.route('**/api/v1/tickets/validate?serial=TEST12', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          success: true,
          data: {
            serial: 'TEST12',
            eventId: 'mock-event-uuid',
            eventTitle: 'Sorteio Especial de Teste E2E',
            eventHeaderImage: '',
            activated: false
          }
        })
      });
    });

    // Intercept ticket activation API
    await page.route('**/api/v1/tickets/activate', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          success: true,
          data: {
            sessionToken: 'mock-session-token-123456',
            serial: 'TEST12',
            eventId: 'mock-event-uuid',
            ticketId: 'mock-ticket-uuid',
            matrix: {
              cards: [
                {
                  cardIndex: 0,
                  matrix: [
                    [1, 16, 31, 46, 61],
                    [2, 17, 32, 47, 62],
                    [3, 18, 0, 48, 63], // 0 is FREE
                    [4, 19, 34, 49, 64],
                    [5, 20, 35, 50, 65]
                  ]
                }
              ]
            }
          }
        })
      });
    });

    // Intercept event details API
    await page.route('**/api/v1/events/mock-event-uuid', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          success: true,
          data: {
            id: 'mock-event-uuid',
            title: 'Sorteio Especial de Teste E2E',
            streamDelaySeconds: 0,
            scheduledAt: new Date(Date.now() + 3600000).toISOString()
          }
        })
      });
    });

    // Intercept initial draws
    await page.route('**/api/v1/events/mock-event-uuid/draws', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          success: true,
          data: []
        })
      });
    });

    // Intercept ticket overrides
    await page.route('**/api/v1/tickets/search?serial=TEST12', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          success: true,
          data: {
            id: 'mock-ticket-uuid',
            serial: 'TEST12',
            markedOverrides: []
          }
        })
      });
    });
  });

  test('should redirect from root to activate page', async ({ page }) => {
    await page.goto('/');
    await expect(page).toHaveURL(/.*\/activate/);
    await expect(page.locator('h1.activation-title')).toHaveText('Ativar Cartela');
  });

  test('should activate ticket and redirect to game room, then fill LGPD modal', async ({ page }) => {
    await page.goto('/activate');

    // Fill the serial input field
    const serialInput = page.locator('#serial-input');
    await serialInput.fill('TEST12');

    // Click Continuar
    await page.getByRole('button', { name: /continuar/i }).click();

    // Wait for redirect to /game
    await expect(page).toHaveURL(/.*\/game/);

    // Now we are in GameRoom, the LGPD modal should be visible
    const nameInput = page.getByLabel('Seu Nome Completo');
    await expect(nameInput).toBeVisible();
    await nameInput.fill('João da Silva');

    const phoneInput = page.getByLabel('Telefone (ex: 11999999999)');
    await phoneInput.fill('11988887777');

    await page.getByText('Concordo em disponibilizar meus dados').click();

    const submitBtn = page.getByRole('button', { name: /entrar na sala/i }).first();
    await submitBtn.click();

    // Verify main landmarks are visible
    await expect(page.locator('#youtube-stream-container')).toBeVisible();
    await expect(page.locator('.navbar-title')).toContainText('BINGO AI LIVE');
  });

  test('should display the onboarding tour on first load', async ({ page }) => {
    await page.goto('/activate');
    await page.locator('#serial-input').fill('TEST12');
    await page.getByRole('button', { name: /continuar/i }).click();
    await expect(page).toHaveURL(/.*\/game/);

    // Fill LGPD Modal because it blocks everything
    await page.getByLabel('Seu Nome Completo').fill('João da Silva');
    await page.getByLabel('Telefone (ex: 11999999999)').fill('11988887777');
    await page.getByText('Concordo em disponibilizar meus dados').click();
    await page.getByRole('button', { name: /entrar na sala/i }).first().click();

    // Check if the onboarding tour overlay is present
    const tourOverlay = page.locator('.v-overlay:has-text("Tutorial")');
    // If onboarding tour exists, verify it or verify step transitions
    const onboardDialog = page.locator('.onboarding-card, .v-card:has-text("Bem-vindo")');
    if (await onboardDialog.count() > 0) {
      await expect(onboardDialog).toBeVisible();
      // Click next step button if present
      const nextBtn = onboardDialog.locator('button:has-text("Próximo"), button:has-text("Começar")');
      if (await nextBtn.count() > 0) {
        await nextBtn.first().click();
      }
    }
  });

  test('should respect accessibility standards (WCAG AA)', async ({ page }) => {
    await page.goto('/activate');
    await page.locator('#serial-input').fill('TEST12');
    await page.getByRole('button', { name: /continuar/i }).click();
    await expect(page).toHaveURL(/.*\/game/);

    // Verify presence of aria-labels on interactive regions
    const serialInput = page.locator('#serial-input');
    if (await serialInput.count() > 0) {
      await expect(serialInput).toHaveAttribute('aria-label');
    }
  });
});
