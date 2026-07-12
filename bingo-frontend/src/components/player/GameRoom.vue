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
      <!-- Announcement Ticker -->
      <div v-if="gameStore.activeAnnouncement" class="announcement-ticker-bar">
        <div class="ticker-content">
          <v-icon color="amber-lighten-1" size="small" class="ticker-icon mr-2">mdi-bullhorn-outline</v-icon>
          <div class="marquee-wrapper">
            <span class="marquee-text">{{ gameStore.activeAnnouncement }}</span>
          </div>
          <v-btn
            icon="mdi-close"
            size="x-small"
            variant="text"
            color="grey-lighten-1"
            class="ml-2 close-ticker-btn"
            @click="clearTicker"
            aria-label="Fechar anúncio"
          />
        </div>
      </div>

      <v-container fluid class="pa-4 pa-sm-6 max-w-7xl mx-auto">
        <!-- Main Layout Grid -->
        <v-row class="game-layout-row">
          
          <!-- LEFT COLUMN: Live Stream & Live Activity (Desktop: 7 cols, Mobile: 12 cols) -->
          <v-col cols="12" md="6" lg="7" class="d-flex flex-column gap-4">
            <!-- YouTube Stream Container -->
            <div id="youtube-stream-container" class="youtube-stream-card">
              <div class="iframe-container">
                <iframe
                  v-if="youtubeEmbedUrl"
                  :src="youtubeEmbedUrl"
                  title="Bingo Live Stream"
                  frameborder="0"
                  allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share"
                  allowfullscreen
                ></iframe>
                <div v-else class="d-flex align-center justify-center text-grey-lighten-1 h-full" style="background: #000; position: absolute; top:0; left:0; width:100%; height:100%;">
                  <span>Aguardando a transmissão começar...</span>
                </div>
              </div>
              <div class="stream-footer px-4 py-3 d-flex justify-between align-center">
                <div class="d-flex flex-column">
                  <span class="stream-title">{{ gameStore.eventDetails?.title || 'Sorteio de Prêmios - Bingo AI' }}</span>
                  <span class="stream-views">
                    <v-icon size="small" class="mr-1">mdi-eye</v-icon>
                    {{ gameStore.activeViewers }} assistindo agora
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
                <v-window-item value="pattern" class="pattern-window">
                  <div class="d-flex align-center justify-center pa-6 text-center w-100 h-100">
                    <div>
                      <h3 class="pattern-title mb-2">{{ activeRoundPatternTitle }}</h3>
                      <p class="pattern-subtitle mb-4">{{ activeRoundPatternSubtitle }}</p>
                      <div class="pattern-grid-preview">
                        <div
                          v-for="i in 25"
                          :key="i"
                          class="pattern-cell-preview"
                          :class="{
                            'pattern-center': i === 13,
                            'pattern-highlight': isCellHighlighted(i)
                          }"
                        >
                          <v-icon v-if="i === 13" size="x-small">mdi-star</v-icon>
                        </div>
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
              <div id="bingo-cards-container" class="cards-outer-container" style="position: relative;">
                <!-- Next Round Banner Overlay -->
                <transition name="fade">
                  <div v-if="showNextRoundAlert" class="next-round-overlay d-flex flex-column align-center justify-center text-center pa-6">
                    <v-icon size="48" color="amber-darken-2" class="mb-3 animate-bounce">mdi-timer-sand</v-icon>
                    <h3 class="text-h6 font-weight-bold text-white mb-2">Fique na Sala!</h3>
                    <p class="text-body-2 text-grey-lighten-2 mb-0">A próxima rodada já vai começar!</p>
                  </div>
                </transition>
                <!-- DESKTOP BINGO CARDS: 2x2 Grid (Visible on md and up) -->
                <div class="cards-desktop-grid">
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
                      :class="{
                        'dot-active': activeMobileCardIndex === cardIdx,
                        'dot-flash-green': cardFlashes[cardIdx],
                        'dot-near-win': nearWinCards[cardIdx]
                      }"
                      @click="activeMobileCardIndex = cardIdx"
                      :aria-label="`Ver Cartela ${cardIdx + 1}`"
                    >
                      <span>{{ cardIdx + 1 }}</span>
                      <!-- +1 Bubble -->
                      <transition name="bounce">
                        <span v-if="cardFlashes[cardIdx]" class="plus-one-badge">+1</span>
                      </transition>
                    </button>
                  </div>

                  <!-- Sliding Banner Alert -->
                  <transition name="slide-y">
                    <div
                      v-if="nearWinAlertCardIndex !== null"
                      class="near-win-banner-alert"
                      @click="focusNearWinCard(nearWinAlertCardIndex)"
                    >
                      <v-icon color="#0b132b" class="mr-2">mdi-alert-decagram-outline</v-icon>
                      <span class="banner-text">
                        Card {{ nearWinAlertCardIndex + 1 }} is almost there! Only 1 number left!
                      </span>
                      <v-icon color="#0b132b" class="ml-auto" size="small">mdi-chevron-right</v-icon>
                    </div>
                  </transition>

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
                  ref="claimBingoBtnRef"
                  class="claim-bingo-btn"
                  :disabled="!isBingoButtonUnlocked || claimCooldown || gameStore.isSubmittingClaim"
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

    <!-- Victory Celebration Full Screen Overlay -->
    <v-dialog v-model="gameStore.showVictoryOverlay" fullscreen transition="dialog-bottom-transition" persistent>
      <v-card class="d-flex flex-column align-center justify-center text-white" style="position: relative; overflow: hidden; background-color: #0b1329 !important;">
        <!-- Canvas for high quality confetti -->
        <canvas ref="canvasRef" style="position: absolute; top: 0; left: 0; width: 100%; height: 100%; pointer-events: none; z-index: 10;"></canvas>

        <div class="text-center pa-6 d-flex flex-column align-center justify-center" style="z-index: 20;">
          <!-- Big pulsing trophy -->
          <div class="victory-trophy-ring mb-6">
            <v-icon color="amber-darken-1" size="96" class="victory-trophy-icon">mdi-trophy-award</v-icon>
          </div>

          <h1 class="text-h2 font-weight-black text-amber-accent-3 mb-4 animate-bounce">
            Temos um Vencedor!
          </h1>
          
          <p class="text-h4 font-weight-bold text-white mb-6">
            🎉 PARABÉNS BINGO! 🎉
          </p>

          <v-card class="bg-slate-900 border-slate-800 pa-6 rounded-xl mb-8 shadow-2xl text-center" style="max-width: 500px; background-color: #121c33 !important; border: 1px solid rgba(255,255,255,0.08) !important;">
            <p class="text-subtitle-1 text-slate-300 mb-2">
              Ganhador(es) da rodada:
            </p>
            <div class="text-h3 font-weight-black text-amber-lighten-2 mb-4 tracking-widest uppercase">
              {{ gameStore.winnerSerial }}
            </div>
            <p class="text-subtitle-2 text-slate-400 mb-0">
              completou com sucesso um padrão vencedor e foi validada pela banca!
            </p>
          </v-card>

          <v-btn
            color="amber-darken-2"
            variant="flat"
            size="x-large"
            class="px-10 py-4 font-weight-black rounded-pill text-slate-950 hover-scale-up text-capitalize"
            @click="gameStore.dismissVictory"
          >
            Continuar Jogando
          </v-btn>
        </div>
      </v-card>
    </v-dialog>

    <!-- Bingo Finished Modal (Epic 3) -->
    <v-dialog v-model="gameStore.isBingoFinished" max-width="500" persistent rounded="xl">
      <v-card class="text-center pa-6" color="#0b1329" theme="dark">
        <div class="mb-4">
          <v-icon color="amber-darken-2" size="64">mdi-party-popper</v-icon>
        </div>
        <v-card-title class="text-h5 font-weight-bold text-white mb-2 text-wrap">
          Bingo Encerrado!
        </v-card-title>
        <v-card-text class="text-slate-300 text-wrap">
          Obrigado por participar! Este evento de bingo chegou ao fim. Fique de olho para os próximos eventos.
        </v-card-text>
        <v-card-actions class="justify-center mt-4">
          <v-btn
            color="amber-darken-2"
            variant="flat"
            class="px-8 py-2 rounded-pill font-weight-bold text-slate-950 text-capitalize"
            @click="handleLogout"
          >
            Sair e Voltar ao Início
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- Activation Modal (LGPD, Name, Phone) -->
    <v-dialog v-model="showActivationModal" max-width="500" persistent rounded="xl">
      <v-card class="pa-6" color="#ffffff" theme="light">
        <div class="text-center mb-4">
          <v-icon color="indigo-darken-4" size="48">mdi-shield-check</v-icon>
          <h2 class="text-h5 font-weight-bold mt-2" style="color: #1A237E;">Identificação</h2>
        </div>
        
        <v-card-text class="pa-0 mb-4">
          <p class="text-body-2 text-grey-darken-2 mb-4 text-center">
            Para validar sua cartela (<strong>{{ activationSerial }}</strong>) e participar, precisamos de algumas informações.
          </p>
          
          <v-text-field
            v-model="activationName"
            label="Seu Nome Completo"
            variant="outlined"
            color="indigo-darken-4"
            density="comfortable"
            class="mb-3"
            :disabled="activationLoading"
          ></v-text-field>

          <v-text-field
            v-model="activationPhone"
            label="Telefone (ex: 11999999999)"
            variant="outlined"
            color="indigo-darken-4"
            density="comfortable"
            class="mb-3"
            v-mask="'(##) # ####-####'"
            :disabled="activationLoading"
          ></v-text-field>

          <v-checkbox
            v-model="lgpdAccepted"
            color="indigo-darken-4"
            class="mt-2"
            :disabled="activationLoading"
          >
            <template v-slot:label>
              <span class="text-caption text-grey-darken-2" style="line-height: 1.2;">
                Concordo em disponibilizar meus dados (Nome e Telefone) apenas para uso de consulta e verificação de autenticidade neste sistema de Bingo. Declaro ciência de que os dados não serão compartilhados com terceiros, de acordo com a LGPD.
              </span>
            </template>
          </v-checkbox>
          
          <v-alert
            v-if="activationError"
            type="error"
            variant="tonal"
            class="mt-3 text-caption"
          >
            {{ activationError }}
          </v-alert>
        </v-card-text>

        <v-card-actions class="pa-0 flex-column gap-2">
          <v-btn
            color="indigo-darken-4"
            variant="flat"
            size="large"
            block
            class="font-weight-bold text-capitalize rounded-lg text-white"
            :loading="activationLoading"
            :disabled="!isActivationFormValid"
            @click="submitActivation"
          >
            Entrar na Sala
          </v-btn>
          <v-btn
            variant="text"
            size="small"
            block
            class="text-grey-darken-1 mt-2"
            :disabled="activationLoading"
            @click="router.replace('/activate')"
          >
            Voltar
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </v-app>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, nextTick, watch, type Ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useGameStore } from '../../stores/game'
import BingoCard from './BingoCard.vue'
import OnboardingTour from './OnboardingTour.vue'
import ConnectionStatusBadge from '../shared/ConnectionStatusBadge.vue'
import type { CardMatrixDto } from '../../services/api'

