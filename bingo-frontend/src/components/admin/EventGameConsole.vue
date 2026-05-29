<template>
  <v-container fluid class="console-container pa-6">
    <!-- Breadcrumb & Header -->
    <div class="d-flex align-center justify-between mb-6 flex-wrap gap-4">
      <div>
        <v-btn
          variant="text"
          color="grey-lighten-1"
          prepend-icon="mdi-arrow-left"
          class="pl-0 text-capitalize mb-2"
          to="/admin"
        >
          Voltar ao Painel
        </v-btn>
        <h1 class="text-h4 font-weight-bold text-white d-flex align-center">
          <v-icon icon="mdi-gamepad-variant-outline" class="mr-2" color="amber-darken-1"></v-icon>
          Console de Sorteio Ao Vivo
        </h1>
        <p class="text-subtitle-2 text-slate-400 mb-0">
          Gerenciador de rodadas em tempo real • Evento: <span class="text-amber-lighten-2 font-weight-bold">{{ eventTitle }}</span>
        </p>
      </div>

      <div class="d-flex gap-3 align-center">
        <!-- WebSocket Admin connection badge status -->
        <div class="status-indicator d-flex align-center gap-2 px-4 py-2 rounded-pill bg-slate-800 border-slate-700">
          <span class="pulse-dot" :class="{ 'dot--connected': isConnected }"></span>
          <span class="text-caption font-weight-bold text-slate-300">
            Broker: {{ isConnected ? 'Conectado' : 'Conectando…' }}
          </span>
        </div>
      </div>
    </div>

    <!-- Main Grid Content Row -->
    <v-row>
      <!-- LEFT COLUMN: Drawing Board Grid (Cols: 8 on desktop, 12 on mobile) -->
      <v-col cols="12" lg="8" class="d-flex flex-column gap-6">
        <v-card class="bg-slate-900 border-slate-800 rounded-xl pa-6 shadow-2xl relative">
          <!-- Pending Draw Alert Banner -->
          <div v-if="pendingBall !== null" class="pending-alert-banner py-3 px-6 rounded-lg d-flex justify-between align-center mb-4">
            <div class="d-flex align-center gap-3">
              <v-icon icon="mdi-alert-circle-outline" color="amber-darken-2" class="animated-shake"></v-icon>
              <span class="text-subtitle-2 font-weight-bold text-amber-lighten-4">
                Sorteio Pendente: Bola <span class="text-amber font-weight-black text-h6">{{ pendingBall }}</span>
              </span>
            </div>
            <div class="d-flex gap-2">
              <v-btn color="grey-darken-3" size="small" class="font-weight-bold" @click="cancelPending">
                Cancelar (Esc)
              </v-btn>
              <v-btn color="amber-darken-2" size="small" class="font-weight-bold text-slate-950" @click="confirmDraw(pendingBall!)">
                Confirmar (Enter)
              </v-btn>
            </div>
          </div>

          <!-- Board Columns: B I N G O Header -->
          <div class="bingo-header-row mb-4">
            <div v-for="letter in ['B', 'I', 'N', 'G', 'O']" :key="letter" class="bingo-header-cell">
              {{ letter }}
            </div>
          </div>

          <!-- The 75-Key Grid Panel -->
          <div class="bingo-grid-board">
            <div
              v-for="ball in 75"
              :key="ball"
              class="ball-key-cell"
              :class="{
                'key--drawn': isDrawn(ball),
                'key--pending': pendingBall === ball,
                'key--typed-active': typedInput === ball
              }"
              @click="handleBallClick(ball)"
              :title="getBallTitle(ball)"
            >
              <span class="key-number">{{ ball }}</span>
              <span v-if="isDrawn(ball)" class="draw-order">
                {{ getDrawSequenceText(ball) }}
              </span>
            </div>
          </div>

          <!-- Keyboard Hotkeys Indicator Footer -->
          <div class="d-flex align-center justify-between mt-6 text-caption text-slate-400 border-t-slate pa-t-4 flex-wrap gap-2">
            <span class="d-flex align-center gap-1">
              <v-icon icon="mdi-keyboard-outline" size="small" class="mr-1"></v-icon>
              Atalhos de Teclado Ativos:
            </span>
            <div class="d-flex gap-4 flex-wrap">
              <span><kbd class="kbd-key">Digite Nº</kbd> Selecionar Bola</span>
              <span><kbd class="kbd-key">Enter</kbd> Confirmar Sorteio</span>
              <span><kbd class="kbd-key">Esc</kbd> Cancelar</span>
              <span><kbd class="kbd-key">Ctrl + Z</kbd> Desfazer</span>
            </div>
          </div>
        </v-card>
      </v-col>

      <!-- RIGHT COLUMN: Console Operations & History (Cols: 4 on desktop, 12 on mobile) -->
      <v-col cols="12" lg="4" class="d-flex flex-column gap-6">
        <!-- Live Operations Control -->
        <v-card class="bg-slate-900 border-slate-800 rounded-xl pa-6 shadow-2xl">
          <h2 class="text-h6 font-weight-bold text-white mb-4 d-flex align-center">
            <v-icon icon="mdi-tune" class="mr-2" color="indigo-lighten-2"></v-icon>
            Controles Operacionais
          </h2>

          <!-- RED UNDO BUTTON -->
          <button
            class="admin-undo-btn mb-6"
            :disabled="adminStore.drawnConsoleNumbers.length === 0"
            @click="triggerUndo"
          >
            <span class="undo-content">
              <v-icon size="large" class="mr-2">mdi-undo-variant</v-icon>
              DESFAZER ÚLTIMA BOLA
            </span>
            <span v-if="adminStore.drawnConsoleNumbers.length > 0" class="undo-badge-text">
              Excluir Bola {{ lastDrawnBall }}
            </span>
          </button>

          <!-- Latency Sync Slider -->
          <div class="latency-control-card pa-4 rounded-xl bg-slate-950 border-slate-800 mb-2">
            <div class="d-flex justify-between align-center mb-2">
              <span class="text-subtitle-2 font-weight-bold text-slate-200">Ajuste de Latência da Live</span>
              <v-chip color="amber-darken-2" variant="flat" size="small" class="font-weight-black">
                {{ adminStore.latencySeconds }}s
              </v-chip>
            </div>
            <p class="text-caption text-slate-400 mb-4">
              Sincronize as marcações digitais dos jogadores com o atraso de transmissão do YouTube Live.
            </p>
            <v-slider
              v-model="adminStore.latencySeconds"
              :min="0"
              :max="30"
              :step="1"
              color="amber-darken-1"
              track-color="slate-800"
              thumb-size="16"
              hide-details
            ></v-slider>
          </div>
        </v-card>

        <!-- Chronological Drawings History -->
        <v-card class="bg-slate-900 border-slate-800 rounded-xl pa-6 shadow-2xl flex-grow-1 d-flex flex-column h-console-history">
          <div class="d-flex justify-between align-center mb-4">
            <h2 class="text-h6 font-weight-bold text-white d-flex align-center mb-0">
              <v-icon icon="mdi-history" class="mr-2" color="indigo-lighten-2"></v-icon>
              Histórico do Round
            </h2>
            <v-chip color="slate-700" size="small" class="text-slate-300 font-weight-bold">
              {{ adminStore.drawnConsoleNumbers.length }} Bolas
            </v-chip>
          </div>

          <!-- Empty History State -->
          <div v-if="adminStore.drawnConsoleNumbers.length === 0" class="flex-grow-1 d-flex flex-column align-center justify-center py-8 text-center">
            <v-icon icon="mdi-numeric-0-circle-outline" size="48" color="slate-700" class="mb-2"></v-icon>
            <span class="text-subtitle-2 text-slate-500 font-weight-bold">Nenhum número sorteado</span>
            <span class="text-caption text-slate-600">Dê duplo-clique em qualquer bola para iniciar o round.</span>
          </div>

          <!-- History Scroll Area -->
          <div v-else class="history-scroll-area flex-grow-1 pr-2">
            <div
              v-for="(ball, idx) in chronologicalDraws"
              :key="ball"
              class="history-item-row"
            >
              <div class="d-flex align-center gap-3">
                <span class="history-badge-seq">{{ adminStore.drawnConsoleNumbers.length - idx }}º</span>
                <span class="history-badge-ball">{{ getLetterForBall(ball) }}-{{ ball }}</span>
              </div>
              <span class="history-item-time">Sorteado</span>
            </div>
          </div>
        </v-card>
      </v-col>
    </v-row>
  </v-container>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAdminStore } from '@/stores/admin'

