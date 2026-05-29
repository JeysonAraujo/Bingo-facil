<template>
  <v-app class="game-app">
    <!-- Navbar / Header -->
    <v-app-bar flat class="game-navbar px-4" color="#0b132b" height="70">
      <div class="d-flex align-center w-full justify-between">
        <div class="d-flex align-center gap-3">
          <div class="live-dot-container">
            <span class="live-dot"></span>
            <span class="live-pulse"></span>
          </div>
          <span class="navbar-title">BINGO <span class="accent-text">AI</span> LIVE</span>
        </div>

        <div class="d-flex align-center gap-2">
          <!-- WebSocket connection status badge -->
          <ConnectionStatusBadge class="d-none d-sm-inline-flex" />
          <v-btn
            variant="tonal"
            color="amber-lighten-1"
            class="px-4 rounded-pill font-weight-bold text-capitalize"
            prepend-icon="mdi-help-circle-outline"
            size="small"
            @click="startTour"
          >
            Como Jogar
          </v-btn>
          <v-btn
            variant="text"
            color="grey-lighten-2"
            class="px-3 rounded-pill font-weight-bold text-capitalize"
            size="small"
            @click="handleLogout"
          >
            Sair
          </v-btn>
        </div>
      </div>
    </v-app-bar>

    <v-main class="game-bg">
      <v-container fluid class="pa-4 pa-sm-6 max-w-7xl mx-auto">
        <!-- Main Layout Grid -->
        <v-row class="game-layout-row">
          
          <!-- LEFT COLUMN: Live Stream & Live Activity (Desktop: 7 cols, Mobile: 12 cols) -->
          <v-col cols="12" md="6" lg="7" class="d-flex flex-column gap-4">
            <!-- YouTube Stream Container -->
            <div id="youtube-stream-container" class="youtube-stream-card">
              <div class="iframe-container">
                <iframe
                  src="https://www.youtube.com/embed/S2gB9eGqQic?autoplay=1&mute=1&loop=1&playlist=S2gB9eGqQic"
                  title="Bingo Live Stream"
                  frameborder="0"
                  allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share"
                  allowfullscreen
                ></iframe>
              </div>
              <div class="stream-footer px-4 py-3 d-flex justify-between align-center">
                <div class="d-flex flex-column">
                  <span class="stream-title">Sorteio de Prêmios - Especial de Inauguração</span>
                  <span class="stream-views">
                    <v-icon size="small" class="mr-1">mdi-eye</v-icon>
                    1,240 assistindo agora
                  </span>
                </div>
                <v-chip color="red-darken-2" variant="flat" size="small" class="font-weight-bold">
                  AO VIVO
                </v-chip>
              </div>
            </div>

            <!-- Live Panel: Winning Pattern & Simulated Live Chat -->
            <v-card class="live-panel-card" rounded="xl" elevation="6">
              <v-tabs v-model="activeTab" bg-color="transparent" color="amber-darken-2" grow>
                <v-tab value="chat" class="font-weight-bold">
                  <v-icon start>mdi-chat-processing-outline</v-icon>
                  Chat da Sala
                </v-tab>
                <v-tab value="pattern" class="font-weight-bold">
                  <v-icon start>mdi-grid</v-icon>
                  Padrão da Rodada
                </v-tab>
              </v-tabs>

              <v-window v-model="activeTab" class="live-panel-content">
                <!-- Chat Window -->
                <v-window-item value="chat" class="chat-window">
                  <div class="chat-messages" ref="chatContainer">
                    <div
                      v-for="msg in chatMessages"
                      :key="msg.id"
                      class="chat-message"
                      :class="{ 'system-message': msg.isSystem }"
                    >
                      <template v-if="msg.isSystem">
                        <div class="system-bubble">
                          <v-icon size="small" class="mr-1" color="amber-darken-2">mdi-bell-outline</v-icon>
                          <span>{{ msg.text }}</span>
                        </div>
                      </template>
                      <template v-else>
                        <span class="sender-name">{{ msg.sender }}:</span>
                        <span class="message-text">{{ msg.text }}</span>
                        <span class="message-time">{{ msg.time }}</span>
                      </template>
                    </div>
                  </div>
                  <div class="chat-input-area">
                    <input
                      v-model="userChatMessage"
                      type="text"
                      placeholder="Envie uma mensagem no chat..."
                      @keyup.enter="sendChatMessage"
                      aria-label="Mensagem do chat"
                    />
                    <v-btn icon color="amber-darken-2" size="small" @click="sendChatMessage" aria-label="Enviar mensagem">
                      <v-icon>mdi-send</v-icon>
                    </v-btn>
                  </div>
                </v-window-item>

                <!-- Pattern Window -->
                <v-window-item value="pattern" class="pattern-window d-flex align-center justify-center pa-6">
                  <div class="text-center">
                    <h3 class="pattern-title mb-2">CARTELA CHEIA</h3>
                    <p class="pattern-subtitle mb-4">Marque todos os 24 números para gritar BINGO!</p>
                    <div class="pattern-grid-preview">
                      <div
                        v-for="i in 25"
                        :key="i"
                        class="pattern-cell-preview"
                        :class="{ 'pattern-center': i === 13 }"
                      >
                        <v-icon v-if="i === 13" size="x-small">mdi-star</v-icon>
                      </div>
                    </div>
                  </div>
                </v-window-item>
              </v-window>
            </v-card>
          </v-col>

          <!-- RIGHT COLUMN: Bingo Cards (Desktop: 5 cols, Mobile: 12 cols) -->
          <v-col cols="12" md="6" lg="5" class="d-flex flex-column gap-4 justify-between">
            <div class="right-column-container d-flex flex-column h-full justify-between">
              
              <!-- Cards Area Header -->
              <div class="d-flex justify-between align-center mb-1">
                <div>
                  <h2 class="cards-title">Suas Cartelas</h2>
                  <span class="cards-subtitle">Serial: <span class="serial-code">{{ gameStore.serial }}</span></span>
                </div>
                <div class="auto-marker-badge">
                  <v-icon size="small" class="mr-1">mdi-robot</v-icon>
                  Auto-Marcação Ativa
                </div>
              </div>

              <!-- CARDS AREA CONTAINER -->
              <div id="bingo-cards-container" class="cards-outer-container">
                <!-- DESKTOP BINGO CARDS: 2x2 Grid (Visible on md and up) -->
                <div class="d-none d-md-grid cards-desktop-grid">
                  <BingoCard
                    v-for="(card, cardIdx) in gameStore.ticketMatrix?.cards"
                    :key="cardIdx"
                    :card="card"
                    :cardIndex="cardIdx"
                    :markedNumbers="markedNumbers"
                    :highlighted="activeDesktopCardIndex === cardIdx"
                    @click="activeDesktopCardIndex = cardIdx"
                  />
                </div>

                <!-- MOBILE BINGO CARDS: Carousel with Mini-map (Visible on sm and down) -->
                <div class="d-flex d-md-none flex-column gap-3 w-full">
                  <!-- Mini-map selector -->
                  <div class="mini-map-selector">
                    <button
                      v-for="(card, cardIdx) in gameStore.ticketMatrix?.cards"
                      :key="cardIdx"
                      class="mini-map-dot"
                      :class="{ 'dot-active': activeMobileCardIndex === cardIdx }"
                      @click="activeMobileCardIndex = cardIdx"
                      :aria-label="`Ver Cartela ${cardIdx + 1}`"
                    >
                      <span>{{ cardIdx + 1 }}</span>
                    </button>
                  </div>

                  <!-- Carousel -->
                  <v-carousel
                    v-model="activeMobileCardIndex"
                    :show-arrows="false"
                    hide-delimiters
                    height="auto"
                    class="mobile-carousel"
                  >
                    <v-carousel-item
                      v-for="(card, cardIdx) in gameStore.ticketMatrix?.cards"
                      :key="cardIdx"
                    >
                      <div class="px-2 pb-2">
                        <BingoCard
                          :card="card"
                          :cardIndex="cardIdx"
                          :markedNumbers="markedNumbers"
                          highlighted
                        />
                      </div>
                    </v-carousel-item>
                  </v-carousel>
                </div>
              </div>

              <!-- BINGO CLAIM BUTTON CONTAINER -->
              <div id="bingo-claim-button-container" class="bingo-action-area mt-4">
                <button
                  class="claim-bingo-btn"
                  @click="triggerClaimBingo"
                  aria-label="Gritar Bingo!"
                >
                  <span class="btn-shine"></span>
                  <span class="btn-content">
                    <v-icon size="large" class="mr-2">mdi-trophy-outline</v-icon>
                    GRITAR BINGO!
                  </span>
                </button>
              </div>

            </div>
          </v-col>
        </v-row>
      </v-container>
    </v-main>

    <!-- Onboarding Tour Component -->
    <OnboardingTour
      v-model="showTour"
      @completed="handleTourCompleted"
    />

    <!-- Bingo Claim Celebratory Dialog -->
    <v-dialog v-model="showClaimDialog" max-width="500" persistent rounded="xl">
      <v-card class="claim-dialog-card text-center pa-6" color="#0f172a" theme="dark">
        <!-- Floating CSS Confetti particles -->
        <div class="confetti-container" aria-hidden="true">
          <div v-for="n in 30" :key="n" class="confetti-piece"></div>
        </div>

        <div class="dialog-icon-wrapper mb-4">
          <div class="dialog-pulse-ring"></div>
          <v-icon color="amber-darken-2" size="48" class="trophy-icon">mdi-trophy</v-icon>
        </div>

        <v-card-title class="dialog-title text-wrap">
          🎉 BINGO REIVINDICADO! 🎉
        </v-card-title>

        <v-card-text class="dialog-body pt-2 text-wrap">
          Sua reivindicação de vitória foi enviada com sucesso ao painel da banca organizadora!
          <div class="serial-box mt-4">
            <span class="serial-box-label">Sua Chave de Serial</span>
            <span class="serial-box-code">{{ gameStore.serial }}</span>
          </div>
          <p class="mt-4 text-grey-lighten-1">
            Fique atento à transmissão ao vivo. O apresentador irá validar sua cartela em tempo real!
          </p>
        </v-card-text>

        <v-card-actions class="justify-center mt-2">
          <v-btn
            color="amber-darken-2"
            variant="flat"
            class="px-8 py-2 rounded-pill font-weight-bold text-white text-capitalize"
            size="large"
            @click="showClaimDialog = false"
          >
            Entendido, continuar assistindo
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </v-app>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick, watch, type Ref } from 'vue'
import { useRouter } from 'vue-router'
import { useGameStore } from '../../stores/game'
import BingoCard from './BingoCard.vue'
import OnboardingTour from './OnboardingTour.vue'
import ConnectionStatusBadge from '../shared/ConnectionStatusBadge.vue'

