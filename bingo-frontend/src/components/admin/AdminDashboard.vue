<template>
  <v-container class="py-8">
    <!-- User Info & Logout Top Bar -->
    <v-row align="center" justify="space-between" class="mb-6 bg-indigo-darken-4 pa-4 rounded-xl text-white mx-0 elevation-2">
      <div class="d-flex align-center">
        <v-avatar color="indigo-lighten-4" class="mr-3">
          <v-icon icon="mdi-account" color="indigo-darken-4"></v-icon>
        </v-avatar>
        <div>
          <div class="text-subtitle-2 text-indigo-lighten-3 leading-none">Bem-vindo,</div>
          <div class="text-h6 font-weight-bold leading-none">{{ authStore.username }}</div>
        </div>
        <v-chip size="x-small" color="indigo-lighten-4" variant="flat" class="ml-3 font-weight-bold text-indigo-darken-4 text-uppercase">
          {{ authStore.role === 'ROLE_SUPER_ADMIN' ? 'SuperAdmin' : 'Admin' }}
        </v-chip>
      </div>
      <div class="d-flex align-center">
        <v-btn
          v-if="authStore.isSuperAdmin"
          color="teal-darken-1"
          prepend-icon="mdi-account-cog"
          class="mr-3 font-weight-bold text-white rounded-lg elevation-1"
          @click="openAdminsDialog"
        >
          Gerenciar Admins
        </v-btn>
        <v-btn
          v-if="authStore.isSuperAdmin"
          color="amber-darken-2"
          prepend-icon="mdi-account-plus"
          class="mr-3 font-weight-bold text-white rounded-lg elevation-1"
          to="/admin/register"
        >
          Cadastrar Admin
        </v-btn>
        <v-btn
          color="indigo-lighten-1"
          variant="flat"
          prepend-icon="mdi-logout"
          class="font-weight-bold text-white rounded-lg"
          @click="handleLogout"
        >
          Sair
        </v-btn>
      </div>
    </v-row>

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
    <v-row v-if="adminStore.isLoading && adminStore.events.length === 0">
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

    <!-- Events Filter Bar -->
    <v-row v-if="adminStore.events.length > 0" class="mb-4 align-center">
      <v-col cols="12" sm="8" class="d-flex flex-wrap gap-2 align-center">
        <v-btn-toggle
          v-model="selectedStatusFilter"
          color="indigo-darken-3"
          mandatory
          variant="outlined"
          density="comfortable"
          rounded="lg"
          class="border-subcard bg-white"
        >
          <v-btn value="ALL" class="text-capitalize font-weight-bold">
            Todos ({{ allCount }})
          </v-btn>
          <v-btn value="ACTIVE_WAITING" class="text-capitalize font-weight-bold">
            Ativos / Aguardando ({{ activeWaitingCount }})
          </v-btn>
          <v-btn value="FINALIZADO" class="text-capitalize font-weight-bold">
            Finalizados ({{ finalizedCount }})
          </v-btn>
        </v-btn-toggle>
      </v-col>

      <v-col cols="12" sm="4" class="d-flex justify-sm-end align-center gap-2">
        <v-btn
          v-if="hiddenCount > 0"
          :color="showHiddenEvents ? 'indigo-darken-3' : 'grey-darken-2'"
          :variant="showHiddenEvents ? 'flat' : 'outlined'"
          size="small"
          rounded="lg"
          prepend-icon="mdi-eye-off-outline"
          class="text-capitalize font-weight-bold"
          @click="showHiddenEvents = !showHiddenEvents"
        >
          {{ showHiddenEvents ? 'Voltar aos Eventos' : `Ocultos (${hiddenCount})` }}
        </v-btn>
        <v-btn
          v-if="showHiddenEvents && hiddenCount > 0"
          color="amber-darken-3"
          variant="text"
          size="small"
          prepend-icon="mdi-restore"
          class="text-capitalize font-weight-bold"
          @click="restoreAllHidden"
        >
          Restaurar Todos
        </v-btn>
      </v-col>
    </v-row>

    <!-- Empty Filtered Results State -->
    <v-row v-if="adminStore.events.length > 0 && filteredEvents.length === 0" justify="center" class="py-10">
      <v-col cols="12" md="6" class="text-center">
        <v-card class="pa-6 elevation-1 rounded-lg border-dashed">
          <v-icon icon="mdi-filter-variant-remove" size="48" color="grey-lighten-1" class="mb-3"></v-icon>
          <h3 class="text-h6 font-weight-bold mb-2">Nenhum evento nesta categoria</h3>
          <p class="text-body-2 text-grey-darken-1 mb-4">
            {{ showHiddenEvents ? 'Nenhum evento oculto no momento.' : 'Não há eventos com o status selecionado.' }}
          </p>
          <v-btn
            v-if="showHiddenEvents"
            color="primary"
            variant="outlined"
            size="small"
            class="text-capitalize font-weight-bold"
            @click="showHiddenEvents = false"
          >
            Voltar aos Eventos Ativos
          </v-btn>
          <v-btn
            v-else
            color="primary"
            variant="outlined"
            size="small"
            class="text-capitalize font-weight-bold"
            @click="selectedStatusFilter = 'ALL'"
          >
            Ver Todos os Eventos
          </v-btn>
        </v-card>
      </v-col>
    </v-row>

    <!-- Events Grid -->
    <v-row v-else-if="filteredEvents.length > 0">
      <v-col v-for="event in filteredEvents" :key="event.id" cols="12" md="6" lg="4">
        <v-card class="elevation-2 rounded-lg border-card h-100 d-flex flex-column transition-card">
          <!-- Card Header Banner -->
          <v-card-item class="bg-indigo-lighten-5 py-4 border-bottom-indigo">
            <div class="d-flex justify-space-between align-center">
              <v-card-title class="font-weight-bold text-h6 color-primary text-truncate pr-2">
                {{ event.title }}
              </v-card-title>
              <v-chip :color="getStatusColor(event.status || 'AGUARDANDO')" size="small" variant="flat" class="font-weight-bold shrink-0">
                {{ formatStatus(event.status || 'AGUARDANDO') }}
              </v-chip>
            </div>
          </v-card-item>

          <v-card-text class="py-4 flex-grow-1">
            <!-- Event Meta Data -->
            <div v-if="authStore.isSuperAdmin && event.adminUsername" class="d-flex align-center mb-3">
              <v-icon icon="mdi-account-tie" color="indigo-lighten-2" class="mr-2" size="20"></v-icon>
              <span class="text-caption font-weight-bold text-indigo-darken-4">
                Organizador: {{ event.adminUsername }}
              </span>
            </div>
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
          <v-card-actions class="pa-4 bg-grey-lighten-5 d-flex flex-column align-stretch">
            <v-btn
              color="indigo-darken-3"
              variant="flat"
              block
              append-icon="mdi-arrow-right"
              class="font-weight-bold rounded-lg py-2 mb-3 text-white"
              min-height="40px"
              :to="`/admin/events/${event.id}/console`"
            >
              Gerenciar Evento
            </v-btn>

            <!-- Dynamic Action Buttons instead of v-select -->
            <div class="mb-3 d-flex gap-2 w-100">
              <v-btn
                v-if="!event.status || event.status === 'AGUARDANDO'"
                color="green-darken-2"
                variant="flat"
                block
                prepend-icon="mdi-play"
                class="font-weight-bold rounded-lg text-white"
                @click="confirmStatusChange(event.id, 'ATIVO')"
              >
                Iniciar Evento
              </v-btn>
              <v-btn
                v-else-if="event.status === 'ATIVO'"
                color="orange-darken-3"
                variant="flat"
                block
                prepend-icon="mdi-stop"
                class="font-weight-bold rounded-lg text-white"
                @click="confirmStatusChange(event.id, 'FINALIZADO')"
              >
                Finalizar Evento
              </v-btn>
              <v-btn
                v-else-if="event.status === 'FINALIZADO'"
                color="grey-darken-1"
                variant="flat"
                block
                disabled
                prepend-icon="mdi-check-all"
                class="font-weight-bold rounded-lg text-white"
              >
                Evento Encerrado
              </v-btn>
            </div>

            <div class="d-flex justify-space-between w-100">
              <v-btn
                color="secondary"
                variant="outlined"
                prepend-icon="mdi-pencil"
                class="flex-grow-1 mr-1 rounded-lg"
                @click="openEditDialog(event)"
              >
                Editar
              </v-btn>
              <v-btn
                v-if="showHiddenEvents"
                color="indigo-darken-2"
                variant="outlined"
                prepend-icon="mdi-eye-outline"
                class="flex-grow-1 ml-1 rounded-lg"
                @click="unhideEvent(event.id)"
              >
                Reexibir
              </v-btn>
              <v-btn
                v-else-if="event.status === 'FINALIZADO'"
                color="blue-grey-darken-1"
                variant="outlined"
                prepend-icon="mdi-eye-off-outline"
                class="flex-grow-1 ml-1 rounded-lg"
                title="Ocultar este evento finalizado do painel"
                @click="hideEvent(event.id)"
              >
                Ocultar
              </v-btn>
              <v-btn
                v-else
                color="red-darken-1"
                variant="outlined"
                prepend-icon="mdi-delete"
                class="flex-grow-1 ml-1 rounded-lg"
                @click="confirmDelete(event.id)"
              >
                Excluir
              </v-btn>
            </div>
          </v-card-actions>
        </v-card>
      </v-col>
    </v-row>

    <!-- Edit Event Dialog -->
    <v-dialog v-model="editDialog" max-width="500px" persistent>
      <v-card class="rounded-xl pa-4">
        <v-card-title class="font-weight-bold text-h5 color-primary pb-2">
          Editar Evento
        </v-card-title>
        <v-card-text>
          <v-form ref="form" v-model="formValid">
            <v-text-field
              v-model="editForm.title"
              label="Título do Evento"
              variant="outlined"
              :rules="[v => !!v || 'O título é obrigatório']"
              class="mb-3 rounded-lg"
            ></v-text-field>
            
            <v-text-field
              v-model="editForm.scheduledAt"
              label="Data e Hora"
              type="datetime-local"
              variant="outlined"
              :rules="[v => !!v || 'A data é obrigatória']"
              class="mb-3 rounded-lg"
            ></v-text-field>

            <v-text-field
              v-model="editForm.youtubeUrl"
              label="URL da Stream (YouTube)"
              variant="outlined"
              placeholder="https://www.youtube.com/watch?v=..."
              class="mb-3 rounded-lg"
            ></v-text-field>
          </v-form>
        </v-card-text>
        <v-card-actions class="pt-0">
          <v-spacer></v-spacer>
          <v-btn color="grey-darken-1" variant="text" @click="editDialog = false">Cancelar</v-btn>
          <v-btn color="primary" variant="flat" class="rounded-lg px-4" :disabled="!formValid" @click="saveEditedEvent">Salvar</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
    <!-- Status Confirmation Dialog -->
    <v-dialog v-model="statusDialog.show" max-width="400px" persistent>
      <v-card class="rounded-xl pa-4">
        <v-card-title class="font-weight-bold text-h6 pb-2" :class="statusDialog.targetStatus === 'ATIVO' ? 'text-green-darken-2' : 'text-orange-darken-3'">
          <v-icon :icon="statusDialog.targetStatus === 'ATIVO' ? 'mdi-play' : 'mdi-alert'" class="mr-2"></v-icon>
          Confirmar Ação
        </v-card-title>
        <v-card-text class="pt-2">
          Tem certeza que deseja {{ statusDialog.targetStatus === 'ATIVO' ? 'iniciar' : 'finalizar' }} este evento?
          <template v-if="statusDialog.targetStatus === 'FINALIZADO'">
            <br><br><strong class="text-red">Atenção:</strong> Esta ação é permanente e impedirá novas jogadas.
          </template>
        </v-card-text>
        <v-card-actions class="pt-0">
          <v-spacer></v-spacer>
          <v-btn color="grey-darken-1" variant="text" @click="statusDialog.show = false">Cancelar</v-btn>
          <v-btn :color="statusDialog.targetStatus === 'ATIVO' ? 'green-darken-2' : 'orange-darken-3'" variant="flat" class="rounded-lg px-4 text-white" @click="executeStatusChange">
            Confirmar
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- Gerenciamento de Administradores Dialog -->
    <v-dialog v-model="isAdminsDialogOpen" max-width="900px" persistent>
      <v-card class="rounded-xl pa-5 text-white bg-slate-900 border-card-premium">
        <div class="d-flex justify-space-between align-center border-bottom-indigo pb-3 mb-4">
          <v-card-title class="text-h5 font-weight-bold text-primary pa-0 d-flex align-center">
            <v-icon icon="mdi-account-cog" class="mr-2" color="primary"></v-icon>
            Gerenciar Administradores
          </v-card-title>
          <v-btn icon="mdi-close" variant="text" color="grey-lighten-1" @click="isAdminsDialogOpen = false"></v-btn>
        </div>

        <v-card-text class="pa-0">
          <v-table class="bg-transparent text-white border-subcard rounded-lg overflow-hidden">
            <thead class="bg-indigo-darken-4">
              <tr>
                <th class="text-left font-weight-bold text-white">Usuário</th>
                <th class="text-left font-weight-bold text-white">E-mail</th>
                <th class="text-left font-weight-bold text-white">Tipo</th>
                <th class="text-center font-weight-bold text-white">Status</th>
                <th class="text-center font-weight-bold text-white">Ações</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="admin in adminStore.managedAdmins" :key="admin.id" class="border-bottom-grey hover-row">
                <td class="font-weight-bold text-capitalize text-indigo-darken-4">{{ admin.username }}</td>
                <td class="font-weight text-indigo-darken-1">{{ admin.email }}</td>
                <td>
                  <v-chip
                    size="small"
                    :color="admin.role === 'ROLE_SUPER_ADMIN' ? 'amber-darken-3' : 'indigo-lighten-2'"
                    variant="flat"
                    class="font-weight-bold text-white text-uppercase"
                  >
                    {{ admin.role === 'ROLE_SUPER_ADMIN' ? 'SuperAdmin' : 'Admin' }}
                  </v-chip>
                </td>
                <td class="text-center">
                  <v-chip
                    size="small"
                    :color="admin.active ? 'green-darken-1' : 'red-darken-1'"
                    variant="flat"
                    class="font-weight-bold text-white"
                  >
                    {{ admin.active ? 'Ativo' : 'Inativo' }}
                  </v-chip>
                </td>
                <td class="text-center">
                  <div class="d-flex justify-center gap-2 align-center py-1">
                    <!-- Toggle Active Switch or Button -->
                    <v-btn
                      :color="admin.active ? 'warning' : 'success'"
                      variant="outlined"
                      size="x-small"
                      class="font-weight-bold text-capitalize"
                      :disabled="admin.username === authStore.username || adminStore.isLoading"
                      @click="handleToggleActive(admin.id)"
                    >
                      {{ admin.active ? 'Desativar' : 'Ativar' }}
                    </v-btn>

                    <!-- Send Password Reset Code -->
                    <v-btn
                      color="indigo-lighten-3"
                      variant="flat"
                      size="x-small"
                      prepend-icon="mdi-email-key"
                      class="font-weight-bold text-capitalize text-indigo-darken-4"
                      :disabled="adminStore.isLoading"
                      @click="handleSendResetCode(admin.id)"
                    >
                      Código
                    </v-btn>

                    <!-- Delete -->
                    <v-btn
                      icon="mdi-delete"
                      variant="text"
                      color="red-lighten-1"
                      size="small"
                      :disabled="admin.username === authStore.username || adminStore.isLoading"
                      @click="handleDeleteAdmin(admin.id)"
                    ></v-btn>
                  </div>
                </td>
              </tr>
              <tr v-if="adminStore.managedAdmins.length === 0">
                <td colspan="5" class="text-center py-4 text-grey-darken-1">
                  Nenhum administrador cadastrado.
                </td>
              </tr>
            </tbody>
          </v-table>
        </v-card-text>

        <v-card-actions class="px-0 pt-4">
          <v-spacer></v-spacer>
          <v-btn color="grey-lighten-1" variant="outlined" class="rounded-lg font-weight-bold" @click="isAdminsDialogOpen = false">
            Fechar
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </v-container>
</template>

