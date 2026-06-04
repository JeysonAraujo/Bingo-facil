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
          Console de Controle Ao Vivo
        </h1>
        <p class="text-subtitle-2 text-slate-400 mb-0">
          Painel de Gerenciamento do Evento • Evento: <span class="text-amber-lighten-2 font-weight-bold">{{ eventTitle }}</span>
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
      <!-- LEFT COLUMN: Drawing Board Grid & Administrative Tabs -->
      <v-col cols="12" lg="8" class="d-flex flex-column gap-6">
        <v-card class="bg-slate-900 border-slate-800 rounded-xl overflow-hidden shadow-2xl">
          <!-- Console Navigation Tabs -->
          <v-tabs
            v-model="activeTab"
            bg-color="#0b1329"
            color="amber-darken-1"
            grow
            class="border-bottom-slate"
          >
            <v-tab value="draw" class="font-weight-bold text-capitalize text-white">
              <v-icon start>mdi-grid</v-icon>
              Painel de Sorteio (BINGO)
            </v-tab>
            <v-tab value="announcement" class="font-weight-bold text-capitalize text-white">
              <v-icon start>mdi-bullhorn-outline</v-icon>
              Transmissão de Anúncios
            </v-tab>
            <v-tab value="override" class="font-weight-bold text-capitalize text-white">
              <v-icon start>mdi-ticket-percent-outline</v-icon>
              Overrides de Cartela
            </v-tab>
          </v-tabs>

          <v-window v-model="activeTab" class="pa-6">
            <!-- TAB 1: Live Sorteio Grid Board -->
            <v-window-item value="draw">
              <!-- Pending Draw Alert Banner -->
              <div v-if="pendingBall !== null" class="pending-alert-banner py-3 px-6 rounded-lg d-flex justify-between align-center mb-6">
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
              <div class="d-flex align-center justify-between mt-6 text-caption text-slate-400 border-t-slate pt-4 flex-wrap gap-2">
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
            </v-window-item>

            <!-- TAB 2: Custom Message Broadcaster -->
            <v-window-item value="announcement">
              <div class="d-flex flex-column gap-4 py-2">
                <h3 class="text-subtitle-1 font-weight-bold text-white d-flex align-center">
                  <v-icon icon="mdi-message-text-outline" color="amber-darken-1" class="mr-2" size="20"></v-icon>
                  Broadcast de Anúncios da Sala
                </h3>
                <p class="text-caption text-slate-400 mb-2">
                  Envie mensagens personalizadas instantaneamente para todos os jogadores. O texto rolará continuamente em um ticker destacado no topo do Game Room deles.
                </p>

                <v-textarea
                  v-model="announcementText"
                  label="Mensagem de Anúncio"
                  placeholder="Ex: Rodada Especial de Inauguração! O prêmio de hoje é um Pix de R$ 500! Fiquem atentos!"
                  variant="outlined"
                  bg-color="slate-950"
                  color="amber-darken-1"
                  rows="3"
                  class="rounded-lg text-white"
                  persistent-placeholder
                  hide-details
                ></v-textarea>

                <div class="d-flex justify-end gap-2 mt-2">
                  <v-btn
                    variant="text"
                    color="slate-400"
                    class="font-weight-bold text-capitalize"
                    @click="announcementText = ''"
                  >
                    Limpar
                  </v-btn>
                  <v-btn
                    color="amber-darken-2"
                    prepend-icon="mdi-send"
                    class="font-weight-bold text-capitalize text-slate-950 px-6 rounded-lg"
                    min-height="40px"
                    :disabled="!announcementText.trim()"
                    @click="submitAnnouncement"
                  >
                    Transmitir Anúncio
                  </v-btn>
                </div>

                <!-- Local announcements log -->
                <div class="mt-6">
                  <h4 class="text-subtitle-2 font-weight-bold text-slate-200 mb-3">
                    Histórico de Encomendas Locais
                  </h4>
                  <div v-if="localAnnouncements.length === 0" class="text-caption text-slate-500 py-4 text-center border-dashed rounded-lg bg-slate-950 border-slate-800">
                    Nenhum anúncio enviado nesta sessão.
                  </div>
                  <div v-else class="d-flex flex-column gap-2">
                    <div
                      v-for="(msg, index) in localAnnouncements"
                      :key="index"
                      class="d-flex justify-between align-center bg-slate-950 border-slate-800 pa-3 rounded-lg text-caption"
                    >
                      <span class="text-slate-300 font-weight-medium text-wrap">{{ msg.text }}</span>
                      <span class="text-slate-500 text-xs shrink-0 ml-2">{{ msg.time }}</span>
                    </div>
                  </div>
                </div>
              </div>
            </v-window-item>

            <!-- TAB 3: Force Overrides panel & Ticket Search -->
            <v-window-item value="override">
              <div class="d-flex flex-column gap-4 py-2">
                <h3 class="text-subtitle-1 font-weight-bold text-white d-flex align-center">
                  <v-icon icon="mdi-magnify" color="amber-darken-1" class="mr-2" size="20"></v-icon>
                  Pesquisar Cartela para Auditoria e Overrides
                </h3>
                <p class="text-caption text-slate-400 mb-2">
                  Pesquise uma cartela pelo serial de 6 caracteres para visualizar a sua grade 5x5 e forçar a marcação/desmarcação manual de células.
                </p>

                <!-- Search bar input -->
                <div class="d-flex gap-3 align-center mb-4">
                  <v-text-field
                    v-model="searchSerial"
                    label="Código de Serial da Cartela"
                    placeholder="Ex: AB24EF"
                    variant="outlined"
                    bg-color="slate-950"
                    color="amber-darken-1"
                    class="rounded-lg text-white"
                    hide-details
                    maxlength="6"
                    @keyup.enter="performTicketSearch"
                  ></v-text-field>
                  <v-btn
                    color="indigo-darken-3"
                    size="large"
                    prepend-icon="mdi-magnify"
                    class="rounded-lg text-white font-weight-bold px-6"
                    min-height="56px"
                    :loading="adminStore.isLoading"
                    :disabled="searchSerial.trim().length !== 6"
                    @click="performTicketSearch"
                  >
                    Pesquisar
                  </v-btn>
                </div>

                <!-- Error Alert in search -->
                <v-alert
                  v-if="searchError"
                  type="warning"
                  variant="tonal"
                  density="compact"
                  closable
                  class="rounded-lg mb-2 text-caption"
                  @click:close="searchError = ''"
                >
                  {{ searchError }}
                </v-alert>

                <!-- Search Result Detail view -->
                <div v-if="adminStore.searchedTicket" class="searched-ticket-card pa-6 rounded-xl bg-slate-950 border-slate-800 mt-2">
                  <div class="d-flex justify-between align-center mb-6 flex-wrap gap-2 border-bottom-slate pb-4">
                    <div>
                      <div class="text-caption text-slate-400">Cartela Serial</div>
                      <div class="text-h6 font-weight-black text-amber-lighten-2">
                        {{ adminStore.searchedTicket.serial }}
                      </div>
                    </div>
                    <div class="text-right">
                      <div class="text-caption text-slate-400">Status de Ativação</div>
                      <v-chip
                        :color="adminStore.searchedTicket.activated ? 'success' : 'grey-darken-2'"
                        size="small"
                        variant="flat"
                        class="font-weight-black mt-1"
                      >
                        {{ adminStore.searchedTicket.activated ? 'Ativa / Jogando' : 'Inativa / Não Ativada' }}
                      </v-chip>
                    </div>
                  </div>

                  <!-- Grid 5x5 Card Viewer -->
                  <div class="override-grid-outer d-flex justify-center my-4">
                    <div class="override-bingo-card">
                      <!-- Grid Headers -->
                      <div class="override-card-header">
                        <span v-for="letter in ['B', 'I', 'N', 'G', 'O']" :key="letter">{{ letter }}</span>
                      </div>
                      
                      <!-- 5x5 cells -->
                      <div class="override-card-grid">
                        <div
                          v-for="cell in ticketCells"
                          :key="`${cell.letter}-${cell.value}`"
                          class="override-cell"
                          :class="{
                            'cell--free': cell.value === 0,
                            'cell--drawn': isDrawn(cell.value) && cell.value !== 0,
                            'cell--overridden': isOverridden(cell.value) && cell.value !== 0
                          }"
                          @click="toggleCellOverride(cell.value)"
                          :title="getCellTitle(cell.value)"
                        >
                          <span v-if="cell.value === 0" class="free-text-star">
                            <v-icon size="x-small">mdi-star</v-icon>
                          </span>
                          <span v-else class="cell-num">{{ cell.value }}</span>

                          <!-- Override indicator lock icon -->
                          <v-icon
                            v-if="isOverridden(cell.value) && cell.value !== 0"
                            size="x-small"
                            class="cell-override-lock-icon"
                          >
                            mdi-lock
                          </v-icon>
                        </div>
                      </div>
                    </div>
                  </div>

                  <!-- Bottom indicators -->
                  <div class="d-flex align-center justify-between mt-6 text-caption text-slate-500 border-t-slate pt-4">
                    <span class="d-flex align-center gap-1">
                      <span class="color-dot bg-green"></span> Marcação Padrão (Sorteada no Round)
                    </span>
                    <span class="d-flex align-center gap-1">
                      <span class="color-dot bg-purple"></span> Override Manual (Forçado pelo Coordenador)
                    </span>
                  </div>
                  <p class="text-caption text-slate-400 mt-4 mb-0 text-center font-weight-medium bg-slate-900 border-slate pa-3 rounded-lg">
                    💡 Dica: Dê um clique em qualquer célula para forçar a marcação (Glow Roxo) ou limpar o override. O jogador receberá a mudança reativa instantaneamente no Cartelão dele!
                  </p>
                </div>
              </div>
            </v-window-item>
          </v-window>
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

        <!-- Fila de Reivindicações (Claims Queue) -->
        <v-card class="bg-slate-900 border-slate-800 rounded-xl pa-6 shadow-2xl d-flex flex-column mb-6">
          <div class="d-flex justify-between align-center mb-4">
            <h2 class="text-h6 font-weight-bold text-white d-flex align-center mb-0">
              <v-icon icon="mdi-trophy-outline" class="mr-2" color="amber-darken-1"></v-icon>
              Reivindicações de Bingo
            </h2>
            <v-chip color="amber-darken-2" size="small" class="text-slate-950 font-weight-bold">
              {{ adminStore.claims.filter(c => c.status === 'PENDING').length }} Pendentes
            </v-chip>
          </div>

          <!-- Empty Claims State -->
          <div v-if="adminStore.claims.length === 0" class="d-flex flex-column align-center justify-center py-6 text-center">
            <v-icon icon="mdi-help-circle-outline" size="36" color="slate-700" class="mb-2"></v-icon>
            <span class="text-subtitle-2 text-slate-500 font-weight-bold">Nenhuma reivindicação recebida</span>
          </div>

          <!-- Claims Queue Scroll Area -->
          <div v-else class="claims-list-area d-flex flex-column gap-2 overflow-y-auto" style="max-height: 250px;">
            <div
              v-for="claim in adminStore.claims"
              :key="claim.claimId"
              class="d-flex justify-between align-center bg-slate-950 border-slate-800 pa-3 rounded-lg"
            >
              <div>
                <div class="text-caption text-slate-400">Cartela Serial</div>
                <div class="text-subtitle-2 font-weight-black text-amber-lighten-2">
                  {{ claim.serial }} (Card {{ claim.cardIndex + 1 }})
                </div>
              </div>
              <div class="d-flex align-center gap-2">
                <v-chip
                  v-if="claim.status !== 'PENDING'"
                  :color="claim.status === 'APPROVED' ? 'success' : 'error'"
                  size="small"
                  class="font-weight-black"
                >
                  {{ claim.status }}
                </v-chip>
                <v-btn
                  v-if="claim.status === 'PENDING'"
                  color="indigo-darken-3"
                  size="small"
                  class="font-weight-bold rounded-lg text-white"
                  @click="openAuditModal(claim)"
                >
                  Auditar
                </v-btn>
              </div>
            </div>
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

    <!-- Dialog de Auditoria Lado a Lado -->
    <v-dialog v-model="isAuditModalOpen" max-width="850px" persistent>
      <v-card class="bg-slate-900 border-slate-800 rounded-xl pa-6 text-white">
        <div class="d-flex justify-between align-center border-bottom-slate pb-4 mb-6">
          <h3 class="text-h5 font-weight-bold d-flex align-center">
            <v-icon icon="mdi-shield-check-outline" color="amber-darken-1" class="mr-2"></v-icon>
            Auditoria da Reivindicação - Cartela {{ auditingClaim?.serial }}
          </h3>
          <v-btn icon="mdi-close" variant="text" color="slate-400" @click="closeAuditModal"></v-btn>
        </div>

        <v-row>
          <!-- LEFT SIDE: Player's Card Grid -->
          <v-col cols="12" md="6" class="d-flex flex-column align-center">
            <h4 class="text-subtitle-1 font-weight-bold mb-4 text-slate-200">
              Grade da Cartela (Card {{ auditingClaim ? auditingClaim.cardIndex + 1 : 1 }})
            </h4>
            
            <div class="override-bingo-card">
              <div class="override-card-header">
                <span v-for="letter in ['B', 'I', 'N', 'G', 'O']" :key="letter">{{ letter }}</span>
              </div>
              <div class="override-card-grid">
                <div
                  v-for="cell in auditTicketCells"
                  :key="`${cell.letter}-${cell.value}`"
                  class="override-cell"
                  :class="{
                    'cell--free': cell.value === 0,
                    'cell--drawn': isDrawn(cell.value) && cell.value !== 0 && !isAuditMatch(cell.value),
                    'cell--overridden': isOverridden(cell.value) && cell.value !== 0 && !isAuditMatch(cell.value),
                    'cell--highlight-match': isAuditMatch(cell.value)
                  }"
                >
                  <span v-if="cell.value === 0" class="free-text-star">
                    <v-icon size="x-small">mdi-star</v-icon>
                  </span>
                  <span v-else class="cell-num">{{ cell.value }}</span>

                  <!-- lock icon if overridden -->
                  <v-icon
                    v-if="isOverridden(cell.value) && cell.value !== 0"
                    size="x-small"
                    class="cell-override-lock-icon"
                  >
                    mdi-lock
                  </v-icon>
                </div>
              </div>
            </div>

            <!-- Legend/Indicators -->
            <div class="d-flex gap-4 mt-6 text-caption text-slate-400">
              <span class="d-flex align-center gap-1">
                <span class="color-dot bg-green"></span> Sorteada
              </span>
              <span class="color-dot bg-purple"></span> Override
              <span class="d-flex align-center gap-1">
                <span class="color-dot bg-amber"></span> Match no Bingo!
              </span>
            </div>
          </v-col>

          <!-- RIGHT SIDE: Chronological Drawn Numbers -->
          <v-col cols="12" md="6" class="d-flex flex-column">
            <h4 class="text-subtitle-1 font-weight-bold mb-4 text-slate-200">
              Sorteios Cronológicos
            </h4>

            <div class="history-scroll-area flex-grow-1 bg-slate-950 border-slate rounded-lg pa-4 overflow-y-auto" style="height: 300px;">
              <div v-if="adminStore.drawnConsoleNumbers.length === 0" class="text-caption text-slate-500 py-8 text-center">
                Nenhuma bola sorteada ainda.
              </div>
              <div v-else class="d-flex flex-column gap-2">
                <div
                  v-for="(ball, idx) in sortedChronologicalDraws"
                  :key="ball"
                  class="d-flex justify-between align-center pa-2 rounded-lg"
                  :class="isBallOnAuditingCard(ball) ? 'bg-amber-darken-4 border-amber' : 'bg-slate-900 border-slate-800'"
                >
                  <div class="d-flex align-center gap-2">
                    <span class="text-caption text-slate-500 font-weight-bold">{{ idx + 1 }}º</span>
                    <span class="font-weight-black" :class="isBallOnAuditingCard(ball) ? 'text-amber-lighten-3' : 'text-slate-300'">
                      {{ getLetterForBall(ball) }}-{{ ball }}
                    </span>
                  </div>
                  <v-chip
                    v-if="isBallOnAuditingCard(ball)"
                    color="amber"
                    variant="flat"
                    size="x-small"
                    class="font-weight-black text-slate-950"
                  >
                    Match
                  </v-chip>
                  <span v-else class="text-caption text-slate-600">Não está na cartela</span>
                </div>
              </div>
            </div>
          </v-col>
        </v-row>

        <!-- Actions -->
        <div class="d-flex justify-end gap-3 border-t-slate pt-6 mt-6">
          <v-btn
            color="grey-darken-3"
            class="font-weight-bold text-capitalize rounded-lg px-6"
            @click="closeAuditModal"
          >
            Fechar
          </v-btn>
          <v-btn
            color="red-darken-3"
            class="font-weight-bold text-capitalize rounded-lg px-6 text-white"
            :loading="isAuditSubmitting"
            @click="handleRejectClaim"
          >
            Rejeitar Reivindicação
          </v-btn>
          <v-btn
            color="emerald-darken-2"
            class="font-weight-bold text-capitalize rounded-lg px-6 text-white bg-emerald-darken-2"
            :loading="isAuditSubmitting"
            @click="handleApproveClaim"
          >
            Aprovar Vencedor!
          </v-btn>
        </div>
      </v-card>
    </v-dialog>
  </v-container>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAdminStore } from '@/stores/admin'