const route = useRoute()
const router = useRouter()
const adminStore = useAdminStore()

const eventId = route.params.eventId as string

// Active console state
const pendingBall = ref<number | null>(null)
const typedInput = ref<number | null>(null)
let doubleClickTimer: number | null = null
let keyClearTimer: number | null = null
let typedAccumulator = ''

// Computed properties
const eventTitle = computed(() => adminStore.activeConsoleEvent?.title || 'Carregando…')
const isConnected = computed(() => !!adminStore._wsAdminClient?.connected)

const chronologicalDraws = computed(() => {
  // Return drawnConsoleNumbers in reverse chronological order
  return [...adminStore.drawnConsoleNumbers].reverse()
})

const lastDrawnBall = computed(() => {
  const len = adminStore.drawnConsoleNumbers.length
  if (len === 0) return ''
  const ball = adminStore.drawnConsoleNumbers[len - 1]
  return `${getLetterForBall(ball)}-${ball}`
})

// Mounted lifecycle
onMounted(async () => {
  // Guard eventId
  if (!eventId) {
    router.replace('/admin')
    return
  }

  // Load backend state
  await adminStore.fetchConsoleEventDetails(eventId)
  await adminStore.fetchConsoleDrawnNumbers(eventId)

  // Establish WebSockets
  adminStore.connectAdminWebSocket(eventId)

  // Add global keydown event listener for administrative hotkeys
  window.addEventListener('keydown', handleGlobalKeydown)
})