<script setup lang="ts">
import { onMounted, ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAdminStore } from '../../stores/admin'
import { useAuthStore } from '../../stores/auth'

const adminStore = useAdminStore()
const authStore = useAuthStore()
const router = useRouter()

// Status and visibility filtering
const selectedStatusFilter = ref<'ALL' | 'ACTIVE_WAITING' | 'FINALIZADO'>('ALL')
const showHiddenEvents = ref(false)
const hiddenEventIds = ref<string[]>(JSON.parse(localStorage.getItem('admin_hidden_events') || '[]'))

const hideEvent = (eventId: string) => {
  if (!hiddenEventIds.value.includes(eventId)) {
    hiddenEventIds.value.push(eventId)
    localStorage.setItem('admin_hidden_events', JSON.stringify(hiddenEventIds.value))
  }
}

const unhideEvent = (eventId: string) => {
  hiddenEventIds.value = hiddenEventIds.value.filter(id => id !== eventId)
  localStorage.setItem('admin_hidden_events', JSON.stringify(hiddenEventIds.value))
  if (hiddenEventIds.value.length === 0) {
    showHiddenEvents.value = false
  }
}

const restoreAllHidden = () => {
  hiddenEventIds.value = []
  localStorage.removeItem('admin_hidden_events')
  showHiddenEvents.value = false
}