const router = useRouter()
const gameStore = useGameStore()

// View state
const activeTab = ref('chat')
const activeDesktopCardIndex = ref(0)
const activeMobileCardIndex = ref(0)
const userChatMessage = ref('')
const showTour = ref(false)
const showClaimDialog = ref(false)

const chatContainer: Ref<HTMLElement | null> = ref(null)

// Marked numbers (initially empty, can be simulated or loaded in future stories)
const markedNumbers = ref(new Set<number>())

// Onboarding completion key
const ONBOARDING_COMPLETED_KEY = 'bingo_onboarding_completed'

// Simulated chat messaging
interface ChatMsg {
  id: number
  sender: string
  text: string
  time: string
  isSystem?: boolean
}

const chatMessages = ref<ChatMsg[]>([
  { id: 1, sender: 'Aline S.', text: 'Boa sorte a todos!', time: '21:00' },
  { id: 2, sender: 'Carlos M.', text: 'Hoje eu levo esse prêmio! 🏆', time: '21:01' },
  { id: 3, sender: 'Sistema', text: 'Sala de jogo aberta! As cartelas ativadas receberão os sorteios automaticamente.', time: '21:02', isSystem: true }
])

const randomChats = [
  { sender: 'Juliana P.', text: 'Nossa, meu coração está disparado! 💓' },
  { sender: 'Marcos T.', text: 'Só falta o número 75 para o meu bingo!' },
  { sender: 'Fernanda R.', text: 'O locutor é muito rápido kkk' },
  { sender: 'Lucas G.', text: 'Vamo que vamo! 🚀' },
  { sender: 'Roberto S.', text: 'Alguém já tem 20 marcadas?' },
  { sender: 'Amanda L.', text: 'Essa cartela 3 tá com tudo!' },
  { sender: 'Sistema', text: 'Próxima rodada trará prêmios maiores! Fique ligado.', isSystem: true }
]

