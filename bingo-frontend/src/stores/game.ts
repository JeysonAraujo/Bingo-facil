import { defineStore } from 'pinia'
import type { Client } from '@stomp/stompjs'
import api, {
  type ApiResponse,
  type TicketActivationResponseDto,
  type TicketMatrixDto,
  type TicketValidationResponseDto
} from '../services/api'
import {
  createWebSocketClient,
  type WsConnectionStatus,
  type WsBallDrawnPayload,
  type WsAnnouncementPayload,
  type WsTicketOverridePayload
} from '../services/websocket'

const SESSION_TOKEN_KEY = 'bingo_session_token'
const TICKET_MATRIX_KEY = 'bingo_ticket_matrix'
const TICKET_SERIAL_KEY = 'bingo_ticket_serial'
const EVENT_ID_KEY = 'bingo_event_id'
const TICKET_ID_KEY = 'bingo_ticket_id'

export const useGameStore = defineStore('game', {
  state: () => ({
    // ── Ticket session state (existing, do not modify) ──────────────────────
    sessionToken: sessionStorage.getItem(SESSION_TOKEN_KEY) as string | null,
    ticketMatrix: (() => {
      try {
        const raw = sessionStorage.getItem(TICKET_MATRIX_KEY)
        return raw ? (JSON.parse(raw) as TicketMatrixDto) : null
      } catch {
        return null
      }
    })() as TicketMatrixDto | null,
    serial: sessionStorage.getItem(TICKET_SERIAL_KEY) as string | null,
    eventId: sessionStorage.getItem(EVENT_ID_KEY) as string | null,
    ticketId: sessionStorage.getItem(TICKET_ID_KEY) as string | null,
    isLoading: false,
    error: null as string | null,
    eventDetails: null as any | null,
    isSubmittingClaim: false,

    // ── WebSocket real-time game state (Story 3.1) ──────────────────────────
    /** Current WebSocket connection status for reactive UI indicators */
    connectionStatus: 'disconnected' as WsConnectionStatus,
    /** Ordered list of ball numbers drawn during the current round */
    drawnNumbers: [] as number[],
    /** Admin-configured stream latency in seconds (0–30); used by latency engine in Story 4.1 */
    latencySeconds: 0,
    /** Internal STOMP client reference — not exposed in template bindings */
    _wsClient: null as Client | null,
    /** Tracks active timeout IDs for drawn balls to prevent early marking or ghost markings on retract */
    _latencyTimeouts: {} as Record<number, number>,

    // ── Overrides & Announcements state (Story 3.3) ──────────────────────────
    /** Forced overrides marked by coordinate admins */
    overriddenNumbers: [] as number[],
    /** Currently active global text announcement */
    activeAnnouncement: null as string | null,

    // ── Victory Celebration state (Story 5.2) ───────────────────────────────
    winnerSerial: null as string | null,
    winnerCardIndex: null as number | null,
    showVictoryOverlay: false,
    activeViewers: 0,
    isBingoFinished: false,
    winnersList: [] as Array<{ serial: string; name: string }>,
    playerName: sessionStorage.getItem('bingo_player_name') as string | null,
    playerPhone: sessionStorage.getItem('bingo_player_phone') as string | null,
    showRoulette: false,
    rouletteParticipants: [] as Array<{ serial: string; name: string }>,
    rouletteWinnerSerial: null as string | null,
    rouletteWinnerName: null as string | null,
    hasClaimedThisRound: false,
    claimsList: [] as any[]
  }),

  getters: {
    isAuthenticated: (state) => !!state.sessionToken,
    /** True when the WebSocket channel is fully connected */
    isConnected: (state) => state.connectionStatus === 'connected'
  },

  actions: {
    async validateTicket(serialCode: string): Promise<TicketValidationResponseDto | null> {
      this.isLoading = true
      this.error = null
      try {
        const response = await api.get<ApiResponse<TicketValidationResponseDto>>(`/tickets/validate?serial=${serialCode}`)
        if (response.data.success) {
          const data = response.data.data
          // Do NOT save session stuff here, just metadata for the view
          this.serial = data.serial
          this.eventId = data.eventId
          return data
        } else {
          this.error = response.data.error?.message || 'Cartela inválida.'
          return null
        }
      } catch (err: any) {
        this.error = err.response?.data?.error?.message || 'Falha ao validar cartela.'
        return null
      } finally {
        this.isLoading = false
      }
    },

    async activateTicket(serialCode: string, playerName: string, playerPhone: string): Promise<boolean> {
      this.isLoading = true
      this.error = null
      try {
        const response = await api.post<ApiResponse<TicketActivationResponseDto>>(
          '/tickets/activate',
          {
            serial: serialCode.toUpperCase(),
            playerName,
            playerPhone
          }
        )
        if (response.data.success) {
          const data = response.data.data
          this.sessionToken = data.sessionToken
          this.ticketMatrix = data.matrix
          this.serial = data.serial
          this.eventId = data.eventId
          this.ticketId = data.ticketId
          this.playerName = playerName
          this.playerPhone = playerPhone

          // Persist to sessionStorage for session recovery on page refresh
          sessionStorage.setItem(SESSION_TOKEN_KEY, data.sessionToken)
          sessionStorage.setItem(TICKET_MATRIX_KEY, JSON.stringify(data.matrix))
          sessionStorage.setItem(TICKET_SERIAL_KEY, data.serial)
          sessionStorage.setItem(EVENT_ID_KEY, data.eventId)
          sessionStorage.setItem(TICKET_ID_KEY, data.ticketId)
          sessionStorage.setItem('bingo_player_name', playerName)
          sessionStorage.setItem('bingo_player_phone', playerPhone)
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
      this.eventId = null
      this.ticketId = null
      this.playerName = null
      this.playerPhone = null
      sessionStorage.removeItem(SESSION_TOKEN_KEY)
      sessionStorage.removeItem(TICKET_MATRIX_KEY)
      sessionStorage.removeItem(TICKET_SERIAL_KEY)
      sessionStorage.removeItem(EVENT_ID_KEY)
      sessionStorage.removeItem(TICKET_ID_KEY)
      sessionStorage.removeItem('bingo_player_name')
      sessionStorage.removeItem('bingo_player_phone')
      this.disconnectFromGame()
    },

    // ── WebSocket actions (Story 3.1) ────────────────────────────────────

    /**
     * Establishes a STOMP WebSocket connection for the given event.
     * Cleans up any existing connection before creating a new one.
     *
     * @param eventId UUID of the active bingo event
     */
    connectToGame(eventId: string, onChatMessage?: (msg: any) => void, onRoundStatusChange?: (msg: any) => void) {
      if (!this.sessionToken || !this.ticketId) return
      
      this.disconnectFromGame()
      
      this.winnerSerial = null
      this.winnerCardIndex = null
      this.showVictoryOverlay = false
      this.winnersList = []
      this.showRoulette = false
      this.rouletteParticipants = []
      this.rouletteWinnerSerial = null
      this.rouletteWinnerName = null
 
      this._wsClient = createWebSocketClient(
        eventId,
        this.sessionToken,
        (ball: WsBallDrawnPayload) => this.addDrawnNumber(ball.ballNumber, ball.timestamp),
        (ball: WsBallDrawnPayload) => this.retractDrawnNumber(ball.ballNumber),
        (status: WsConnectionStatus) => this.setConnectionStatus(status),
        this.ticketId,
        (announcement: WsAnnouncementPayload) => {
          this.activeAnnouncement = announcement.text
        },
        (override: WsTicketOverridePayload) => {
          this.applyTicketOverride(override.ballNumber, override.action)
        },
        (delay: number) => {
          this.setLatencySeconds(delay)
        },
        (claimMsg) => {
          if (claimMsg.eventType === 'BINGO_APPROVED') {
            const serial = claimMsg.payload.serial
            const name = claimMsg.payload.playerName || 'Jogador'
            if (!this.winnersList) {
              this.winnersList = []
            }
            if (!this.winnersList.some((w: any) => w.serial === serial)) {
              this.winnersList.push({ serial, name })
            }
            this.winnerSerial = this.winnersList.map((w: any) => `${w.name} (${w.serial})`).join(', ')
            this.winnerCardIndex = claimMsg.payload.cardIndex
            this.showVictoryOverlay = true
          } else if (claimMsg.eventType === 'BINGO_CLAIM') {
            if (!this.claimsList) {
              this.claimsList = []
            }
            if (!this.claimsList.some((c: any) => c.claimId === claimMsg.payload.claimId)) {
              this.claimsList.push(claimMsg.payload)
            }
          } else if (claimMsg.eventType === 'TIE_ROULETTE') {
            this.rouletteParticipants = claimMsg.payload.participants
            this.rouletteWinnerSerial = claimMsg.payload.winnerSerial
            this.rouletteWinnerName = claimMsg.payload.winnerName
            this.showRoulette = true
          }
        },
        onChatMessage,
        (roundMsg) => {
          if (this.eventDetails && this.eventDetails.rounds) {
            const r = this.eventDetails.rounds.find((r: any) => r.id === roundMsg.roundId)
            if (r) {
              r.status = roundMsg.status
            }
          }
          if (roundMsg.status === 'IN_PROGRESS') {
            this.drawnNumbers = []
            this.winnersList = []
            this.showRoulette = false
            this.rouletteParticipants = []
            this.rouletteWinnerSerial = null
            this.rouletteWinnerName = null
            this.hasClaimedThisRound = false
            this.claimsList = []
          }
          if (onRoundStatusChange) {
            onRoundStatusChange(roundMsg)
          }
        },
        (viewers: number) => {
          this.activeViewers = viewers
        },
        undefined, // onNaBoa (not used by player)
        (status: string) => {
          if (status === 'FINALIZADO') {
            this.isBingoFinished = true
            this.disconnectFromGame()
          }
        }
      )
    },

    async fetchEventDetails(eventId: string): Promise<void> {
      try {
        const response = await api.get<ApiResponse<any>>(`/events/${eventId}`)
        if (response.data.success && response.data.data) {
          this.eventDetails = response.data.data
          this.setLatencySeconds(response.data.data.streamDelaySeconds || 0)
        }
      } catch (err) {
        console.error('[Store] Failed to fetch event details:', err)
      }
    },

    async claimBingo(cardIndex: number): Promise<boolean> {
      if (this.isSubmittingClaim || !this.ticketId || !this.sessionToken) return false
      this.isSubmittingClaim = true
      try {
        const response = await api.post<ApiResponse<any>>('/claims', {
          ticketId: this.ticketId,
          sessionToken: this.sessionToken,
          cardIndex: cardIndex
        })
        if (response.data.success) {
          this.hasClaimedThisRound = true
          return true
        }
        return false
      } catch (err: any) {
        if (err.response && err.response.status === 409) {
          console.log('[Store] Claim BINGO already registered (409 Conflict). Treating as success.')
          this.hasClaimedThisRound = true
          return true
        }
        console.error('[Store] Claim BINGO failed:', err)
        return false
      } finally {
        this.isSubmittingClaim = false
      }
    },

    async fetchInitialDraws(eventId: string): Promise<void> {
      try {
        const response = await api.get<ApiResponse<any[]>>(`/events/${eventId}/draws`)
        if (response.data.success && response.data.data) {
          for (const key in this._latencyTimeouts) {
            clearTimeout(this._latencyTimeouts[key])
          }
          this._latencyTimeouts = {}
          this.drawnNumbers = []

          response.data.data.forEach((msg: any) => {
            this.addDrawnNumber(msg.payload.ballNumber, msg.timestamp)
          })
        }
      } catch (err) {
        console.error('[Store] Failed to fetch initial drawn numbers:', err)
      }
    },

    sendChatMessage(text: string): void {
      if (this._wsClient && this._wsClient.connected && this.eventId) {
        const sender = this.serial ? `Cartela ${this.serial}` : 'Jogador'
        this._wsClient.publish({
          destination: `/app/events/${this.eventId}/chat`,
          body: JSON.stringify({ sender, text })
        })
      }
    },

    dismissVictory() {
      this.showVictoryOverlay = false
      this.winnerSerial = null
      this.winnersList = []
      this.winnerCardIndex = null
    },

    async fetchTicketOverrides(serial: string): Promise<void> {
      try {
        const response = await api.get<ApiResponse<any>>(`/tickets/search?serial=${serial.toUpperCase()}`)
        if (response.data.success && response.data.data) {
          this.overriddenNumbers = response.data.data.markedOverrides || []
        }
      } catch (err) {
        console.error('[Store] Failed to fetch ticket overrides:', err)
      }
    },

    /**
     * Appends or removes a forced ticket override.
     */
    applyTicketOverride(ballNumber: number, action: 'MARK' | 'UNMARK'): void {
      if (action === 'MARK') {
        if (!this.overriddenNumbers.includes(ballNumber)) {
          this.overriddenNumbers.push(ballNumber)
        }
      } else {
        const index = this.overriddenNumbers.indexOf(ballNumber)
        if (index !== -1) {
          this.overriddenNumbers.splice(index, 1)
        }
      }
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
      for (const key in this._latencyTimeouts) {
        clearTimeout(this._latencyTimeouts[key])
      }
      this._latencyTimeouts = {}
      this.setConnectionStatus('disconnected')
      this.hasClaimedThisRound = false
      this.claimsList = []
    },

    /**
     * Appends a drawn ball number to the ordered list.
     * Called by the WebSocket callback on BALL_DRAWN events.
     * Story 4.1 will intercept this to apply latency queuing before visual marking.
     *
     * @param ball Ball number (1–75)
     * @param timestamp Optional ISO timestamp of when the ball was drawn
     */
    addDrawnNumber(ball: number, timestamp?: string): void {
      if (typeof ball !== 'number' || isNaN(ball) || ball < 1 || ball > 75) {
        console.warn(`[Store] Ignored invalid ball draw number received: ${ball}`)
        return
      }
      if (this.drawnNumbers.includes(ball)) {
        return
      }

      // If we already have a pending timeout for this ball, clear it
      if (this._latencyTimeouts[ball]) {
        clearTimeout(this._latencyTimeouts[ball])
        delete this._latencyTimeouts[ball]
      }

      // Calculate latency delay
      let timeToWait = 0
      if (timestamp && this.latencySeconds > 0) {
        const drawTime = new Date(timestamp).getTime()
        const now = Date.now()
        const elapsed = now - drawTime
        timeToWait = (this.latencySeconds * 1000) - elapsed
      } else if (this.latencySeconds > 0) {
        timeToWait = this.latencySeconds * 1000
      }

      if (timeToWait > 0) {
        const timeoutId = window.setTimeout(() => {
          if (!this.drawnNumbers.includes(ball)) {
            this.drawnNumbers.push(ball)
          }
          delete this._latencyTimeouts[ball]
        }, timeToWait)
        this._latencyTimeouts[ball] = timeoutId
      } else {
        this.drawnNumbers.push(ball)
      }
    },

    /**
     * Removes a drawn ball number from the local state list.
     * Called by the WebSocket callback on BALL_RETRACTED events.
     *
     * @param ball Ball number (1–75)
     */
    retractDrawnNumber(ball: number): void {
      // Clear any pending timeout for this ball first
      if (this._latencyTimeouts[ball]) {
        clearTimeout(this._latencyTimeouts[ball])
        delete this._latencyTimeouts[ball]
      }
      const index = this.drawnNumbers.indexOf(ball)
      if (index !== -1) {
        this.drawnNumbers.splice(index, 1)
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