const router = useRouter()
const route = useRoute()
const gameStore = useGameStore()

// Activation Modal Logic
const showActivationModal = ref(false)
const activationSerial = ref('')
const activationName = ref('')
const activationPhone = ref('')
const lgpdAccepted = ref(false)
const activationLoading = ref(false)
const activationError = ref<string | null>(null)

const isActivationFormValid = computed(() => {
  return activationName.value.trim().length > 2 &&
         activationPhone.value.trim().length >= 10 &&
         lgpdAccepted.value
})

async function submitActivation() {
  activationLoading.value = true
  activationError.value = null
  const success = await gameStore.activateTicket(
    activationSerial.value,
    activationName.value,
    activationPhone.value
  )
  activationLoading.value = false
  if (success) {
    showActivationModal.value = false
    await initializeGame()
  } else {
    activationError.value = gameStore.error || 'Falha na ativação da cartela.'
  }
}

async function initializeGame() {
  // Check onboarding status
  const onboardingDone = localStorage.getItem(ONBOARDING_COMPLETED_KEY)
  if (!onboardingDone) {
    showTour.value = true
  }

  const activeEventId = gameStore.eventId || 'placeholder-event-id'
  
  // 0. Recover event details
  await gameStore.fetchEventDetails(activeEventId)
  
  // 1. Recover chronological history from database
  await gameStore.fetchInitialDraws(activeEventId)

  // 1.5. Recover current ticket overrides from database if serial is present
  if (gameStore.serial) {
    await gameStore.fetchTicketOverrides(gameStore.serial)
  }
  
  // 2. Open WebSocket channel for real-time updates
  gameStore.connectToGame(
    activeEventId,
    (chatMsg: any) => {
      const timeStr = new Date(chatMsg.timestamp).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
      chatMessages.value.push({
        id: Date.now() + Math.random(),
        sender: chatMsg.sender,
        text: chatMsg.text,
        time: timeStr
      })
      if (chatMessages.value.length > 50) {
        chatMessages.value.shift()
      }
      scrollToBottom()
    },
    (roundMsg: any) => {
      if (roundMsg.status === 'FINISHED') {
        if (hasPendingRounds.value) {
          showNextRoundAlert.value = true
        }
        chatMessages.value.push({
          id: Date.now(),
          sender: 'Sistema',
          text: `Rodada #${roundMsg.roundNumber} finalizada!`,
          time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
          isSystem: true
        })
        scrollToBottom()
      } else if (roundMsg.status === 'IN_PROGRESS') {
        showNextRoundAlert.value = false
        chatMessages.value.push({
          id: Date.now(),
          sender: 'Sistema',
          text: `Rodada #${roundMsg.roundNumber} iniciada! Boa sorte!`,
          time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
          isSystem: true
        })
        scrollToBottom()
      }
    }
  )

  // Initialize previous marked counts for off-screen flashes
  const cards = gameStore.ticketMatrix?.cards || []
  prevMarkedCounts.value = cards.map((card) => getCardMarkedCount(card))
}

