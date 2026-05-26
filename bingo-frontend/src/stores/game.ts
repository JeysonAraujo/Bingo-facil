import { defineStore } from 'pinia'
import api, {
  type ApiResponse,
  type TicketActivationResponseDto,
  type TicketMatrixDto
} from '../services/api'

const SESSION_TOKEN_KEY = 'bingo_session_token'
const TICKET_MATRIX_KEY = 'bingo_ticket_matrix'
const TICKET_SERIAL_KEY = 'bingo_ticket_serial'

export const useGameStore = defineStore('game', {
  state: () => ({
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
    error: null as string | null
  }),

  getters: {
    isAuthenticated: (state) => !!state.sessionToken
  },

  actions: {
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
    }
  }
})
