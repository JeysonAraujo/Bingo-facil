<template>
  <v-container class="py-8">
    <!-- Header -->
    <v-row class="mb-6">
      <v-col cols="12">
        <div class="d-flex align-center mb-2">
          <v-btn
            icon="mdi-arrow-left"
            variant="text"
            color="primary"
            class="mr-2"
            to="/admin"
          ></v-btn>
          <h1 class="text-h4 font-weight-bold color-primary mb-0">
            Cadastrar Novo Evento
          </h1>
        </div>
        <p class="text-subtitle-1 text-grey-darken-1 pl-12 mb-0">
          Configure a data de agendamento, as rodadas sequenciais e as regras de vitória.
        </p>
      </v-col>
    </v-row>

    <!-- Error Alert -->
    <v-alert
      v-if="adminStore.error"
      type="error"
      title="Erro ao salvar"
      variant="tonal"
      class="mb-6 rounded-lg border-red"
      closable
    >
      {{ adminStore.error }}
    </v-alert>

    <!-- Form Body -->
    <v-form ref="form" v-model="isFormValid" @submit.prevent="submitForm">
      <v-row>
        <!-- Left Column: Event Metadata -->
        <v-col cols="12" md="4">
          <v-card class="pa-5 elevation-2 rounded-lg border-card h-100">
            <h2 class="text-h6 font-weight-bold mb-4 color-primary border-bottom-indigo pb-2">
              <v-icon icon="mdi-information-outline" class="mr-1" color="primary"></v-icon>
              Dados do Evento
            </h2>

            <v-text-field
              v-model="title"
              label="Título do Evento"
              placeholder="Ex: Super Bingo de Sábado"
              variant="outlined"
              color="primary"
              :rules="[v => !!v || 'O título é obrigatório']"
              required
              class="mb-4"
            ></v-text-field>

            <v-text-field
              v-model="eventDate"
              label="Data do Evento"
              type="date"
              variant="outlined"
              color="primary"
              :rules="[v => !!v || 'A data é obrigatória']"
              required
              class="mb-4"
            ></v-text-field>

            <v-text-field
              v-model="eventTime"
              label="Hora do Evento"
              type="time"
              variant="outlined"
              color="primary"
              :rules="[v => !!v || 'A hora é obrigatória']"
              required
              class="mb-6"
            ></v-text-field>

            <div class="text-caption text-grey-darken-1 border-subcard pa-3 bg-grey-lighten-4 rounded-lg">
              <v-icon icon="mdi-lightbulb-on-outline" color="amber-darken-3" class="mr-1" size="16"></v-icon>
              <strong>Atenção:</strong> Certifique-se de configurar a hora correspondente ao início planejado da live de transmissão.
            </div>
          </v-card>
        </v-col>

        <!-- Right Column: Rounds and Prizes Structure -->
        <v-col cols="12" md="8">
          <v-card class="pa-5 elevation-2 rounded-lg border-card">
            <div class="d-flex justify-between align-center mb-4 border-bottom-indigo pb-2">
              <h2 class="text-h6 font-weight-bold color-primary mb-0">
                <v-icon icon="mdi-format-list-numbered" class="mr-1" color="primary"></v-icon>
                Estrutura de Rodadas
              </h2>
              <v-btn
                color="primary"
                variant="outlined"
                prepend-icon="mdi-plus-circle"
                class="rounded-lg font-weight-bold"
                @click="addRound"
              >
                Adicionar Rodada
              </v-btn>
            </div>

            <div v-if="rounds.length === 0" class="text-center py-8 bg-grey-lighten-5 rounded-lg border-dashed mb-4">
              <v-icon icon="mdi-alert-circle-outline" size="48" color="grey-lighten-1" class="mb-2"></v-icon>
              <p class="text-body-1 text-grey-darken-1 mb-0">
                Configure pelo menos uma rodada para o sorteio.
              </p>
            </div>

            <!-- Dynamic Rounds List -->
            <v-expansion-panels v-else v-model="activePanel" multiple class="mb-6">
              <v-expansion-panel
                v-for="(round, rIndex) in rounds"
                :key="rIndex"
                class="rounded-lg mb-4 border-subcard overflow-hidden elevation-1"
              >
                <!-- Round Header -->
                <v-expansion-panel-title class="bg-indigo-lighten-5 py-3">
                  <div class="d-flex justify-between align-center w-100 pr-4">
                    <span class="font-weight-bold color-primary text-subtitle-1">
                      Rodada #{{ round.roundNumber }}
                    </span>
                    <v-btn
                      icon="mdi-delete"
                      variant="text"
                      color="red-darken-2"
                      size="small"
                      class="shrink-0"
                      @click.stop="removeRound(rIndex)"
                      aria-label="Excluir Rodada"
                    ></v-btn>
                  </div>
                </v-expansion-panel-title>

                <!-- Round Content -->
                <v-expansion-panel-text class="py-4">
                  <div class="d-flex justify-between align-center mb-4">
                    <h3 class="text-subtitle-2 font-weight-bold text-grey-darken-2 mb-0">
                      Prêmios da Rodada
                    </h3>
                    <v-btn
                      color="primary"
                      variant="text"
                      prepend-icon="mdi-plus"
                      size="small"
                      class="font-weight-bold"
                      @click="addPrize(rIndex)"
                    >
                      Adicionar Prêmio
                    </v-btn>
                  </div>

                  <div v-if="round.prizes.length === 0" class="text-center py-4 bg-grey-lighten-5 rounded-lg border-dashed mb-2">
                    <p class="text-caption text-grey-darken-2 mb-0">
                      Nenhum prêmio cadastrado nesta rodada.
                    </p>
                  </div>

                  <!-- Dynamic Prizes in Round -->
                  <v-row v-for="(prize, pIndex) in round.prizes" :key="pIndex" class="align-center border-bottom-grey py-2 px-1">
                    <v-col cols="12" sm="4" class="py-1">
                      <v-text-field
                        v-model="prize.name"
                        label="Nome do Prêmio"
                        placeholder="Ex: Geladeira, R$ 500"
                        variant="outlined"
                        density="compact"
                        :rules="[v => !!v || 'O nome do prêmio é obrigatório']"
                        required
                        hide-details="auto"
                      ></v-text-field>
                    </v-col>
                    <v-col cols="12" sm="4" class="py-1">
                      <v-select
                        v-model="prize.type"
                        label="Tipo de Marcação"
                        :items="prizeTypes"
                        item-title="title"
                        item-value="value"
                        variant="outlined"
                        density="compact"
                        hide-details="auto"
                        @update:model-value="onPrizeTypeChange(rIndex, pIndex)"
                      ></v-select>
                    </v-col>
                    <v-col cols="10" sm="3" class="py-1">
                      <v-select
                        v-model="prize.drawPattern"
                        label="Padrão de Vitória"
                        :items="drawPatterns"
                        item-title="title"
                        item-value="value"
                        variant="outlined"
                        density="compact"
                        hide-details="auto"
                      ></v-select>
                    </v-col>
                    <v-col cols="2" sm="1" class="text-center py-1">
                      <v-btn
                        icon="mdi-close-circle"
                        variant="text"
                        color="red-darken-1"
                        size="small"
                        @click="removePrize(rIndex, pIndex)"
                        aria-label="Excluir Prêmio"
                      ></v-btn>
                    </v-col>
                  </v-row>
                </v-expansion-panel-text>
              </v-expansion-panel>
            </v-expansion-panels>

            <!-- Bottom Actions -->
            <v-card-actions class="pa-0 pt-4 border-top-grey">
              <v-spacer></v-spacer>
              <v-btn
                variant="outlined"
                color="primary"
                size="large"
                class="rounded-lg font-weight-bold mr-3"
                to="/admin"
                min-height="48px"
              >
                Voltar para o Painel
              </v-btn>
              <v-btn
                color="secondary"
                variant="flat"
                size="large"
                class="rounded-lg font-weight-bold text-primary pulsing-gold"
                type="submit"
                :disabled="!isFormValid || rounds.length === 0 || adminStore.isLoading"
                :loading="adminStore.isLoading"
                min-height="48px"
              >
                Salvar Evento
              </v-btn>
            </v-card-actions>
          </v-card>
        </v-col>
      </v-row>
    </v-form>
  </v-container>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAdminStore } from '../../stores/admin'