// Victory celebration logic
const canvasRef = ref<HTMLCanvasElement | null>(null)
let animationFrameId: number | null = null

interface ConfettiParticle {
  x: number
  y: number
  size: number
  color: string
  speedX: number
  speedY: number
  rotation: number
  rotationSpeed: number
}

const colors = ['#f59e0b', '#10b981', '#3b82f6', '#ec4899', '#8b5cf6', '#ef4444']

const playSynthesizedChime = () => {
  try {
    const audioCtx = new (window.AudioContext || (window as any).webkitAudioContext)()
    const playNote = (freq: number, start: number, duration: number) => {
      const osc = audioCtx.createOscillator()
      const gainNode = audioCtx.createGain()
      osc.type = 'sine'
      osc.frequency.setValueAtTime(freq, start)
      gainNode.gain.setValueAtTime(0.1, start)
      gainNode.gain.exponentialRampToValueAtTime(0.0001, start + duration)
      osc.connect(gainNode)
      gainNode.connect(audioCtx.destination)
      osc.start(start)
      osc.stop(start + duration)
    }
    const now = audioCtx.currentTime
    playNote(523.25, now, 0.4) // C5
    playNote(659.25, now + 0.1, 0.4) // E5
    playNote(783.99, now + 0.2, 0.4) // G5
    playNote(1046.50, now + 0.3, 0.8) // C6
  } catch (e) {
    console.error('Web Audio API chime failed:', e)
  }
}