onUnmounted(() => {
  // Clean up resources
  adminStore.disconnectAdminWebSocket()
  window.removeEventListener('keydown', handleGlobalKeydown)
  
  if (doubleClickTimer) clearTimeout(doubleClickTimer)
  if (keyClearTimer) clearTimeout(keyClearTimer)
})

// Draw core logic
const isDrawn = (ball: number) => {
  return adminStore.drawnConsoleNumbers.includes(ball)
}

const getDrawSequenceText = (ball: number) => {
  const idx = adminStore.drawnConsoleNumbers.indexOf(ball)
  return idx !== -1 ? `${idx + 1}º` : ''
}

const getBallTitle = (ball: number) => {
  if (isDrawn(ball)) return `Bola ${ball} já sorteada`
  if (pendingBall.value === ball) return 'Dê duplo-clique para confirmar o sorteio!'
  return `Clique uma vez para pré-selecionar a bola ${ball}`
}

const getLetterForBall = (ball: number): string => {
  if (ball >= 1 && ball <= 15) return 'B'
  if (ball >= 16 && ball <= 30) return 'I'
  if (ball >= 31 && ball <= 45) return 'N'
  if (ball >= 46 && ball <= 60) return 'G'
  if (ball >= 61 && ball <= 75) return 'O'
  return ''
}

/**
 * Manages ball click lifecycle: turns key flashing yellow on first click,
 * and confirms draw on second click if done within 500ms (Double Click guard).
 */
const handleBallClick = (ball: number) => {
  if (isDrawn(ball)) return

  if (pendingBall.value === ball) {
    // Second click: Confirm draw
    confirmDraw(ball)
  } else {
    // First click: Set pending state
    pendingBall.value = ball
    
    if (doubleClickTimer) clearTimeout(doubleClickTimer)
    doubleClickTimer = window.setTimeout(() => {
      // Clear pending state after 1.5 seconds of inactivity
      if (pendingBall.value === ball) {
        pendingBall.value = null
      }
    }, 1500)
  }
}

/**
 * Triggers the actual draw over STOMP WebSocket draws app prefix.
 */
const confirmDraw = (ball: number) => {
  if (isDrawn(ball)) return

  const nextSequence = adminStore.drawnConsoleNumbers.length + 1
  adminStore.sendDrawBall(eventId, ball, nextSequence)

  // Clear timers & local state
  pendingBall.value = null
  typedInput.value = null
  typedAccumulator = ''
  if (doubleClickTimer) clearTimeout(doubleClickTimer)
}

const cancelPending = () => {
  pendingBall.value = null
  typedInput.value = null
  typedAccumulator = ''
}