let chatInterval: number | undefined

// Guard & Onboarding Trigger on Mount
onMounted(() => {
  if (!gameStore.isAuthenticated) {
    router.replace('/activate')
    return
  }

  // Check onboarding status
  const onboardingDone = localStorage.getItem(ONBOARDING_COMPLETED_KEY)
  if (!onboardingDone) {
    showTour.value = true
  }

  // Connect to WebSocket for the active event.
  // Event ID is currently hardcoded as a placeholder until the player session
  // carries the active eventId (wired in Story 3.2 when the admin draw board is live).
  // Using a sentinel value that gracefully degrades if no real event is active.
  const activeEventId = 'placeholder-event-id'
  gameStore.connectToGame(activeEventId)

  // Start simulated chat messages
  chatInterval = window.setInterval(() => {
    const randomIndex = Math.floor(Math.random() * randomChats.length)
    const chatTemplate = randomChats[randomIndex]
    
    const now = new Date()
    const timeStr = `${now.getHours().toString().padStart(2, '0')}:${now.getMinutes().toString().padStart(2, '0')}`
    
    chatMessages.value.push({
      id: Date.now(),
      sender: chatTemplate.sender,
      text: chatTemplate.text,
      time: timeStr,
      isSystem: chatTemplate.isSystem
    })
    
    // Cap chat messages to prevent performance degradation
    if (chatMessages.value.length > 50) {
      chatMessages.value.shift()
    }
    
    scrollToBottom()
  }, 9000) // append message every 9 seconds
})