const allCount = computed(() => {
  return adminStore.events.filter(e => !hiddenEventIds.value.includes(e.id)).length
})

const activeWaitingCount = computed(() => {
  return adminStore.events.filter(e => !hiddenEventIds.value.includes(e.id) && (e.status === 'ATIVO' || !e.status || e.status === 'AGUARDANDO')).length
})

const finalizedCount = computed(() => {
  return adminStore.events.filter(e => !hiddenEventIds.value.includes(e.id) && e.status === 'FINALIZADO').length
})

const hiddenCount = computed(() => hiddenEventIds.value.length)

const filteredEvents = computed(() => {
  return adminStore.events.filter(event => {
    const isHidden = hiddenEventIds.value.includes(event.id)
    if (showHiddenEvents.value) {
      return isHidden
    }
    if (isHidden) {
      return false
    }

    const status = event.status || 'AGUARDANDO'
    if (selectedStatusFilter.value === 'ACTIVE_WAITING') {
      return status === 'ATIVO' || status === 'AGUARDANDO'
    }
    if (selectedStatusFilter.value === 'FINALIZADO') {
      return status === 'FINALIZADO'
    }
    return true
  })
})

const handleLogout = async () => {
  await authStore.logout()
  router.push('/admin/login')
}

const editDialog = ref(false)
const formValid = ref(false)
const editingEventId = ref<string | null>(null)
const editForm = ref({
  title: '',
  scheduledAt: '',
  youtubeUrl: ''
})

