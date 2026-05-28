<template>
  <Teleport to="body">
    <Transition name="fade">
      <div v-if="active" class="onboarding-overlay" :style="overlayStyle">
        <!-- Spotlight element -->
        <div
          v-if="spotlightRect"
          class="spotlight"
          :style="spotlightStyle"
        >
          <!-- Pulsing ring around target -->
          <div class="spotlight-pulse"></div>
        </div>

        <!-- Tooltip Card -->
        <div
          class="tooltip-card-wrapper"
          :style="tooltipWrapperStyle"
        >
          <v-card class="tour-card" elevation="12" rounded="xl">
            <!-- Progress Bar -->
            <v-progress-linear
              :model-value="((currentStep + 1) / steps.length) * 100"
              color="amber-darken-2"
              height="6"
              rounded
            ></v-progress-linear>

            <v-card-item class="pt-4 pb-2">
              <template v-slot:prepend>
                <div class="step-icon-bg mr-3">
                  <span class="step-icon">{{ activeStep.icon }}</span>
                </div>
              </template>
              <v-card-title class="tour-title text-wrap">
                {{ activeStep.title }}
              </v-card-title>
              <v-card-subtitle class="tour-step-indicator">
                Passo {{ currentStep + 1 }} de {{ steps.length }}
              </v-card-subtitle>
            </v-card-item>

            <v-card-text class="tour-content py-2">
              <p class="tour-description">{{ activeStep.description }}</p>
            </v-card-text>

            <v-card-actions class="px-4 pb-4 pt-2 justify-space-between">
              <v-btn
                variant="text"
                color="grey-darken-1"
                class="px-4 text-capitalize rounded-lg"
                @click="skipTour"
              >
                Pular
              </v-btn>

              <div class="d-flex gap-2">
                <v-btn
                  v-if="currentStep > 0"
                  variant="outlined"
                  color="primary"
                  class="px-4 text-capitalize rounded-lg"
                  @click="prevStep"
                >
                  Voltar
                </v-btn>
                
                <v-btn
                  color="amber-darken-2"
                  class="px-5 text-capitalize text-white rounded-lg font-weight-bold"
                  elevation="2"
                  @click="nextStep"
                >
                  {{ currentStep === steps.length - 1 ? 'Começar!' : 'Avançar' }}
                </v-btn>
              </div>
            </v-card-actions>
          </v-card>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch, nextTick, type CSSProperties } from 'vue'

const props = defineProps<{
  modelValue: boolean
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
  (e: 'completed'): void
}>()

const active = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const currentStep = ref(0)
const spotlightRect = ref<DOMRect | null>(null)
const windowWidth = ref(window.innerWidth)
const windowHeight = ref(window.innerHeight)

const steps = [
  {
    target: '#youtube-stream-container',
    title: 'Transmissão ao Vivo 🎥',
    description: 'Assista ao apresentador cantando as pedras em tempo real! A transmissão fica sempre visível no topo no celular ou à esquerda no computador para você não perder nada.',
    icon: '📺'
  },
  {
    target: '#bingo-cards-container',
    title: 'Suas 4 Cartelas 🎫',
    description: 'Aqui ficam as suas cartelas ativas! Fique tranquilo: a marcação dos números sorteados é AUTOMÁTICA e atualiza instantaneamente na tela assim que o locutor canta o número.',
    icon: '⚡'
  },
  {
    target: '#bingo-claim-button-container',
    title: 'Grite BINGO! 🏆',
    description: 'Assim que completar o padrão da rodada (por exemplo: linha, coluna ou cartela cheia) em qualquer uma das suas cartelas, clique rápido aqui para reivindicar sua vitória e garantir seu prêmio!',
    icon: '🎉'
  }
]

const activeStep = computed(() => steps[currentStep.value])

// Style for background overlay to capture pointer events
const overlayStyle = computed<CSSProperties>(() => ({
  pointerEvents: 'auto'
}))

// Style for spotlight highlighting the element
const spotlightStyle = computed<CSSProperties>(() => {
  if (!spotlightRect.value) return {}
  
  const padding = 8
  return {
    top: `${spotlightRect.value.top - padding}px`,
    left: `${spotlightRect.value.left - padding}px`,
    width: `${spotlightRect.value.width + padding * 2}px`,
    height: `${spotlightRect.value.height + padding * 2}px`,
  }
})