onUnmounted(() => {
  if (chatInterval) {
    clearInterval(chatInterval)
  }
  // Cleanly disconnect WebSocket when the player leaves the room
  gameStore.disconnectFromGame()
})

function handleLogout() {
  gameStore.clearSession()
  router.replace('/activate')
}

function startTour() {
  showTour.value = true
}

function handleTourCompleted() {
  localStorage.setItem(ONBOARDING_COMPLETED_KEY, 'true')
}

function triggerClaimBingo() {
  showClaimDialog.value = true
}

function sendChatMessage() {
  if (!userChatMessage.value.trim()) return
  
  const now = new Date()
  const timeStr = `${now.getHours().toString().padStart(2, '0')}:${now.getMinutes().toString().padStart(2, '0')}`
  
  chatMessages.value.push({
    id: Date.now(),
    sender: 'Você',
    text: userChatMessage.value,
    time: timeStr
  })
  
  userChatMessage.value = ''
  scrollToBottom()
}

function scrollToBottom() {
  nextTick(() => {
    if (chatContainer.value) {
      chatContainer.value.scrollTop = chatContainer.value.scrollHeight
    }
  })
}

watch(activeTab, (newTab) => {
  if (newTab === 'chat') {
    scrollToBottom()
  }
})
</script>

<style scoped>
.game-app {
  background: #0b132b !important;
}

.game-navbar {
  border-bottom: 1px solid rgba(255, 255, 255, 0.08) !important;
}

.live-dot-container {
  position: relative;
  width: 12px;
  height: 12px;
  margin-right: 4px;
}

.live-dot {
  position: absolute;
  width: 12px;
  height: 12px;
  background-color: #ef4444;
  border-radius: 50%;
  z-index: 2;
}

.live-pulse {
  position: absolute;
  width: 24px;
  height: 24px;
  background-color: rgba(239, 68, 68, 0.4);
  border-radius: 50%;
  top: -6px;
  left: -6px;
  animation: live-glow 1.5s infinite ease-out;
  z-index: 1;
}

@keyframes live-glow {
  0% {
    transform: scale(0.6);
    opacity: 1;
  }
  100% {
    transform: scale(1.6);
    opacity: 0;
  }
}

.navbar-title {
  color: #ffffff;
  font-weight: 900;
  font-size: 1.25rem;
  letter-spacing: 0.1em;
}

.accent-text {
  color: #ffab00;
}

.game-bg {
  background: radial-gradient(circle at top, #1c2541 0%, #0b132b 80%);
  min-height: calc(100vh - 70px);
}

.game-layout-row {
  min-height: calc(100vh - 120px);
}

/* Left Column Styling */
.youtube-stream-card {
  background: #1c2541;
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.3);
}

.iframe-container {
  position: relative;
  width: 100%;
  padding-bottom: 56.25%; /* 16:9 Aspect Ratio */
  height: 0;
  background: #000;
}