const triggerVictorySound = () => {
  const audio = new Audio('/victory.mp3')
  audio.play().catch(() => {
    // Fail-safe programmatic chime if file is missing or blocked
    playSynthesizedChime()
  })
}

const startConfetti = () => {
  const canvas = canvasRef.value
  if (!canvas) return
  const ctx = canvas.getContext('2d')
  if (!ctx) return

  canvas.width = window.innerWidth
  canvas.height = window.innerHeight

  const particles: ConfettiParticle[] = []
  
  // Spawn initial burst
  for (let i = 0; i < 150; i++) {
    particles.push({
      x: canvas.width / 2 + (Math.random() - 0.5) * 50,
      y: canvas.height + 20,
      size: Math.random() * 8 + 6,
      color: colors[Math.floor(Math.random() * colors.length)],
      speedX: (Math.random() - 0.5) * 15,
      speedY: -Math.random() * 15 - 10,
      rotation: Math.random() * 360,
      rotationSpeed: (Math.random() - 0.5) * 10
    })
  }

  const animate = () => {
    ctx.clearRect(0, 0, canvas.width, canvas.height)
    
    if (particles.length < 100 && Math.random() < 0.4) {
      particles.push({
        x: Math.random() * canvas.width,
        y: -10,
        size: Math.random() * 8 + 6,
        color: colors[Math.floor(Math.random() * colors.length)],
        speedX: (Math.random() - 0.5) * 4,
        speedY: Math.random() * 5 + 2,
        rotation: Math.random() * 360,
        rotationSpeed: (Math.random() - 0.5) * 5
      })
    }

    for (let i = particles.length - 1; i >= 0; i--) {
      const p = particles[i]
      p.x += p.speedX
      p.y += p.speedY
      p.rotation += p.rotationSpeed
      p.speedY += 0.15
      p.speedX *= 0.98

      ctx.save()
      ctx.translate(p.x, p.y)
      ctx.rotate((p.rotation * Math.PI) / 180)
      ctx.fillStyle = p.color
      ctx.fillRect(-p.size / 2, -p.size / 2, p.size, p.size)
      ctx.restore()

      if (p.y > canvas.height + 50 || p.x < -50 || p.x > canvas.width + 50) {
        particles.splice(i, 1)
      }
    }

    animationFrameId = requestAnimationFrame(animate)
  }

  animate()
}

const stopConfetti = () => {
  if (animationFrameId) {
    cancelAnimationFrame(animationFrameId)
    animationFrameId = null
  }
}

watch(
  () => gameStore.showVictoryOverlay,
  (show) => {
    if (show) {
      triggerVictorySound()
      nextTick(() => {
        startConfetti()
      })
    } else {
      stopConfetti()
    }
  }
)

onUnmounted(() => {
  stopConfetti()
})

// View state
const activeTab = ref('chat')
const activeDesktopCardIndex = ref(0)
const activeMobileCardIndex = ref(0)
const userChatMessage = ref('')
const showTour = ref(false)
const showClaimDialog = ref(false)

const chatContainer: Ref<HTMLElement | null> = ref(null)

// Marked numbers computed from both drawn numbers and forced coordinator overrides
const markedNumbers = computed(() => {
  const union = new Set<number>()
  gameStore.drawnNumbers.forEach((n) => union.add(n))
  gameStore.overriddenNumbers.forEach((n) => union.add(n))
  return union
})

