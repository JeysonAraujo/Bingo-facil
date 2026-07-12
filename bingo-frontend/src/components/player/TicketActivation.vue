<template>
  <v-app>
    <v-main class="activation-bg">
      <v-container class="fill-height" fluid>
        <v-row align="center" justify="center">
          <v-col cols="12" sm="8" md="5" lg="4">

            <!-- Card de ativação -->
            <v-card class="activation-card" elevation="12" rounded="xl">

              <!-- Cabeçalho com logo/ícone -->
              <div class="activation-header">
                <div class="activation-logo">🎱</div>
                <h1 class="activation-title">Ativar Cartela</h1>
                <p class="activation-subtitle">
                  Digite o código de 6 caracteres impresso no seu cartelão
                </p>
              </div>

              <v-card-text class="activation-body">

                <!-- Input do serial -->
                <v-text-field
                  id="serial-input"
                  v-model="serialInput"
                  :disabled="isLoading"
                  :error="!!error"
                  :error-messages="error || ''"
                  autofocus
                  class="serial-field mb-3"
                  color="primary"
                  counter="6"
                  density="comfortable"
                  maxlength="6"
                  placeholder="SERIAL"
                  rounded
                  variant="outlined"
                  aria-label="Código serial da cartela"
                  aria-required="true"
                  @input="onInput"
                />



                <!-- Botão de ativação -->
                <v-btn
                  color="indigo-darken-4"
                  size="large"
                  block
                  class="font-weight-bold rounded-lg text-white mb-3 text-capitalize"
                  :disabled="isLoading || !serialInput || serialInput.length !== 6"
                  @click="submitForm"
                >
                  Continuar
                </v-btn>

                <!-- Loading state -->
                <div v-if="isLoading" class="loading-state">
                  <v-progress-circular
                    color="primary"
                    indeterminate
                    size="36"
                  />
                  <span class="loading-text">Verificando seus dados...</span>
                </div>

                <!-- Dica para quem escaneou o QR -->
                <p class="hint-text" v-if="!isLoading && !error">
                  💡 Você também pode escanear o QR Code impresso no cartelão e depois completar seu nome e telefone para entrar.
                </p>

              </v-card-text>
            </v-card>

            <!-- Rodapé -->
            <p class="footer-text">© 2026 Bingo AI · Todos os direitos reservados</p>

          </v-col>
        </v-row>
      </v-container>
    </v-main>
  </v-app>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useGameStore } from '../../stores/game'

const route = useRoute()
const router = useRouter()
const gameStore = useGameStore()

const serialInput = ref('')

const isLoading = ref(false)
const error = ref<string | null>(null)

// Auto-fill from QR code URL param (?serial=XXXXXX)
onMounted(() => {
  const serialParam = route.query.serial as string | undefined
  if (serialParam && serialParam.length === 6) {
    serialInput.value = serialParam.toUpperCase()
  }
})

// Normalize serial input
function onInput() {
  error.value = null
  const val = serialInput.value.toUpperCase().replace(/[^A-Z0-9]/g, '')
  serialInput.value = val
}

async function submitForm() {
  if (!serialInput.value || serialInput.value.length !== 6) {
    error.value = 'O serial deve ter exatamente 6 caracteres.'
    return
  }

  isLoading.value = true
  error.value = null
  const validationResult = await gameStore.validateTicket(serialInput.value)
  isLoading.value = false

  if (validationResult) {
    if (validationResult.activated) {
      // If already activated and they don't have session token, 
      // they need to activate again to get the token (with name/phone).
      // We still redirect to /game and let the modal handle it.
    }
    router.push(`/game?serial=${serialInput.value}`)
  } else {
    error.value = gameStore.error || 'Cartela não encontrada ou inválida.'
  }
}
</script>

<style scoped>
.activation-bg {
  background: linear-gradient(135deg, #1A237E 0%, #283593 50%, #1565C0 100%);
  min-height: 100vh;
}

.activation-card {
  background: rgba(255, 255, 255, 0.97);
  backdrop-filter: blur(12px);
  overflow: hidden;
}

.activation-header {
  background: linear-gradient(135deg, #1A237E, #1565C0);
  padding: 40px 32px 32px;
  text-align: center;
}

.activation-logo {
  font-size: 56px;
  margin-bottom: 12px;
  filter: drop-shadow(0 4px 8px rgba(0, 0, 0, 0.3));
  animation: bounce 2s ease-in-out infinite;
}

@keyframes bounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-8px); }
}

.activation-title {
  font-size: 1.8rem;
  font-weight: 700;
  color: white;
  margin: 0 0 8px 0;
  letter-spacing: -0.5px;
}

.activation-subtitle {
  font-size: 0.95rem;
  color: rgba(255, 255, 255, 0.8);
  margin: 0;
  line-height: 1.5;
}

.activation-body {
  padding: 32px !important;
}

.serial-field :deep(input) {
  font-size: 1.8rem !important;
  font-weight: 700 !important;
  letter-spacing: 0.4em !important;
  text-align: center !important;
  text-transform: uppercase !important;
  color: #1A237E !important;
}

.loading-state {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  padding: 16px 0;
}

.loading-text {
  font-size: 0.95rem;
  color: #616161;
}

.hint-text {
  font-size: 0.85rem;
  color: #9E9E9E;
  text-align: center;
  margin-top: 16px;
  line-height: 1.5;
}

.footer-text {
  text-align: center;
  color: rgba(255, 255, 255, 0.5);
  font-size: 0.8rem;
  margin-top: 24px;
}
</style>