.iframe-container iframe {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  border: 0;
}

.stream-footer {
  color: #ffffff;
}

.stream-title {
  font-weight: 700;
  font-size: 0.95rem;
  color: #f8fafc;
}

.stream-views {
  font-size: 0.75rem;
  color: #94a3b8;
  margin-top: 2px;
}

.live-panel-card {
  background: #1c2541 !important;
  color: #ffffff !important;
  border: 1px solid rgba(255, 255, 255, 0.08) !important;
  height: 320px;
  display: flex;
  flex-direction: column;
}

.live-panel-content {
  flex-grow: 1;
  overflow: hidden;
}

/* Chat Styling */
.chat-window {
  display: flex;
  flex-direction: column;
  height: 100%;
  justify-content: space-between;
}

.chat-messages {
  flex-grow: 1;
  overflow-y: auto;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  scroll-behavior: smooth;
}

.chat-message {
  font-size: 0.85rem;
  line-height: 1.4;
  word-break: break-word;
}

.sender-name {
  font-weight: 800;
  color: #ffd600;
  margin-right: 6px;
}

.message-text {
  color: #f1f5f9;
}

.message-time {
  font-size: 0.7rem;
  color: #64748b;
  margin-left: 8px;
}

.system-message {
  align-self: center;
  width: 100%;
  text-align: center;
}

.system-bubble {
  display: inline-flex;
  align-items: center;
  background: rgba(255, 214, 0, 0.08);
  border: 1px solid rgba(255, 214, 0, 0.2);
  color: #ffe082;
  padding: 4px 14px;
  border-radius: 20px;
  font-size: 0.8rem;
  font-weight: 600;
}

.chat-input-area {
  padding: 10px 16px;
  border-top: 1px solid rgba(255, 255, 255, 0.08);
  display: flex;
  gap: 10px;
  align-items: center;
  background: rgba(11, 19, 43, 0.5);
}

.chat-input-area input {
  flex-grow: 1;
  background: #0b132b;
  border: 1px solid rgba(255, 255, 255, 0.15);
  color: #ffffff;
  padding: 8px 16px;
  border-radius: 20px;
  font-size: 0.85rem;
  outline: none;
  transition: border-color 0.2s;
}

.chat-input-area input:focus {
  border-color: #ffd600;
}

/* Pattern Preview Styling */
.pattern-title {
  color: #ffd600;
  font-weight: 900;
  font-size: 1.2rem;
  letter-spacing: 0.05em;
}

.pattern-subtitle {
  font-size: 0.85rem;
  color: #94a3b8;
}

.pattern-grid-preview {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 6px;
  width: 140px;
  margin: 0 auto;
}

.pattern-cell-preview {
  aspect-ratio: 1;
  background: #ffd600;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.pattern-cell-preview.pattern-center {
  background: #ffab00;
  color: #ffffff;
}

/* Right Column & Cards Layout */
.right-column-container {
  height: 100%;
}

.cards-title {
  font-size: 1.4rem;
  font-weight: 800;
  color: #ffffff;
}

.cards-subtitle {
  font-size: 0.85rem;
  color: #94a3b8;
}

.serial-code {
  font-weight: 700;
  color: #ffd600;
  letter-spacing: 0.05em;
}

.auto-marker-badge {
  background: rgba(16, 185, 129, 0.12);
  border: 1px solid rgba(16, 185, 129, 0.3);
  color: #34d399;
  font-size: 0.75rem;
  font-weight: 700;
  padding: 4px 12px;
  border-radius: 20px;
}

.cards-outer-container {
  flex-grow: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-top: 10px;
}

.cards-desktop-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
  width: 100%;
}

/* Mobile Carousel and Dot Indicator */
.mini-map-selector {
  display: flex;
  justify-content: center;
  gap: 12px;
  margin-bottom: 6px;
}

.mini-map-dot {
  width: 38px;
  height: 38px;
  border-radius: 50%;
  background: #1c2541;
  border: 2px solid rgba(255, 255, 255, 0.15);
  color: #94a3b8;
  font-weight: 700;
  font-size: 0.85rem;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
  cursor: pointer;
}

.mini-map-dot.dot-active {
  background: #ffd600;
  color: #0b132b;
  border-color: #ffd600;
  box-shadow: 0 0 10px rgba(255, 214, 0, 0.4);
  transform: scale(1.1);
}

