import { defineStore } from 'pinia'
import type { Client } from '@stomp/stompjs'
import api, {
  type ApiResponse,
  type TicketActivationResponseDto,
  type TicketMatrixDto
} from '../services/api'
import { createWebSocketClient, type WsConnectionStatus, type WsBallDrawnPayload } from '../services/websocket'

const SESSION_TOKEN_KEY = 'bingo_session_token'
const TICKET_MATRIX_KEY = 'bingo_ticket_matrix'
const TICKET_SERIAL_KEY = 'bingo_ticket_serial'

export const useGameStore = defineStore('game', {
  state: () => ({
    // ── Ticket session state (existing, do not modify) ──────────────────────
    sessionToken: localStorage.getItem(SESSION_TOKEN_KEY) as string | null,
    ticketMatrix: (() => {
      try {
        const raw = localStorage.getItem(TICKET_MATRIX_KEY)
        return raw ? (JSON.parse(raw) as TicketMatrixDto) : null
      } catch {
        return null
      }
    })() as TicketMatrixDto | null,
    serial: localStorage.getItem(TICKET_SERIAL_KEY) as string | null,
    isLoading: false,
    error: null as string | null,

    // ── WebSocket real-time game state (Story 3.1) ──────────────────────────
    /** Current WebSocket connection status for reactive UI indicators */
    connectionStatus: 'disconnected' as WsConnectionStatus,
    /** Ordered list of ball numbers drawn during the current round */
    drawnNumbers: [] as number[],
    /** Admin-configured stream latency in seconds (0–30); used by latency engine in Story 4.1 */
    latencySeconds: 0,
    /** Internal STOMP client reference — not exposed in template bindings */
    _wsClient: null as Client | null
  }),

  getters: {
    isAuthenticated: (state) => !!state.sessionToken,
    /** True when the WebSocket channel is fully connected */
    isConnected: (state) => state.connectionStatus === 'connected'
  },

  actions: {
    // ── Existing ticket activation actions (unchanged) ────────────────────

    async activateTicket(serial: string): Promise<boolean> {
      this.isLoading = true
      this.error = null
      try {
        const response = await api.post<ApiResponse<TicketActivationResponseDto>>(
          '/tickets/activate',
          { serial: serial.toUpperCase() }
        )
        if (response.data.success) {
          const data = response.data.data
          this.sessionToken = data.sessionToken
          this.ticketMatrix = data.matrix
          this.serial = data.serial

          // Persist to localStorage for session recovery on page refresh
          localStorage.setItem(SESSION_TOKEN_KEY, data.sessionToken)
          localStorage.setItem(TICKET_MATRIX_KEY, JSON.stringify(data.matrix))
          localStorage.setItem(TICKET_SERIAL_KEY, data.serial)
          return true
        } else {
          this.error = response.data.error?.message || 'Falha ao ativar cartela.'
          return false
        }
      } catch (err: any) {
        this.error =
          err.response?.data?.error?.message ||
          err.message ||
          'Erro ao conectar ao servidor.'
        return false
      } finally {
        this.isLoading = false
      }
    },

    clearSession() {
      this.sessionToken = null
      this.ticketMatrix = null
      this.serial = null
      localStorage.removeItem(SESSION_TOKEN_KEY)
      localStorage.removeItem(TICKET_MATRIX_KEY)
      localStorage.removeItem(TICKET_SERIAL_KEY)
    },

    // ── WebSocket actions (Story 3.1) ────────────────────────────────────

    /**
     * Establishes a STOMP WebSocket connection for the given event.
     * Cleans up any existing connection before creating a new one.
     *
     * @param eventId UUID of the active bingo event
     */
    connectToGame(eventId: string): void {
      // Prevent duplicate connections
      if (this._wsClient) {
        this._wsClient.deactivate()
        this._wsClient = null
      }

      this._wsClient = createWebSocketClient(
        eventId,
        this.sessionToken,
        (ball: WsBallDrawnPayload) => this.addDrawnNumber(ball.ballNumber),
        (status: WsConnectionStatus) => this.setConnectionStatus(status)
      )
    },

    /**
     * Gracefully disconnects the active WebSocket connection.
     * Safe to call even when not connected.
     */
    disconnectFromGame(): void {
      if (this._wsClient) {
        this._wsClient.deactivate()
        this._wsClient = null
      }
      this.setConnectionStatus('disconnected')
    },

    /**
     * Appends a drawn ball number to the ordered list.
     * Called by the WebSocket callback on BALL_DRAWN events.
     * Story 4.1 will intercept this to apply latency queuing before visual marking.
     *
     * @param ball Ball number (1–75)
     */
    addDrawnNumber(ball: number): void {
      if (!this.drawnNumbers.includes(ball)) {
        this.drawnNumbers.push(ball)
      }
    },

    /**
     * Updates the connection status reactively.
     * Drives the ConnectionStatusBadge UI component.
     *
     * @param status New connection status
     */
    setConnectionStatus(status: WsConnectionStatus): void {
      this.connectionStatus = status
    },

    /**
     * Updates the Admin-configured stream latency.
     * Used by the latency alignment engine (Story 4.1).
     *
     * @param seconds Delay in seconds (0–30)
     */
    setLatencySeconds(seconds: number): void {
      this.latencySeconds = Math.max(0, Math.min(30, seconds))
    }
  }
})