const isAdminsDialogOpen = ref(false)

const openAdminsDialog = async () => {
  isAdminsDialogOpen.value = true
  await adminStore.fetchManagedAdmins()
}

const handleToggleActive = async (adminId: string) => {
  const confirmAction = window.confirm('Deseja realmente alterar o status ativo deste administrador?')
  if (confirmAction) {
    await adminStore.toggleAdminActive(adminId)
  }
}

const handleSendResetCode = async (adminId: string) => {
  const confirmAction = window.confirm('Deseja realmente gerar um código de recuperação de senha para este administrador? O código será enviado para o e-mail cadastrado.')
  if (confirmAction) {
    const success = await adminStore.sendAdminResetCode(adminId)
    if (success) {
      alert('Código gerado e enviado com sucesso para o e-mail cadastrado!')
    }
  }
}

const handleDeleteAdmin = async (adminId: string) => {
  const confirmAction = window.confirm('Deseja realmente EXCLUIR este administrador definitivamente? Esta ação não pode ser desfeita.')
  if (confirmAction) {
    await adminStore.deleteAdmin(adminId)
  }
}

onMounted(() => {
  adminStore.fetchEvents()
})

const openEditDialog = (event: any) => {
  editingEventId.value = event.id
  let formattedDate = ''
  if (event.scheduledAt) {
    const d = new Date(event.scheduledAt)
    const pad = (n: number) => n.toString().padStart(2, '0')
    // Local date string format: YYYY-MM-DDTHH:MM
    formattedDate = `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`
  }
  editForm.value = {
    title: event.title,
    scheduledAt: formattedDate,
    youtubeUrl: event.youtubeUrl || ''
  }
  editDialog.value = true
}