.mobile-carousel {
  border-radius: 20px;
  overflow: visible !important;
}

/* BINGO Button Action Area */
.bingo-action-area {
  width: 100%;
}

.claim-bingo-btn {
  width: 100%;
  position: relative;
  background: linear-gradient(135deg, #ffd600 0%, #ff8f00 100%);
  border: none;
  padding: 18px 0;
  border-radius: 16px;
  font-weight: 900;
  font-size: 1.35rem;
  color: #0b132b;
  letter-spacing: 0.05em;
  cursor: pointer;
  overflow: hidden;
  box-shadow: 0 10px 30px rgba(255, 214, 0, 0.3);
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
  display: flex;
  align-items: center;
  justify-content: center;
  animation: pulse-button 2s infinite;
}

.claim-bingo-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 16px 40px rgba(255, 214, 0, 0.5);
  background: linear-gradient(135deg, #ffeb3b 0%, #ffab00 100%);
}

.claim-bingo-btn:active {
  transform: translateY(1px);
  box-shadow: 0 4px 15px rgba(255, 214, 0, 0.25);
}

.btn-shine {
  position: absolute;
  top: 0;
  left: -100%;
  width: 50%;
  height: 100%;
  background: linear-gradient(
    90deg,
    rgba(255, 255, 255, 0) 0%,
    rgba(255, 255, 255, 0.4) 50%,
    rgba(255, 255, 255, 0) 100%
  );
  transform: skewX(-25deg);
  animation: shine 3s infinite ease-in-out;
}

.btn-content {
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  z-index: 2;
  text-shadow: 0 1px 2px rgba(255, 255, 255, 0.4);
}

@keyframes shine {
  0% { left: -100%; }
  35%, 100% { left: 150%; }
}

@keyframes pulse-button {
  0% { transform: scale(1); }
  50% { transform: scale(1.02); box-shadow: 0 10px 35px rgba(255, 214, 0, 0.45); }
  100% { transform: scale(1); }
}

/* Claim Celebratory Dialog Custom Styles */
.claim-dialog-card {
  border: 2px solid #ffd600 !important;
  box-shadow: 0 25px 50px rgba(255, 214, 0, 0.15) !important;
  position: relative;
  overflow: hidden;
}

.dialog-icon-wrapper {
  position: relative;
  width: 90px;
  height: 90px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: center;
}

.dialog-pulse-ring {
  position: absolute;
  width: 90px;
  height: 90px;
  border: 2px solid #ffd600;
  border-radius: 50%;
  animation: dialog-glow 2s infinite ease-out;
}

.trophy-icon {
  z-index: 2;
  animation: trophy-wobble 4s infinite ease-in-out;
}

@keyframes dialog-glow {
  0% { transform: scale(0.8); opacity: 0.8; }
  100% { transform: scale(1.4); opacity: 0; }
}

@keyframes trophy-wobble {
  0%, 100% { transform: rotate(0deg) scale(1); }
  25% { transform: rotate(-5deg) scale(1.05); }
  75% { transform: rotate(5deg) scale(1.05); }
}

.dialog-title {
  font-size: 1.6rem !important;
  font-weight: 900 !important;
  color: #ffd600 !important;
  letter-spacing: 0.05em;
  text-shadow: 0 4px 10px rgba(0,0,0,0.4);
}

.dialog-body {
  font-size: 1rem !important;
  line-height: 1.6 !important;
}

.serial-box {
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.15);
  border-radius: 12px;
  padding: 10px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.serial-box-label {
  font-size: 0.7rem;
  color: #94a3b8;
  text-transform: uppercase;
  letter-spacing: 0.08em;
}

.serial-box-code {
  font-size: 1.4rem;
  font-weight: 800;
  color: #ffd600;
  letter-spacing: 0.15em;
}

/* CSS Confetti Pieces */
.confetti-container {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  overflow: hidden;
  z-index: 10;
}

.confetti-piece {
  position: absolute;
  width: 8px;
  height: 12px;
  background: #ffd600;
  top: -20px;
  opacity: 0;
  animation: fall 3s infinite linear;
}

.confetti-piece:nth-child(2n) { background: #ff8f00; width: 6px; height: 10px; }
.confetti-piece:nth-child(3n) { background: #22c55e; width: 10px; height: 10px; }
.confetti-piece:nth-child(4n) { background: #3b82f6; width: 8px; height: 8px; }
.confetti-piece:nth-child(5n) { background: #ec4899; width: 12px; height: 6px; }

/* Set up different animations for confetti pieces for variance */
.confetti-piece:nth-child(1) { left: 5%; animation-delay: 0.1s; animation-duration: 2.5s; }
.confetti-piece:nth-child(2) { left: 10%; animation-delay: 0.8s; animation-duration: 3.1s; }
.confetti-piece:nth-child(3) { left: 18%; animation-delay: 0.3s; animation-duration: 2.8s; }
.confetti-piece:nth-child(4) { left: 25%; animation-delay: 1.2s; animation-duration: 3.5s; }
.confetti-piece:nth-child(5) { left: 32%; animation-delay: 0.5s; animation-duration: 2.9s; }
.confetti-piece:nth-child(6) { left: 40%; animation-delay: 1.5s; animation-duration: 3.2s; }
.confetti-piece:nth-child(7) { left: 45%; animation-delay: 0.2s; animation-duration: 2.6s; }
.confetti-piece:nth-child(8) { left: 52%; animation-delay: 1.9s; animation-duration: 3.8s; }
.confetti-piece:nth-child(9) { left: 58%; animation-delay: 0.6s; animation-duration: 3.0s; }
.confetti-piece:nth-child(10) { left: 65%; animation-delay: 1.1s; animation-duration: 2.7s; }
.confetti-piece:nth-child(11) { left: 72%; animation-delay: 0.4s; animation-duration: 3.4s; }
.confetti-piece:nth-child(12) { left: 78%; animation-delay: 1.6s; animation-duration: 3.1s; }
.confetti-piece:nth-child(13) { left: 84%; animation-delay: 0.7s; animation-duration: 2.5s; }
.confetti-piece:nth-child(14) { left: 90%; animation-delay: 1.3s; animation-duration: 2.9s; }
.confetti-piece:nth-child(15) { left: 95%; animation-delay: 0.2s; animation-duration: 3.6s; }
.confetti-piece:nth-child(16) { left: 8%; animation-delay: 2.1s; animation-duration: 2.9s; }
.confetti-piece:nth-child(17) { left: 15%; animation-delay: 2.5s; animation-duration: 3.2s; }
.confetti-piece:nth-child(18) { left: 22%; animation-delay: 1.7s; animation-duration: 2.6s; }
.confetti-piece:nth-child(19) { left: 30%; animation-delay: 2.8s; animation-duration: 3.1s; }
.confetti-piece:nth-child(20) { left: 38%; animation-delay: 2.3s; animation-duration: 3.5s; }
.confetti-piece:nth-child(21) { left: 48%; animation-delay: 1.9s; animation-duration: 2.8s; }
.confetti-piece:nth-child(22) { left: 55%; animation-delay: 2.4s; animation-duration: 3.3s; }
.confetti-piece:nth-child(23) { left: 62%; animation-delay: 2.1s; animation-duration: 3.0s; }
.confetti-piece:nth-child(24) { left: 70%; animation-delay: 2.7s; animation-duration: 2.7s; }
.confetti-piece:nth-child(25) { left: 75%; animation-delay: 1.8s; animation-duration: 3.4s; }
.confetti-piece:nth-child(26) { left: 82%; animation-delay: 2.9s; animation-duration: 3.2s; }
.confetti-piece:nth-child(27) { left: 88%; animation-delay: 2.2s; animation-duration: 2.8s; }
.confetti-piece:nth-child(28) { left: 93%; animation-delay: 2.6s; animation-duration: 3.0s; }
.confetti-piece:nth-child(29) { left: 12%; animation-delay: 3.0s; animation-duration: 3.4s; }
.confetti-piece:nth-child(30) { left: 50%; animation-delay: 3.1s; animation-duration: 2.9s; }

@keyframes fall {
  0% {
    top: -20px;
    transform: translateX(0px) rotate(0deg);
    opacity: 1;
  }
  90% {
    opacity: 1;
  }
  100% {
    top: 105%;
    transform: translateX(25px) rotate(360deg);
    opacity: 0;
  }
}

/* Adjust column layouts min/max sizes to fit viewport nicely */
@media (min-width: 960px) {
  .game-layout-row {
    flex-wrap: nowrap;
  }
}
</style>