// Calculate optimal position for the tooltip card
const tooltipWrapperStyle = computed<CSSProperties>(() => {
  if (!spotlightRect.value) {
    // Center of screen if no target is found
    return {
      top: '50%',
      left: '50%',
      transform: 'translate(-50%, -50%)',
      position: 'fixed'
    }
  }

  const rect = spotlightRect.value
  const padding = 16
  const cardWidth = Math.min(400, windowWidth.value - 32)
  const cardHeight = 220 // approximate
  
  let top = 0
  let left = 0

  // Standard calculation: place below or above
  if (rect.bottom + cardHeight + padding < windowHeight.value) {
    // Place below
    top = rect.bottom + padding
    left = Math.max(16, Math.min(rect.left + rect.width / 2 - cardWidth / 2, windowWidth.value - cardWidth - 16))
  } else if (rect.top - cardHeight - padding > 0) {
    // Place above
    top = rect.top - cardHeight - padding
    left = Math.max(16, Math.min(rect.left + rect.width / 2 - cardWidth / 2, windowWidth.value - cardWidth - 16))
  } else {
    // Fallback: center on screen but shifted down
    top = windowHeight.value - cardHeight - 24
    left = (windowWidth.value - cardWidth) / 2
  }

  return {
    top: `${top}px`,
    left: `${left}px`,
    width: `${cardWidth}px`,
    position: 'fixed',
    transition: 'all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1)'
  }
})

// Update the highlighted element's bounding rect
function updateSpotlight(shouldScroll = false) {
  if (!active.value) return

  const targetSelector = activeStep.value.target
  const element = document.querySelector(targetSelector)

  if (element) {
    if (shouldScroll) {
      // Scroll element into view if not visible
      element.scrollIntoView({ behavior: 'smooth', block: 'center' })
      
      // Give scroll time to settle before getting rect
      setTimeout(() => {
        const updatedElement = document.querySelector(targetSelector)
        if (updatedElement) {
          spotlightRect.value = updatedElement.getBoundingClientRect()
        }
      }, 150)
    } else {
      spotlightRect.value = element.getBoundingClientRect()
    }
  } else {
    spotlightRect.value = null
  }
}

function handleResize() {
  windowWidth.value = window.innerWidth
  windowHeight.value = window.innerHeight
  updateSpotlight(false)
}

const handleScroll = () => {
  updateSpotlight(false)
}

function nextStep() {
  if (currentStep.value < steps.length - 1) {
    currentStep.value++
  } else {
    completeTour()
  }
}

function prevStep() {
  if (currentStep.value > 0) {
    currentStep.value--
  }
}

function skipTour() {
  completeTour()
}

function completeTour() {
  active.value = false
  emit('completed')
}

// Watchers
watch(currentStep, () => {
  nextTick(() => {
    updateSpotlight(true)
  })
})

watch(active, (newVal) => {
  if (newVal) {
    currentStep.value = 0
    document.body.style.overflow = 'hidden' // Lock scroll when tour is active
    nextTick(() => {
      updateSpotlight(true)
    })
  } else {
    document.body.style.overflow = '' // Restore scroll
    spotlightRect.value = null
  }
})

onMounted(() => {
  window.addEventListener('resize', handleResize)
  window.addEventListener('scroll', handleScroll, { passive: true })
  
  if (active.value) {
    document.body.style.overflow = 'hidden'
    nextTick(() => {
      updateSpotlight(true)
    })
  }
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  window.removeEventListener('scroll', handleScroll)
  document.body.style.overflow = ''
})
</script>

<style scoped>
.onboarding-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  z-index: 9999;
  background-color: rgba(15, 23, 42, 0.7);
  backdrop-filter: blur(2px);
  overflow: hidden;
  transition: opacity 0.3s ease;
}

/* Spotlight Effect */
.spotlight {
  position: fixed;
  border-radius: 16px;
  box-shadow: 0 0 0 9999px rgba(15, 23, 42, 0.75);
  z-index: 10000;
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
  pointer-events: none;
  border: 2px solid #ffab00;
}

.spotlight-pulse {
  position: absolute;
  top: -6px;
  left: -6px;
  right: -6px;
  bottom: -6px;
  border-radius: 20px;
  border: 2px solid #ffd600;
  opacity: 0;
  animation: pulse-ring 2s cubic-bezier(0.215, 0.61, 0.355, 1) infinite;
  pointer-events: none;
}

/* Tooltip Card Styling */
.tooltip-card-wrapper {
  z-index: 10001;
}

.tour-card {
  background: #ffffff !important;
  color: #1e293b !important;
  border: 1px solid rgba(226, 232, 240, 0.8);
  max-width: 100%;
}

.step-icon-bg {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  background: rgba(255, 171, 0, 0.12);
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid rgba(255, 171, 0, 0.25);
  flex-shrink: 0;
}

.step-icon {
  font-size: 1.4rem;
}

.tour-title {
  font-size: 1.15rem !important;
  font-weight: 800 !important;
  color: #0f172a !important;
  line-height: 1.3;
}

.tour-step-indicator {
  font-size: 0.75rem !important;
  font-weight: 600 !important;
  color: #ffab00 !important;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  margin-top: 2px;
}

.tour-description {
  font-size: 0.9rem;
  line-height: 1.6;
  color: #475569;
  margin: 0;
}

/* Animations */
@keyframes pulse-ring {
  0% {
    transform: scale(0.95);
    opacity: 0.8;
  }
  50% {
    opacity: 0.3;
  }
  100% {
    transform: scale(1.1);
    opacity: 0;
  }
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