import type { CreateRoundRequest, CreatePrizeRequest, CreateEventRequest } from '../../services/api'

const router = useRouter()
const adminStore = useAdminStore()

const isFormValid = ref(false)
const title = ref('')
const eventDate = ref('')
const eventTime = ref('')
const activePanel = ref<number[]>([0])

// Define structured options
const prizeTypes = [
  { title: 'Linha', value: 'LINE' },
  { title: 'Coluna', value: 'COLUMN' },
  { title: 'Diagonal', value: 'DIAGONAL' },
  { title: 'Janelão (Borda Externa)', value: 'OUTER_BORDER' },
  { title: 'Janelinha (Borda Interna)', value: 'INNER_BORDER' },
  { title: 'Cartela Cheia', value: 'FULL_CARD' }
]

const drawPatterns = [
  { title: 'Qualquer Linha', value: 'ANY_LINE' },
  { title: 'Qualquer Coluna', value: 'ANY_COLUMN' },
  { title: 'Diagonais', value: 'DIAGONAL' },
  { title: 'Janelão (Borda Externa)', value: 'OUTER_BORDER' },
  { title: 'Janelinha (Borda Interna)', value: 'INNER_BORDER' },
  { title: 'Grade Completa', value: 'FULL_GRID' }
]

// Dynamic local state structure
interface LocalPrize {
  name: string
  type: string
  drawPattern: string
}