const route = useRoute()
const router = useRouter()
const adminStore = useAdminStore()

const eventId = route.params.eventId as string

// Active console state
const activeTab = ref('draw')
const pendingBall = ref<number | null>(null)
const typedInput = ref<number | null>(null)
let doubleClickTimer: number | null = null
let keyClearTimer: number | null = null
let typedAccumulator = ''

// Announcements tab state
const announcementText = ref('')
interface LocalAnnouncement {
  text: string
  time: string
}
const localAnnouncements = ref<LocalAnnouncement[]>([])

// Overrides tab state
const searchSerial = ref('')
const searchError = ref('')

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

// Maps a 5x5 card layout for the searched active ticket
interface Cell {
  letter: string
  value: number
}
const ticketCells = computed<Cell[]>(() => {
  const ticket = adminStore.searchedTicket
  if (!ticket || !ticket.matrix || !ticket.matrix.cards || ticket.matrix.cards.length === 0) {
    return []
  }

  const card = ticket.matrix.cards[0]
  const cells: Cell[] = []
  
  // Format cells by grid rows (index 0 to 4)
  for (let row = 0; row < 5; row++) {
    cells.push({ letter: 'B', value: card.b[row] })
    cells.push({ letter: 'I', value: card.i[row] })
    cells.push({ letter: 'N', value: row === 2 ? 0 : card.n[row] }) // Middle element row 2 column 2 is 0 (FREE space)
    cells.push({ letter: 'G', value: card.g[row] })
    cells.push({ letter: 'O', value: card.o[row] })
  }

  return cells
})

