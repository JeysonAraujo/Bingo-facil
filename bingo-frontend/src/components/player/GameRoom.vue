<template>
  <v-app>
    <v-main class="game-bg">
      <v-container class="fill-height" fluid>
        <v-row align="center" justify="center">
          <v-col cols="12" md="10" lg="8">

            <!-- Header da sala de jogo -->
            <div class="game-header">
              <div class="game-header-content">
                <div class="game-status-indicator">
                  <span class="status-dot"></span>
                  <span class="status-text">Sala de Jogo · Ao Vivo</span>
                </div>
                <button class="logout-btn" @click="handleLogout" aria-label="Sair do jogo">
                  Sair
                </button>
              </div>
            </div>

            <!-- Card de boas-vindas -->
            <v-card class="welcome-card" elevation="8" rounded="xl">
              <div class="welcome-header">
                <div class="welcome-icon">🎉</div>
                <h1 class="welcome-title">Cartela Ativada!</h1>
                <p class="welcome-subtitle">Bem-vindo ao Bingo AI</p>
              </div>

              <v-card-text class="welcome-body">

                <!-- Informações da sessão -->
                <div class="session-info">
                  <div class="session-badge">
                    <span class="session-label">Seu Serial</span>
                    <span class="session-serial">{{ gameStore.serial }}</span>
                  </div>
                </div>

                <!-- Preview das cartelas -->
                <div class="cards-preview-section">
                  <h2 class="section-title">Suas 4 Cartelas</h2>
                  <p class="section-desc">
                    Aguarde o início do sorteio. Seus números serão marcados automaticamente conforme as pedras forem sorteadas.
                  </p>

                  <div class="cards-grid" v-if="gameStore.ticketMatrix?.cards?.length">
                    <div
                      v-for="(card, cardIndex) in gameStore.ticketMatrix.cards"
                      :key="cardIndex"
                      class="mini-card"
                    >
                      <div class="mini-card-header">
                        <span v-for="col in ['B','I','N','G','O']" :key="col" class="col-letter">{{ col }}</span>
                      </div>
                      <div class="mini-card-body">
                        <template v-for="row in 5" :key="row">
                          <span class="mini-cell">{{ getCell(card, row - 1, 0) }}</span>
                          <span class="mini-cell">{{ getCell(card, row - 1, 1) }}</span>
                          <span class="mini-cell" :class="{ free: row === 3 }">{{ getCell(card, row - 1, 2) }}</span>
                          <span class="mini-cell">{{ getCell(card, row - 1, 3) }}</span>
                          <span class="mini-cell">{{ getCell(card, row - 1, 4) }}</span>
                        </template>
                      </div>
                      <div class="mini-card-label">Cartela {{ cardIndex + 1 }}</div>
                    </div>
                  </div>
                </div>

                <!-- Status em desenvolvimento -->
                <div class="dev-notice">
                  <v-icon color="amber-darken-2" size="20">mdi-hammer-wrench</v-icon>
                  <span>
                    A sala de jogo completa com vídeo ao vivo, marcação automática e botão BINGO! será implementada na próxima story.
                  </span>
                </div>

              </v-card-text>
            </v-card>

          </v-col>
        </v-row>
      </v-container>
    </v-main>
  </v-app>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useGameStore } from '../../stores/game'
import type { CardMatrixDto } from '../../services/api'

const router = useRouter()
const gameStore = useGameStore()

// Guard: redirect to /activate if no session token
onMounted(() => {
  if (!gameStore.isAuthenticated) {
    router.replace('/activate')
  }
})

function handleLogout() {
  gameStore.clearSession()
  router.replace('/activate')
}

// Helper to get a cell value from a card (column-first storage)
function getCell(card: CardMatrixDto, rowIndex: number, colIndex: number): string | number {
  const columns = [card.b, card.i, card.n, card.g, card.o]
  const col = columns[colIndex]
  if (!col) return ''
  const val = col[rowIndex]
  // Center FREE cell (row 2, col 2 = N column index 2)
  if (rowIndex === 2 && colIndex === 2) return '★'
  return val ?? ''
}
</script>