const triggerUndo = () => {
  if (adminStore.drawnConsoleNumbers.length === 0) return
  
  const confirmAction = window.confirm('Deseja realmente cancelar e excluir o sorteio da última bola?')
  if (confirmAction) {
    adminStore.sendUndoLastDraw(eventId)
    cancelPending()
  }
}

/**
 * Administrative Global keyboard hotkeys:
 * - Typing 1-75 selects it as pending selection.
 * - Pressing 'Enter' confirms pending ball.
 * - Pressing 'Esc' cancels pending/typed state.
 * - Pressing 'Ctrl+Z' triggers the UNDO operation.
 */
const handleGlobalKeydown = (event: KeyboardEvent) => {
  // If focused on an input element (like sliders or system inputs), bypass hotkeys
  const activeEl = document.activeElement
  if (activeEl && (activeEl.tagName === 'INPUT' || activeEl.tagName === 'TEXTAREA')) {
    return
  }

  // Ctrl+Z: Undo last ball
  if ((event.ctrlKey || event.metaKey) && event.key.toLowerCase() === 'z') {
    event.preventDefault()
    triggerUndo()
    return
  }

  // Esc: Cancel pending
  if (event.key === 'Escape') {
    event.preventDefault()
    cancelPending()
    return
  }

  // Enter: Confirm pending
  if (event.key === 'Enter') {
    event.preventDefault()
    if (pendingBall.value !== null) {
      confirmDraw(pendingBall.value)
    } else if (typedInput.value !== null) {
      confirmDraw(typedInput.value)
    }
    return
  }

  // Numeric Input (0-9): Accumulate typed numbers
  if (event.key >= '0' && event.key <= '9') {
    typedAccumulator += event.key
    const parsed = parseInt(typedAccumulator, 10)

    if (parsed >= 1 && parsed <= 75) {
      typedInput.value = parsed
      pendingBall.value = parsed
    } else {
      // Out of bounds: Reset accumulator
      typedAccumulator = event.key
      const singleDigit = parseInt(typedAccumulator, 10)
      if (singleDigit >= 1 && singleDigit <= 9) {
        typedInput.value = singleDigit
        pendingBall.value = singleDigit
      } else {
        cancelPending()
      }
    }

    // Auto-clear key buffer after 1.5 seconds of inactivity
    if (keyClearTimer) clearTimeout(keyClearTimer)
    keyClearTimer = window.setTimeout(() => {
      typedAccumulator = ''
    }, 1500)
  }
}
</script>

<style scoped>
.console-container {
  background-color: #0b1329;
  min-height: 100vh;
}

.bg-slate-900 {
  background-color: #121c33 !important;
}

.bg-slate-950 {
  background-color: #080f20 !important;
}

.bg-slate-800 {
  background-color: #1a2642 !important;
}

.border-slate-800 {
  border: 1px solid rgba(255, 255, 255, 0.08) !important;
}

.border-slate-700 {
  border: 1px solid rgba(255, 255, 255, 0.15) !important;
}

.text-slate-400 {
  color: #94a3b8;
}

.text-slate-300 {
  color: #cbd5e1;
}

.text-slate-500 {
  color: #64748b;
}

.text-slate-600 {
  color: #475569;
}

/* Status Indicator Dot */
.pulse-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background-color: #f59e0b;
  display: inline-block;
}

.dot--connected {
  background-color: #10b981;
  box-shadow: 0 0 8px rgba(16, 185, 129, 0.6);
  animation: glow-green 2s infinite ease-in-out;
}

@keyframes glow-green {
  0%, 100% { opacity: 1; transform: scale(1); }
  50% { opacity: 0.5; transform: scale(0.8); }
}

/* Pending draw alert banner */
.pending-alert-banner {
  background: rgba(245, 158, 11, 0.15);
  border: 1px solid rgba(245, 158, 11, 0.3);
}

.animated-shake {
  animation: shake 0.5s infinite;
}

@keyframes shake {
  0%, 100% { transform: translateX(0); }
  25% { transform: translateX(-2px); }
  75% { transform: translateX(2px); }
}

/* Bingo header row (B I N G O) */
.bingo-header-row {
  display: grid;
  grid-template-columns: repeat(15, 1fr);
  gap: 8px;
  text-align: center;
}