const claimBingoBtnRef = ref<HTMLElement | null>(null)

function getCardCellValue(card: any, row: number, col: number): number {
  const columnKeys = ['b', 'i', 'n', 'g', 'o']
  const key = columnKeys[col]
  return card[key] ? card[key][row] : 0
}

function isCardCellMarked(card: any, row: number, col: number): boolean {
  if (row === 2 && col === 2) return true // FREE space is always marked!
  const val = getCardCellValue(card, row, col)
  return markedNumbers.value.has(val)
}

function checkCardLine(card: any): boolean {
  for (let row = 0; row < 5; row++) {
    let rowMarked = true
    for (let col = 0; col < 5; col++) {
      if (!isCardCellMarked(card, row, col)) {
        rowMarked = false
        break
      }
    }
    if (rowMarked) return true
  }
  return false
}

function checkCardColumn(card: any): boolean {
  for (let col = 0; col < 5; col++) {
    let colMarked = true
    for (let row = 0; row < 5; row++) {
      if (!isCardCellMarked(card, row, col)) {
        colMarked = false
        break
      }
    }
    if (colMarked) return true
  }
  return false
}

function checkCardDiagonal(card: any): boolean {
  let mainDiag = true
  for (let i = 0; i < 5; i++) {
    if (!isCardCellMarked(card, i, i)) {
      mainDiag = false
      break
    }
  }
  if (mainDiag) return true

  let antiDiag = true
  for (let i = 0; i < 5; i++) {
    if (!isCardCellMarked(card, i, 4 - i)) {
      antiDiag = false
      break
    }
  }
  return antiDiag
}

function checkCardOuterBorder(card: any): boolean {
  // Row 0
  for (let col = 0; col < 5; col++) {
    if (!isCardCellMarked(card, 0, col)) return false
  }
  // Row 4
  for (let col = 0; col < 5; col++) {
    if (!isCardCellMarked(card, 4, col)) return false
  }
  // Col 0 (rows 1..3)
  for (let row = 1; row <= 3; row++) {
    if (!isCardCellMarked(card, row, 0)) return false
  }
  // Col 4 (rows 1..3)
  for (let row = 1; row <= 3; row++) {
    if (!isCardCellMarked(card, row, 4)) return false
  }
  return true
}

function checkCardInnerBorder(card: any): boolean {
  // Row 1 (cols 1..3)
  if (!isCardCellMarked(card, 1, 1)) return false
  if (!isCardCellMarked(card, 1, 2)) return false
  if (!isCardCellMarked(card, 1, 3)) return false

  // Row 2 (cols 1, 3)
  if (!isCardCellMarked(card, 2, 1)) return false
  if (!isCardCellMarked(card, 2, 3)) return false

  // Row 3 (cols 1..3)
  if (!isCardCellMarked(card, 3, 1)) return false
  if (!isCardCellMarked(card, 3, 2)) return false
  if (!isCardCellMarked(card, 3, 3)) return false

  return true
}

function checkCardFullCard(card: any): boolean {
  for (let row = 0; row < 5; row++) {
    for (let col = 0; col < 5; col++) {
      if (!isCardCellMarked(card, row, col)) {
        return false
      }
    }
  }
  return true
}

function doesCardMatchPatterns(card: any, patterns: string[]): boolean {
  if (!patterns || patterns.length === 0) {
    return checkCardFullCard(card)
  }
  for (const pattern of patterns) {
    const pat = pattern.toUpperCase()
    if ((pat === 'LINE' || pat === 'ANY_LINE') && checkCardLine(card)) return true
    if ((pat === 'COLUMN' || pat === 'ANY_COLUMN') && checkCardColumn(card)) return true
    if (pat === 'DIAGONAL' && checkCardDiagonal(card)) return true
    if ((pat === 'OUTER_BORDER' || pat === 'JANELÃO' || pat === 'JANELAO') && checkCardOuterBorder(card)) return true
    if ((pat === 'INNER_BORDER' || pat === 'JANELINHA') && checkCardInnerBorder(card)) return true
    if ((pat === 'FULL_CARD' || pat === 'FULL_GRID' || pat === 'BINGO_CHEIO') && checkCardFullCard(card)) return true
  }
  return false
}

const activeRoundPatterns = computed(() => {
  if (!gameStore.eventDetails || !gameStore.eventDetails.rounds) return []
  const activeRound = gameStore.eventDetails.rounds.find((r: any) => r.status === 'IN_PROGRESS')
  if (!activeRound || !activeRound.prizes) return []
  return activeRound.prizes.map((p: any) => p.drawPattern)
})