<style scoped>
.game-bg {
  background: linear-gradient(160deg, #0d1b4b 0%, #1a237e 40%, #0d47a1 100%);
  min-height: 100vh;
}

.game-header {
  padding: 20px 0 16px;
}

.game-header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.game-status-indicator {
  display: flex;
  align-items: center;
  gap: 8px;
}

.status-dot {
  width: 10px;
  height: 10px;
  background: #4CAF50;
  border-radius: 50%;
  animation: pulse-green 1.5s ease-in-out infinite;
}

@keyframes pulse-green {
  0%, 100% { box-shadow: 0 0 0 0 rgba(76, 175, 80, 0.5); }
  50% { box-shadow: 0 0 0 6px rgba(76, 175, 80, 0); }
}

.status-text {
  color: rgba(255, 255, 255, 0.85);
  font-size: 0.9rem;
  font-weight: 500;
}

.logout-btn {
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.3);
  color: white;
  padding: 6px 20px;
  border-radius: 20px;
  font-size: 0.85rem;
  cursor: pointer;
  transition: background 0.2s;
}

.logout-btn:hover {
  background: rgba(255, 255, 255, 0.2);
}

.welcome-card {
  background: rgba(255, 255, 255, 0.97);
  overflow: hidden;
}

.welcome-header {
  background: linear-gradient(135deg, #FFD600 0%, #FFAB00 100%);
  padding: 36px 32px 28px;
  text-align: center;
}

.welcome-icon {
  font-size: 52px;
  margin-bottom: 8px;
}

.welcome-title {
  font-size: 2rem;
  font-weight: 800;
  color: #1A237E;
  margin: 0 0 6px 0;
}

.welcome-subtitle {
  font-size: 1rem;
  color: rgba(26, 35, 126, 0.7);
  margin: 0;
}

.welcome-body {
  padding: 28px 32px 32px !important;
}

.session-info {
  display: flex;
  justify-content: center;
  margin-bottom: 28px;
}

.session-badge {
  display: flex;
  flex-direction: column;
  align-items: center;
  background: #F5F5F5;
  border: 2px solid #E0E0E0;
  border-radius: 12px;
  padding: 12px 24px;
}

.session-label {
  font-size: 0.75rem;
  color: #9E9E9E;
  text-transform: uppercase;
  letter-spacing: 0.1em;
  margin-bottom: 4px;
}

.session-serial {
  font-size: 1.6rem;
  font-weight: 700;
  letter-spacing: 0.25em;
  color: #1A237E;
}

.section-title {
  font-size: 1.1rem;
  font-weight: 700;
  color: #212121;
  margin: 0 0 8px 0;
}

.section-desc {
  font-size: 0.9rem;
  color: #757575;
  margin-bottom: 20px;
  line-height: 1.5;
}

.cards-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

.mini-card {
  background: #F8F9FF;
  border: 2px solid #E8EAF6;
  border-radius: 10px;
  padding: 10px;
  transition: transform 0.2s, box-shadow 0.2s;
}

.mini-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(26, 35, 126, 0.15);
}

.mini-card-header {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 2px;
  margin-bottom: 4px;
}

.col-letter {
  text-align: center;
  font-size: 0.65rem;
  font-weight: 700;
  color: #1A237E;
  padding: 2px 0;
}

.mini-card-body {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 2px;
}

.mini-cell {
  text-align: center;
  font-size: 0.65rem;
  font-weight: 600;
  color: #424242;
  background: white;
  border: 1px solid #E0E0E0;
  border-radius: 3px;
  padding: 3px 1px;
  line-height: 1;
}

.mini-cell.free {
  background: #2E7D32;
  color: white;
  border-color: #2E7D32;
}

.mini-card-label {
  text-align: center;
  font-size: 0.7rem;
  color: #9E9E9E;
  margin-top: 6px;
  font-weight: 600;
}

.dev-notice {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  background: #FFF8E1;
  border: 1px solid #FFE082;
  border-radius: 10px;
  padding: 14px 16px;
  font-size: 0.85rem;
  color: #5D4037;
  line-height: 1.5;
}
</style>
