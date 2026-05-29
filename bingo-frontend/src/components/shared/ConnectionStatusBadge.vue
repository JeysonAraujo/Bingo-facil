<template>
  <!-- Accessible connection status indicator with pulsing dot -->
  <div
    class="connection-badge"
    :class="`status--${connectionStatus}`"
    role="status"
    aria-atomic="true"
    aria-live="polite"
    :aria-label="`Status da conexão: ${statusLabel}`"
    :title="statusLabel"
  >
    <span class="badge-dot" aria-hidden="true" />
    <span class="badge-label">{{ statusLabel }}</span>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { storeToRefs } from 'pinia'
import { useGameStore } from '@/stores/game'

const gameStore = useGameStore()
const { connectionStatus } = storeToRefs(gameStore)

const statusLabel = computed(() => {
  switch (connectionStatus.value) {
    case 'connected':    return 'Conectado'
    case 'connecting':   return 'Conectando…'
    case 'error':        return 'Erro de conexão'
    case 'disconnected':
    default:             return 'Desconectado'
  }
})
</script>

<style scoped>
@layer tailwind-utilities {
  .connection-badge {
    display: inline-flex;
    align-items: center;
    gap: 6px;
    padding: 4px 10px;
    border-radius: 9999px;
    font-size: 0.75rem;
    font-weight: 600;
    letter-spacing: 0.03em;
    user-select: none;
    transition: background-color 0.3s ease, color 0.3s ease;
  }

  /* ── Status colour tokens ────────────────────────────────────────────── */
  .status--connected {
    background-color: rgba(46, 125, 50, 0.15);
    color: #2E7D32;
  }

  .status--connecting {
    background-color: rgba(245, 124, 0, 0.15);
    color: #E65100;
  }

  .status--error {
    background-color: rgba(183, 28, 28, 0.15);
    color: #B71C1C;
  }

  .status--disconnected {
    background-color: rgba(66, 66, 66, 0.12);
    color: #424242;
  }

  /* ── Pulsing dot indicator ─────────────────────────────────────────── */
  .badge-dot {
    display: inline-block;
    width: 8px;
    height: 8px;
    border-radius: 50%;
    flex-shrink: 0;
  }

  .status--connected    .badge-dot { background-color: #2E7D32; animation: pulse-dot 2s ease-in-out infinite; }
  .status--connecting   .badge-dot { background-color: #E65100; animation: pulse-dot 1s ease-in-out infinite; }
  .status--error        .badge-dot { background-color: #B71C1C; }
  .status--disconnected .badge-dot { background-color: #9E9E9E; }

  @keyframes pulse-dot {
    0%, 100% { opacity: 1; transform: scale(1); }
    50%       { opacity: 0.4; transform: scale(0.7); }
  }

  .badge-label {
    line-height: 1;
  }
}
</style>