const activeRoundPatternTitle = computed(() => {
  const patterns = activeRoundPatterns.value
  if (!patterns || patterns.length === 0) return 'CARTELA CHEIA'
  const pat = patterns[0].toUpperCase()
  switch (pat) {
    case 'LINE':
    case 'ANY_LINE':
      return 'MARCAÇÃO LINHA'
    case 'COLUMN':
    case 'ANY_COLUMN':
      return 'MARCAÇÃO COLUNA'
    case 'DIAGONAL':
      return 'MARCAÇÃO DIAGONAL'
    case 'OUTER_BORDER':
    case 'JANELÃO':
    case 'JANELAO':
      return 'JANELÃO (OUTER BORDER)'
    case 'INNER_BORDER':
    case 'JANELINHA':
      return 'JANELINHA (INNER BORDER)'
    case 'FULL_CARD':
    case 'FULL_GRID':
    case 'BINGO_CHEIO':
    default:
      return 'CARTELA CHEIA'
  }
})

const activeRoundPatternSubtitle = computed(() => {
  const patterns = activeRoundPatterns.value
  if (!patterns || patterns.length === 0) return 'Marque todos os 24 números para gritar BINGO!'
  const pat = patterns[0].toUpperCase()
  switch (pat) {
    case 'LINE':
    case 'ANY_LINE':
      return 'Marque qualquer linha horizontal de 5 números para gritar BINGO!'
    case 'COLUMN':
    case 'ANY_COLUMN':
      return 'Marque qualquer coluna vertical de 5 números para gritar BINGO!'
    case 'DIAGONAL':
      return 'Marque qualquer uma das duas diagonais de 5 números para gritar BINGO!'
    case 'OUTER_BORDER':
    case 'JANELÃO':
    case 'JANELAO':
      return 'Marque todos os números da borda externa da cartela!'
    case 'INNER_BORDER':
    case 'JANELINHA':
      return 'Marque a moldura interna de 3x3 (excluindo o centro).'
    case 'FULL_CARD':
    case 'FULL_GRID':
    case 'BINGO_CHEIO':
    default:
      return 'Marque todos os 24 números para gritar BINGO!'
  }
})

const isCellHighlighted = (index: number) => {
  const patterns = activeRoundPatterns.value
  if (!patterns || patterns.length === 0) return true
  const pat = patterns[0].toUpperCase()
  
  const row = Math.floor((index - 1) / 5)
  const col = (index - 1) % 5
  
  switch (pat) {
    case 'LINE':
    case 'ANY_LINE':
      return row === 2
    case 'COLUMN':
    case 'ANY_COLUMN':
      return col === 2
    case 'DIAGONAL':
      return row === col || row === 4 - col
    case 'OUTER_BORDER':
    case 'JANELÃO':
    case 'JANELAO':
      return row === 0 || row === 4 || col === 0 || col === 4
    case 'INNER_BORDER':
    case 'JANELINHA':
      return row >= 1 && row <= 3 && col >= 1 && col <= 3 && !(row === 2 && col === 2)
    case 'FULL_CARD':
    case 'FULL_GRID':
    case 'BINGO_CHEIO':
    default:
      return true
  }
}

const winningCardIndex = computed(() => {
  const cards = gameStore.ticketMatrix?.cards || []
  const patterns = activeRoundPatterns.value
  for (let i = 0; i < cards.length; i++) {
    if (doesCardMatchPatterns(cards[i], patterns)) {
      return i
    }
  }
  return null
})

const isBingoButtonUnlocked = computed(() => winningCardIndex.value !== null)

watch(isBingoButtonUnlocked, (unlocked) => {
  if (unlocked) {
    nextTick(() => {
      if (claimBingoBtnRef.value) {
        claimBingoBtnRef.value.focus()
      }
    })
  }
})

// Story 4.3 Mobile Mini-map and Near-win alerts states
const prevMarkedCounts = ref<number[]>([])
const cardFlashes = ref<Record<number, boolean>>({})
const nearWinAlertCardIndex = ref<number | null>(null)

function getCardMarkedCount(card: CardMatrixDto): number {
  let count = 0
  const columnKeys: (keyof CardMatrixDto)[] = ['b', 'i', 'n', 'g', 'o']
  for (let c = 0; c < 5; c++) {
    const key = columnKeys[c]
    const col = card[key] || []
    for (let r = 0; r < 5; r++) {
      if (r === 2 && c === 2) continue
      const val = col[r]
      if (val !== undefined && markedNumbers.value.has(val)) {
        count++
      }
    }
  }
  return count
}

const nearWinCards = computed(() => {
  const cards = gameStore.ticketMatrix?.cards || []
  return cards.map((card) => getCardMarkedCount(card) === 23)
})

function focusNearWinCard(idx: number) {
  activeMobileCardIndex.value = idx
  nearWinAlertCardIndex.value = null
}