.bingo-header-cell {
  grid-column: span 3;
  background-color: #1a2642;
  color: #ffffff;
  font-weight: 900;
  font-size: 1.15rem;
  padding: 6px 0;
  border-radius: 8px;
  border: 1px solid rgba(255, 255, 255, 0.08);
}

/* 75-Key Grid Board */
.bingo-grid-board {
  display: grid;
  grid-template-columns: repeat(15, 1fr);
  gap: 8px;
}

.ball-key-cell {
  aspect-ratio: 1;
  background-color: #0b1329;
  border: 1px solid rgba(255, 255, 255, 0.06);
  border-radius: 10px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  user-select: none;
  position: relative;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
}

.ball-key-cell:hover {
  background-color: #1e294b;
  border-color: #3b82f6;
  transform: scale(1.05);
  z-index: 10;
}

.key-number {
  color: #f1f5f9;
  font-weight: 800;
  font-size: 1rem;
}

.draw-order {
  font-size: 0.6rem;
  color: #34d399;
  font-weight: 700;
  margin-top: 2px;
}

/* Key status colors */
.key--drawn {
  background-color: rgba(16, 185, 129, 0.15) !important;
  border: 2px solid #10b981 !important;
  box-shadow: 0 0 10px rgba(16, 185, 129, 0.15);
}

.key--drawn .key-number {
  color: #34d399 !important;
}

.key--pending {
  background-color: rgba(245, 158, 11, 0.18) !important;
  border: 2px dashed #f59e0b !important;
  animation: pending-blink 1s infinite alternate;
}

.key--pending .key-number {
  color: #fbbf24 !important;
}

.key--typed-active {
  background-color: rgba(59, 130, 246, 0.18) !important;
  border: 2px solid #3b82f6 !important;
  transform: scale(1.1);
  z-index: 10;
  box-shadow: 0 0 12px rgba(59, 130, 246, 0.5);
}

.key--typed-active .key-number {
  color: #60a5fa !important;
}

@keyframes pending-blink {
  0% { transform: scale(1); opacity: 1; }
  100% { transform: scale(1.05); opacity: 0.7; }
}

/* Keyboard indicators */
.kbd-key {
  background-color: #1e293b;
  border: 1px solid #475569;
  border-radius: 4px;
  box-shadow: 0 2px 0 #0f172a;
  color: #e2e8f0;
  display: inline-block;
  font-family: monospace;
  font-size: 0.7rem;
  font-weight: 700;
  line-height: 1;
  padding: 2px 6px;
  vertical-align: middle;
}

.border-t-slate {
  border-top: 1px solid rgba(255, 255, 255, 0.08);
}

.pa-t-4 {
  padding-top: 16px;
}

/* RED UNDO BUTTON */
.admin-undo-btn {
  width: 100%;
  position: relative;
  background: linear-gradient(135deg, #ef4444 0%, #991b1b 100%);
  border: none;
  padding: 14px 0;
  border-radius: 12px;
  font-weight: 800;
  font-size: 1rem;
  color: #ffffff;
  letter-spacing: 0.03em;
  cursor: pointer;
  overflow: hidden;
  box-shadow: 0 4px 15px rgba(239, 68, 68, 0.25);
  transition: all 0.2s ease-in-out;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.admin-undo-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(239, 68, 68, 0.4);
  background: linear-gradient(135deg, #f87171 0%, #b91c1c 100%);
}

.admin-undo-btn:active:not(:disabled) {
  transform: translateY(0);
}

.admin-undo-btn:disabled {
  background: #1e293b;
  color: #475569;
  box-shadow: none;
  cursor: not-allowed;
}

.undo-badge-text {
  font-size: 0.7rem;
  color: rgba(255, 255, 255, 0.8);
  font-weight: 600;
  margin-top: 2px;
}

/* History Scroll Area */
.h-console-history {
  height: 380px;
}

.history-scroll-area {
  flex-grow: 1;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.history-item-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: #080f20;
  padding: 8px 12px;
  border-radius: 8px;
  border: 1px solid rgba(255, 255, 255, 0.04);
}

.history-badge-seq {
  font-size: 0.7rem;
  color: #64748b;
  font-weight: bold;
}

.history-badge-ball {
  font-size: 0.9rem;
  color: #f1f5f9;
  font-weight: 800;
}

.history-item-time {
  font-size: 0.65rem;
  color: #10b981;
  font-weight: bold;
}
</style>