// Audit / claims reactive state
const isAuditModalOpen = ref(false)
const auditingClaim = ref<any | null>(null)
const isAuditSubmitting = ref(false)

const openAuditModal = async (claim: any) => {
  auditingClaim.value = claim
  isAuditModalOpen.value = true
  searchError.value = ''
  await adminStore.searchTicketBySerial(claim.serial)
}

const closeAuditModal = () => {
  isAuditModalOpen.value = false
  auditingClaim.value = null
}

const handleApproveClaim = async () => {
  if (!auditingClaim.value) return
  isAuditSubmitting.value = true
  try {
    const success = await adminStore.approveClaim(auditingClaim.value.claimId)
    if (success) {
      closeAuditModal()
    }
  } finally {
    isAuditSubmitting.value = false
  }
}

const handleRejectClaim = async () => {
  if (!auditingClaim.value) return
  isAuditSubmitting.value = true
  try {
    const success = await adminStore.rejectClaim(auditingClaim.value.claimId)
    if (success) {
      closeAuditModal()
    }
  } finally {
    isAuditSubmitting.value = false
  }
}

const sortedChronologicalDraws = computed(() => {
  return adminStore.drawnConsoleNumbers
})

const auditTicketCells = computed<Cell[]>(() => {
  const ticket = adminStore.searchedTicket
  const claim = auditingClaim.value
  if (!ticket || !ticket.matrix || !ticket.matrix.cards || !claim) {
    return []
  }
  const cardIndex = claim.cardIndex ?? 0
  const card = ticket.matrix.cards[cardIndex]
  if (!card) return []
  const cells: Cell[] = []
  for (let row = 0; row < 5; row++) {
    cells.push({ letter: 'B', value: card.b[row] })
    cells.push({ letter: 'I', value: card.i[row] })
    cells.push({ letter: 'N', value: row === 2 ? 0 : card.n[row] })
    cells.push({ letter: 'G', value: card.g[row] })
    cells.push({ letter: 'O', value: card.o[row] })
  }
  return cells
})