watch(markedNumbers, () => {
  const cards = gameStore.ticketMatrix?.cards || []
  cards.forEach((card, idx) => {
    const newCount = getCardMarkedCount(card)
    const prevCount = prevMarkedCounts.value[idx] || 0
    if (newCount > prevCount) {
      if (activeMobileCardIndex.value !== idx) {
        cardFlashes.value[idx] = true
        window.setTimeout(() => {
          cardFlashes.value[idx] = false
        }, 1500)
      }
    }
    prevMarkedCounts.value[idx] = newCount
  })
}, { deep: true })

watch(nearWinCards, (newVals, oldVals) => {
  newVals.forEach((isNearWin, idx) => {
    const wasNearWin = oldVals && oldVals[idx]
    if (isNearWin && !wasNearWin) {
      nearWinAlertCardIndex.value = idx
    }
  })
}, { deep: true })

// Clears the active announcement ticker
function clearTicker() {
  gameStore.activeAnnouncement = null
}

// Onboarding completion key
const ONBOARDING_COMPLETED_KEY = 'bingo_onboarding_completed'

// Chat messaging
interface ChatMsg {
  id: number
  sender: string
  text: string
  time: string
  isSystem?: boolean
}

const chatMessages = ref<ChatMsg[]>([
  { id: 1, sender: 'Sistema', text: 'Sala de jogo aberta! As cartelas ativadas receberão os sorteios automaticamente.', time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }), isSystem: true }
])

const showNextRoundAlert = ref(false)