const saveEditedEvent = async () => {
  if (!editingEventId.value) return
  const event = adminStore.events.find(e => e.id === editingEventId.value)
  const dateObj = new Date(editForm.value.scheduledAt)
  const payload = {
    title: editForm.value.title,
    scheduledAt: dateObj.toISOString(),
    youtubeUrl: editForm.value.youtubeUrl,
    rounds: event ? event.rounds.map((r: any) => ({
      roundNumber: r.roundNumber,
      prizes: r.prizes.map((p: any) => ({
        name: p.name,
        type: p.type,
        drawPattern: p.drawPattern
      }))
    })) : []
  }
  const success = await adminStore.updateEvent(editingEventId.value, payload)
  if (success) {
    editDialog.value = false
    editingEventId.value = null
  }
}

const confirmDelete = async (eventId: string) => {
  if (window.confirm("Você tem certeza que deseja excluir este evento permanentemente? Todos os dados vinculados serão perdidos.")) {
    await adminStore.deleteEvent(eventId)
  }
}

const statusDialog = ref({
  show: false,
  eventId: '',
  targetStatus: ''
})

const confirmStatusChange = (eventId: string, targetStatus: string) => {
  statusDialog.value.eventId = eventId
  statusDialog.value.targetStatus = targetStatus
  statusDialog.value.show = true
}

const executeStatusChange = async () => {
  statusDialog.value.show = false
  await adminStore.updateEventStatus(statusDialog.value.eventId, statusDialog.value.targetStatus)
}

const getStatusColor = (status: string) => {
  switch (status) {
    case 'ATIVO':
      return 'success'
    case 'FINALIZADO':
      return 'error'
    case 'AGUARDANDO':
    default:
      return 'warning'
  }
}

const formatStatus = (status: string) => {
  switch (status) {
    case 'ATIVO':
      return 'Ativo'
    case 'FINALIZADO':
      return 'Finalizado'
    case 'AGUARDANDO':
      return 'Aguardando'
    default:
      return status
  }
}

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