const isBallOnAuditingCard = (ball: number) => {
  return auditTicketCells.value.some(cell => cell.value === ball && ball !== 0)
}

const isAuditMatch = (cellValue: number) => {
  return cellValue !== 0 && isDrawn(cellValue) && isBallOnAuditingCard(cellValue)
}

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

  // Fetch initial claims for the round
  const event = adminStore.activeConsoleEvent
  if (event && event.rounds && event.rounds.length > 0) {
    const activeRound = event.rounds.find(r => r.status === 'IN_PROGRESS') || event.rounds[0]
    if (activeRound) {
      await adminStore.fetchClaims(activeRound.id)
    }
  }

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

const isOverridden = (ball: number) => {
  const ticket = adminStore.searchedTicket
  return ticket && ticket.markedOverrides && ticket.markedOverrides.includes(ball)
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

const getCellTitle = (ball: number) => {
  if (ball === 0) return 'FREE SPACE'
  if (isDrawn(ball)) return `Bola ${ball} (Sorteada no Round)`
  if (isOverridden(ball)) return `Bola ${ball} (Marcada via Override Manual) • Clique para remover override`
  return `Bola ${ball} (Não marcada) • Clique para forçar marcação manual`
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

// Announcements triggers
const submitAnnouncement = () => {
  const text = announcementText.value.trim()
  if (!text) return

  adminStore.sendAnnouncement(eventId, text)

  // Log locally
  const now = new Date()
  const timeStr = `${now.getHours().toString().padStart(2, '0')}:${now.getMinutes().toString().padStart(2, '0')}`
  localAnnouncements.value.unshift({ text, time: timeStr })

  // Clear
  announcementText.value = ''
}

// Ticket Search triggers
const performTicketSearch = async () => {
  searchError.value = ''
  const serial = searchSerial.value.trim()
  if (serial.length !== 6) return

  const success = await adminStore.searchTicketBySerial(serial)
  if (!success) {
    searchError.value = adminStore.error || 'Cartela não encontrada.'
  }
}

// Cell Override triggers
const toggleCellOverride = (ball: number) => {
  if (ball === 0 || !adminStore.searchedTicket) return

  const ticketId = adminStore.searchedTicket.id
  const action = isOverridden(ball) ? 'UNMARK' : 'MARK'

  adminStore.sendTicketOverride(ticketId, ball, action)
}

/**
 * Administrative Global keyboard hotkeys (active ONLY on BINGO Draw Board tab):
 * - Typing 1-75 selects it as pending selection.
 * - Pressing 'Enter' confirms pending ball.
 * - Pressing 'Esc' cancels pending/typed state.
 * - Pressing 'Ctrl+Z' triggers the UNDO operation.
 */
const handleGlobalKeydown = (event: KeyboardEvent) => {
  // Lock hotkeys if focused on textarea or text fields (e.g. search / announcemet text fields)
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

  // Only active numeric keyboard sorting inside Bingo Sorteio tab
  if (activeTab.value !== 'draw') return

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

let debounceTimer: number | null = null

watch(
  () => adminStore.latencySeconds,
  (newVal) => {
    if (debounceTimer) clearTimeout(debounceTimer)
    debounceTimer = window.setTimeout(async () => {
      await adminStore.updateStreamDelay(eventId, newVal)
    }, 500)
  }
)
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

.border-bottom-slate {
  border-bottom: 1px solid rgba(255, 255, 255, 0.08) !important;
}

.text-slate-400 {
  color: #94a3b8;
}

.text-slate-300 {
  color: #cbd5e1;
}

.text-slate-200 {
  color: #e2e8f0;
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

.border-slate {
  border: 1px solid rgba(255, 255, 255, 0.05);
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
  height: 480px;
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

/* 5x5 Override Card Grid layout */
.searched-ticket-card {
  background-color: #080f20;
  border: 1px solid rgba(255, 255, 255, 0.06);
}

.override-bingo-card {
  width: 280px;
  background: #0f172a;
  border: 3px solid #1e293b;
  border-radius: 16px;
  padding: 12px;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.4);
}

.override-card-header {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 6px;
  text-align: center;
  font-weight: 900;
  font-size: 1.25rem;
  color: #ffab00;
  margin-bottom: 8px;
  user-select: none;
}

.override-card-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 6px;
}

.override-cell {
  aspect-ratio: 1;
  background: #1e293b;
  border: 1px solid rgba(255, 255, 255, 0.04);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 800;
  font-size: 0.95rem;
  color: #f1f5f9;
  cursor: pointer;
  position: relative;
  user-select: none;
  transition: all 0.2s ease-in-out;
}

.override-cell:hover:not(.cell--free) {
  transform: scale(1.08);
  box-shadow: 0 0 8px rgba(255, 171, 0, 0.3);
  background-color: #334155;
  z-index: 5;
}

.cell--free {
  background: #ffab00 !important;
  color: #0b132b !important;
  cursor: not-allowed;
}

.cell--drawn {
  background: rgba(46, 125, 50, 0.15) !important;
  border: 2px solid #2e7d32 !important;
  color: #4caf50 !important;
}

.cell--overridden {
  background: rgba(147, 51, 234, 0.15) !important;
  border: 2px solid #a855f7 !important;
  color: #c084fc !important;
  box-shadow: 0 0 10px rgba(168, 85, 247, 0.25);
  animation: override-glow-pulse 2s infinite alternate;
}

@keyframes override-glow-pulse {
  0% { box-shadow: 0 0 6px rgba(168, 85, 247, 0.2); }
  100% { box-shadow: 0 0 12px rgba(168, 85, 247, 0.4); }
}

.cell-override-lock-icon {
  position: absolute;
  top: 2px;
  right: 2px;
  color: #c084fc;
}

.color-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  display: inline-block;
  margin-right: 4px;
}

.bg-green { background-color: #2e7d32; }
.bg-purple { background-color: #a855f7; }
.bg-amber { background-color: #f59e0b; }

.cell--highlight-match {
  background: rgba(245, 158, 11, 0.15) !important;
  border: 2px solid #f59e0b !important;
  color: #f59e0b !important;
  box-shadow: 0 0 10px rgba(245, 158, 11, 0.3);
}

.border-amber {
  border: 1px solid rgba(245, 158, 11, 0.3) !important;
}

.bg-amber-darken-4 {
  background-color: rgba(245, 158, 11, 0.15) !important;
}

.bg-emerald-darken-2 {
  background-color: #059669 !important;
}
</style>