const youtubeEmbedUrl = computed(() => {
  const url = gameStore.eventDetails?.youtubeUrl
  if (!url) return null
  
  let videoId = ''
  try {
    const regExp = /^.*(youtu.be\/|v\/|u\/\w\/|embed\/|watch\?v=|\&v=)([^#\&\?]*).*/
    const match = url.match(regExp)
    if (match && match[2].length === 11) {
      videoId = match[2]
    }
  } catch (e) {
    console.error('Error parsing youtube URL', e)
  }
  
  if (videoId) {
    return `https://www.youtube.com/embed/${videoId}?autoplay=1&mute=1&loop=1&playlist=${videoId}`
  }
  return null
})

const hasPendingRounds = computed(() => {
  if (!gameStore.eventDetails || !gameStore.eventDetails.rounds) return false
  return gameStore.eventDetails.rounds.some((r: any) => r.status === 'PENDING')
})

// Guard & Onboarding Trigger on Mount
onMounted(async () => {
  if (!gameStore.isAuthenticated) {
    const serialQuery = route.query.serial as string
    if (serialQuery && serialQuery.length === 6) {
      // Show modal
      activationSerial.value = serialQuery.toUpperCase()
      showActivationModal.value = true
    } else {
      router.replace('/activate')
    }
    return
  }

  await initializeGame()
})

onUnmounted(() => {
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

const claimCooldown = ref(false)

async function triggerClaimBingo() {
  if (claimCooldown.value) return;
  
  const winningIdx = winningCardIndex.value
  if (winningIdx === null) return

  claimCooldown.value = true
  setTimeout(() => { claimCooldown.value = false }, 3000) // 3 seconds delay

  const success = await gameStore.claimBingo(winningIdx)
  if (success) {
    showClaimDialog.value = true
  } else {
    alert("Falha ao reivindicar BINGO. Certifique-se de que sua cartela atende às regras.")
  }
}

function sendChatMessage() {
  if (!userChatMessage.value.trim()) return
  
  gameStore.sendChatMessage(userChatMessage.value)
  
  userChatMessage.value = ''
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
  background: #1c2541;
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.pattern-cell-preview.pattern-highlight {
  background: #ffd600;
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
  display: none;
}

@media (min-width: 960px) {
  .cards-desktop-grid {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 16px;
    width: 100%;
  }
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

.claim-bingo-btn:disabled {
  background: #475569 !important;
  color: #94a3b8 !important;
  cursor: not-allowed;
  box-shadow: none !important;
  animation: none !important;
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

/* Announcement Ticker Premium Styles */
.announcement-ticker-bar {
  background: linear-gradient(90deg, #1e1b4b 0%, #311042 100%);
  border-bottom: 2px solid #ffd600;
  color: #ffffff;
  height: 38px;
  display: flex;
  align-items: center;
  overflow: hidden;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.2);
  z-index: 10;
  position: relative;
}

.ticker-content {
  display: flex;
  align-items: center;
  width: 100%;
  padding: 0 16px;
  justify-content: space-between;
}

.ticker-icon {
  flex-shrink: 0;
  animation: pulse-icon 1.5s infinite;
}

@keyframes pulse-icon {
  0% { transform: scale(1); opacity: 0.8; }
  50% { transform: scale(1.15); opacity: 1; }
  100% { transform: scale(1); opacity: 0.8; }
}

.marquee-wrapper {
  flex-grow: 1;
  overflow: hidden;
  position: relative;
  white-space: nowrap;
  display: flex;
  align-items: center;
  margin: 0 12px;
}

.marquee-text {
  display: inline-block;
  padding-left: 100%;
  animation: marquee 20s linear infinite;
  font-weight: 700;
  font-size: 0.9rem;
  letter-spacing: 0.05em;
  color: #fef08a; /* AAA contrast gold-yellow */
}

.close-ticker-btn {
  flex-shrink: 0;
  transition: transform 0.2s;
}

.close-ticker-btn:hover {
  transform: scale(1.1);
  color: #ffffff !important;
}

@keyframes marquee {
  0% {
    transform: translate3d(0, 0, 0);
  }
  100% {
    transform: translate3d(-100%, 0, 0);
  }
}

/* Story 4.3 - Mini-map and near-win styles */
.dot-flash-green {
  background-color: #2e7d32 !important;
  border-color: #4caf50 !important;
  color: #ffffff !important;
  animation: flash-green-anim 1.5s ease-out;
}

@keyframes flash-green-anim {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.15); }
}

.dot-near-win {
  border-color: #ffd600 !important;
  box-shadow: 0 0 12px rgba(255, 214, 0, 0.8);
  animation: gold-pulse-mini 1.5s infinite ease-in-out;
}

@keyframes gold-pulse-mini {
  0%, 100% {
    box-shadow: 0 0 8px rgba(255, 214, 0, 0.5);
    border-color: #ffd600;
  }
  50% {
    box-shadow: 0 0 18px rgba(255, 214, 0, 0.9);
    border-color: #ffab00;
  }
}

.plus-one-badge {
  position: absolute;
  top: -8px;
  right: -8px;
  background-color: #ef4444;
  color: #ffffff;
  font-size: 0.65rem;
  font-weight: 900;
  padding: 2px 6px;
  border-radius: 10px;
  border: 1px solid #ffffff;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.2);
}

.near-win-banner-alert {
  background: linear-gradient(135deg, #ffd600 0%, #ffab00 100%);
  color: #0b132b;
  font-weight: 800;
  font-size: 0.9rem;
  padding: 12px 18px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  box-shadow: 0 10px 25px rgba(255, 214, 0, 0.4);
  cursor: pointer;
  z-index: 10;
  margin-bottom: 12px;
  animation: pulse-border 1.5s infinite alternate;
}

@keyframes pulse-border {
  0% {
    box-shadow: 0 10px 25px rgba(255, 214, 0, 0.4);
  }
  100% {
    box-shadow: 0 10px 30px rgba(255, 214, 0, 0.7), 0 0 0 4px rgba(255, 214, 0, 0.3);
  }
}

/* slide-y transition */
.slide-y-enter-active,
.slide-y-leave-active {
  transition: all 0.4s cubic-bezier(0.16, 1, 0.3, 1);
}
.slide-y-enter-from,
.slide-y-leave-to {
  transform: translateY(-20px);
  opacity: 0;
}

/* bounce transition */
.bounce-enter-active {
  animation: bounce-in 0.5s;
}
.bounce-leave-active {
  animation: bounce-in 0.5s reverse;
}
@keyframes bounce-in {
  0% {
    transform: scale(0);
  }
  50% {
    transform: scale(1.25);
  }
  100% {
    transform: scale(1);
  }
}

/* Victory Celebration styles */
.victory-trophy-ring {
  width: 160px;
  height: 160px;
  border-radius: 50%;
  background: rgba(245, 158, 11, 0.1);
  border: 4px solid #f59e0b;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 0 30px rgba(245, 158, 11, 0.4);
  animation: victory-pulse 2s infinite ease-in-out;
}

.victory-trophy-icon {
  animation: trophy-wobble 3s infinite ease-in-out;
}

@keyframes victory-pulse {
  0%, 100% {
    box-shadow: 0 0 20px rgba(245, 158, 11, 0.3);
    transform: scale(1);
  }
  50% {
    box-shadow: 0 0 45px rgba(245, 158, 11, 0.7);
    transform: scale(1.05);
  }
}

@keyframes trophy-wobble {
  0%, 100% { transform: rotate(0deg); }
  25% { transform: rotate(-8deg); }
  75% { transform: rotate(8deg); }
}

.hover-scale-up {
  transition: all 0.2s ease-in-out;
}

.hover-scale-up:hover {
  transform: scale(1.05);
  box-shadow: 0 10px 20px rgba(245, 158, 11, 0.4) !important;
}

.next-round-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(11, 19, 43, 0.92);
  backdrop-filter: blur(10px);
  z-index: 15;
  border-radius: 20px;
  border: 1px solid rgba(255, 214, 0, 0.25);
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.5s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