interface LocalRound {
  roundNumber: number
  prizes: LocalPrize[]
}

const rounds = reactive<LocalRound[]>([])

onMounted(() => {
  // Initialize with exactly 1 round and 2 default prizes
  addRound()
})

const addRound = () => {
  const roundNumber = rounds.length + 1
  rounds.push({
    roundNumber,
    prizes: [
      { name: 'Prêmio da Linha', type: 'LINE', drawPattern: 'ANY_LINE' },
      { name: 'Cartela Cheia', type: 'FULL_CARD', drawPattern: 'FULL_GRID' }
    ]
  })
  // Expand the newly added round
  activePanel.value = [rounds.length - 1]
}

const removeRound = (index: number) => {
  rounds.splice(index, 1)
  // Re-number remaining rounds consecutively
  rounds.forEach((round, idx) => {
    round.roundNumber = idx + 1
  })
}

const addPrize = (rIndex: number) => {
  rounds[rIndex].prizes.push({
    name: '',
    type: 'LINE',
    drawPattern: 'ANY_LINE'
  })
}

const removePrize = (rIndex: number, pIndex: number) => {
  rounds[rIndex].prizes.splice(pIndex, 1)
}

const onPrizeTypeChange = (rIndex: number, pIndex: number) => {
  const prize = rounds[rIndex].prizes[pIndex]
  if (prize.type === 'LINE') {
    prize.drawPattern = 'ANY_LINE'
  } else if (prize.type === 'COLUMN') {
    prize.drawPattern = 'ANY_COLUMN'
  } else if (prize.type === 'DIAGONAL') {
    prize.drawPattern = 'DIAGONAL'
  } else if (prize.type === 'OUTER_BORDER') {
    prize.drawPattern = 'OUTER_BORDER'
  } else if (prize.type === 'INNER_BORDER') {
    prize.drawPattern = 'INNER_BORDER'
  } else if (prize.type === 'FULL_CARD') {
    prize.drawPattern = 'FULL_GRID'
  }
}

const submitForm = async () => {
  if (!isFormValid.value || rounds.length === 0) return

  // Standardize scheduling time into ISO-8601 UTC string
  const dateTimeString = `${eventDate.value}T${eventTime.value}:00`
  const localDateTime = new Date(dateTimeString)
  const scheduledAt = localDateTime.toISOString()

  // Format request payload
  const payload: CreateEventRequest = {
    title: title.value,
    scheduledAt,
    rounds: rounds.map(r => ({
      roundNumber: r.roundNumber,
      prizes: r.prizes.map(p => ({
        name: p.name,
        type: p.type,
        drawPattern: p.drawPattern
      }))
    }))
  }

  const success = await adminStore.createEvent(payload)
  if (success) {
    // Redirect to administrative dashboard
    router.push('/admin')
  }
}
</script>

<style scoped>
.color-primary {
  color: #1A237E;
}

.border-card {
  border: 2px solid #E0E0E0 !important;
}

.border-bottom-indigo {
  border-bottom: 2px solid #C5CAE9 !important;
}

.border-bottom-grey {
  border-bottom: 1px solid #EEEEEE !important;
}

.border-top-grey {
  border-top: 1px solid #E0E0E0 !important;
}

.border-subcard {
  border: 1px solid #E0E0E0 !important;
}

.border-dashed {
  border: 2px dashed #BDBDBD !important;
}

.shrink-0 {
  flex-shrink: 0;
}

/* Pulsing accent styling */
.pulsing-gold {
  box-shadow: 0 0 0 0 rgba(255, 214, 0, 0.6);
  animation: pulse-gold 2s infinite;
  background-color: #FFD600 !important;
}

.pulsing-gold:hover {
  background-color: #FFEA00 !important;
}

@keyframes pulse-gold {
  0% {
    transform: scale(1);
    box-shadow: 0 0 0 0 rgba(255, 214, 0, 0.7);
  }
  70% {
    transform: scale(1.02);
    box-shadow: 0 0 0 8px rgba(255, 214, 0, 0);
  }
  100% {
    transform: scale(1);
    box-shadow: 0 0 0 0 rgba(255, 214, 0, 0);
  }
}
</style>
