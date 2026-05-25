<template>
  <v-container class="py-8">
    <!-- Header Area -->
    <v-row align="center" class="mb-6">
      <v-col cols="12" sm="8">
        <h1 class="text-h4 font-weight-bold color-primary d-flex align-center">
          <v-icon icon="mdi-view-dashboard" class="mr-2" color="primary"></v-icon>
          Painel de Controle do Administrador
        </h1>
        <p class="text-subtitle-1 text-grey-darken-1 mb-0">
          Gerencie seus eventos e configure as estruturas de rodadas e prêmios.
        </p>
      </v-col>
      <v-col cols="12" sm="4" class="text-sm-right">
        <v-btn
          color="primary"
          size="large"
          prepend-icon="mdi-plus"
          class="rounded-lg elevation-2 font-weight-bold px-6"
          to="/admin/events/new"
          min-height="48px"
        >
          Criar Novo Evento
        </v-btn>
      </v-col>
    </v-row>

    <!-- Error Alert -->
    <v-alert
      v-if="adminStore.error"
      type="error"
      title="Erro"
      variant="tonal"
      class="mb-6 rounded-lg border-red"
      closable
    >
      {{ adminStore.error }}
    </v-alert>

    <!-- Loading Skeleton Grid -->
    <v-row v-if="adminStore.isLoading">
      <v-col v-for="n in 3" :key="n" cols="12" md="4">
        <v-card class="pa-4 rounded-lg elevation-1" loading></v-card>
      </v-col>
    </v-row>

    <!-- Empty State -->
    <v-row v-else-if="adminStore.events.length === 0" justify="center" class="py-12">
      <v-col cols="12" md="6" class="text-center">
        <v-card class="pa-8 elevation-2 rounded-lg border-dashed">
          <v-icon icon="mdi-calendar-blank-outline" size="64" color="grey-lighten-1" class="mb-4"></v-icon>
          <h2 class="text-h5 font-weight-bold mb-2">Nenhum evento encontrado</h2>
          <p class="text-body-1 text-grey-darken-1 mb-6">
            Você ainda não cadastrou nenhum evento de bingo. Comece criando um agora mesmo!
          </p>
          <v-btn
            color="primary"
            size="large"
            prepend-icon="mdi-plus"
            class="rounded-lg elevation-1 px-6 font-weight-bold"
            to="/admin/events/new"
            min-height="48px"
          >
            Criar Primeiro Evento
          </v-btn>
        </v-card>
      </v-col>
    </v-row>

    <!-- Events Grid -->
    <v-row v-else>
      <v-col v-for="event in adminStore.events" :key="event.id" cols="12" md="6" lg="4">
        <v-card class="elevation-2 rounded-lg border-card h-100 d-flex flex-column transition-card">
          <!-- Card Header Banner -->
          <v-card-item class="bg-indigo-lighten-5 py-4 border-bottom-indigo">
            <div class="d-flex justify-between align-center">
              <v-card-title class="font-weight-bold text-h6 color-primary text-truncate pr-2">
                {{ event.title }}
              </v-card-title>
              <v-chip color="success" size="small" variant="flat" class="font-weight-bold shrink-0">
                Ativo
              </v-chip>
            </div>
          </v-card-item>

          <v-card-text class="py-4 flex-grow-1">
            <!-- Event Meta Data -->
            <div class="d-flex align-center mb-3">
              <v-icon icon="mdi-calendar-clock" color="primary" class="mr-2" size="20"></v-icon>
              <span class="text-body-2 font-weight-bold">
                Agendado: {{ formatDateTime(event.scheduledAt) }}
              </span>
            </div>
            <div class="d-flex align-center mb-4">
              <v-icon icon="mdi-clock-outline" color="grey-darken-1" class="mr-2" size="20"></v-icon>
              <span class="text-caption text-grey-darken-1">
                Criado em: {{ formatDateTime(event.createdAt) }}
              </span>
            </div>

            <v-divider class="mb-4"></v-divider>

            <!-- Rounds & Prizes Info -->
            <h3 class="text-subtitle-2 font-weight-bold mb-3 d-flex align-center">
              <v-icon icon="mdi-trophy-variant-outline" color="amber-darken-2" class="mr-1" size="18"></v-icon>
              Rodadas e Estrutura de Prêmios:
            </h3>

            <div v-for="round in event.rounds" :key="round.id" class="mb-3 bg-grey-lighten-4 pa-3 rounded-lg border-subcard">
              <div class="d-flex justify-between align-center mb-1">
                <span class="text-subtitle-2 font-weight-bold color-primary">
                  Rodada #{{ round.roundNumber }}
                </span>
                <v-chip color="grey-darken-2" size="x-small" variant="outlined" class="font-weight-bold">
                  {{ round.status }}
                </v-chip>
              </div>
              <ul class="pl-4 mb-0 text-caption text-grey-darken-3">
                <li v-for="prize in round.prizes" :key="prize.id">
                  <strong>{{ prize.name }}</strong> ({{ formatPrizeType(prize.type) }})
                </li>
              </ul>
            </div>
          </v-card-text>

          <v-divider></v-divider>

          <!-- Card Footer Actions -->
          <v-card-actions class="pa-4 bg-grey-lighten-5">
            <v-btn
              color="primary"
              variant="elevated"
              block
              append-icon="mdi-arrow-right"
              class="font-weight-bold rounded-lg py-2"
              min-height="40px"
            >
              Gerenciar Evento
            </v-btn>
          </v-card-actions>
        </v-card>
      </v-col>
    </v-row>
  </v-container>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { useAdminStore } from '../../stores/admin'

const adminStore = useAdminStore()

onMounted(() => {
  adminStore.fetchEvents()
})

// Utility formatters
const formatDateTime = (isoString: string) => {
  if (!isoString) return ''
  const date = new Date(isoString)
  return date.toLocaleString('pt-BR', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const formatPrizeType = (type: string) => {
  switch (type) {
    case 'LINE':
      return 'Linha'
    case 'COLUMN':
      return 'Coluna'
    case 'FULL_CARD':
      return 'Cartela Cheia'
    default:
      return type
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

.border-subcard {
  border: 1px solid #E0E0E0 !important;
}

.border-dashed {
  border: 2px dashed #BDBDBD !important;
}

.transition-card {
  transition: all 0.25s ease-in-out;
}

.transition-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 16px rgba(26, 35, 126, 0.12) !important;
  border-color: #1A237E !important;
}

.shrink-0 {
  flex-shrink: 0;
}
</style>
