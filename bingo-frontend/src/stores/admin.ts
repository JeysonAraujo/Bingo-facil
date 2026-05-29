import { defineStore } from 'pinia'
import api, { type ApiResponse, type EventResponseDto, type CreateEventRequest } from '../services/api'
import { createWebSocketClient } from '../services/websocket'

export const useAdminStore = defineStore('admin', {
  state: () => ({
    events: [] as EventResponseDto[],
    isLoading: false,
    error: null as string | null,

    // ── WebSocket & Game Console state (Story 3.2) ──────────────────────────
    activeConsoleEvent: null as EventResponseDto | null,
    drawnConsoleNumbers: [] as number[],
    latencySeconds: 0,
    _wsAdminClient: null as any
  }),

  actions: {
    async fetchEvents() {
      this.isLoading = true
      this.error = null
      try {
        const response = await api.get<ApiResponse<EventResponseDto[]>>('/events')
        if (response.data.success) {
          this.events = response.data.data
        } else {
          this.error = response.data.error?.message || 'Falha ao buscar eventos'
        }
      } catch (err: any) {
        console.error(err)
        this.error = err.response?.data?.error?.message || err.message || 'Erro ao conectar ao servidor'
      } finally {
        this.isLoading = false
      }
    },

    async createEvent(payload: CreateEventRequest): Promise<boolean> {
      this.isLoading = true
      this.error = null
      try {
        const response = await api.post<ApiResponse<EventResponseDto>>('/events', payload)
        if (response.data.success) {
          // Add created event to the list
          this.events.unshift(response.data.data)
          return true
        } else {
          this.error = response.data.error?.message || 'Falha ao criar evento'
          return false
        }
      } catch (err: any) {
        console.error(err)
        this.error = err.response?.data?.error?.message || err.message || 'Erro ao salvar o evento'
        return false
      } finally {
        this.isLoading = false
      }
    },

    async generateTickets(eventId: string, quantity: number = 500): Promise<boolean> {
      this.isLoading = true
      this.error = null
      try {
        const response = await api.post(`/events/${eventId}/tickets/generate?quantity=${quantity}`)
        return response.status === 201 || response.status === 200
      } catch (err: any) {
        console.error(err)
        this.error = err.response?.data?.error?.message || err.message || 'Erro ao gerar cartelas'
        return false
      } finally {
        this.isLoading = false
      }
    },

    async downloadTicketsPdf(eventId: string): Promise<void> {
      this.isLoading = true
      this.error = null
      try {
        const response = await api.get(`/events/${eventId}/tickets/pdf`, {
          responseType: 'blob'
        })
        const url = window.URL.createObjectURL(new Blob([response.data]))
        const link = document.createElement('a')
        link.href = url
        link.setAttribute('download', `tickets-${eventId}.pdf`)
        document.body.appendChild(link)
        link.click()
        link.remove()
        window.URL.revokeObjectURL(url)
      } catch (err: any) {
        console.error(err)
        this.error = err.response?.data?.error?.message || err.message || 'Erro ao baixar o PDF'
      } finally {
        this.isLoading = false
      }
    },

    // ── Console Actions (Story 3.2) ──────────────────────────────────────────

    /**
     * Fetches details for the active console event.
     */
    async fetchConsoleEventDetails(eventId: string) {
      this.isLoading = true
      this.error = null
      try {
        const response = await api.get<ApiResponse<EventResponseDto>>(`/events/${eventId}`)
        if (response.data.success) {
          this.activeConsoleEvent = response.data.data
        } else {
          this.error = response.data.error?.message || 'Falha ao buscar detalhes do evento'
        }
      } catch (err: any) {
        console.error(err)
        this.error = err.response?.data?.error?.message || err.message || 'Erro ao carregar evento'
      } finally {
        this.isLoading = false
      }
    },

    /**
     * Fetches the chronological history of drawn balls for the console.
     */
    async fetchConsoleDrawnNumbers(eventId: string) {
      try {
        const response = await api.get<ApiResponse<any[]>>(`/events/${eventId}/draws`)
        if (response.data.success && response.data.data) {
          this.drawnConsoleNumbers = response.data.data.map(msg => msg.payload.ballNumber)
        }
      } catch (err) {
        console.error('[AdminStore] Failed to fetch initial drawn numbers:', err)
      }
    },

    /**
     * Establishes a STOMP WebSocket connection for the Admin Console.
     */
    connectAdminWebSocket(eventId: string) {
      if (this._wsAdminClient) {
        this._wsAdminClient.deactivate()
        this._wsAdminClient = null
      }

      this._wsAdminClient = createWebSocketClient(
        eventId,
        null, // No session token required for admin
        (ball) => {
          if (!this.drawnConsoleNumbers.includes(ball.ballNumber)) {
            this.drawnConsoleNumbers.push(ball.ballNumber)
          }
        },
        (ball) => {
          const idx = this.drawnConsoleNumbers.indexOf(ball.ballNumber)
          if (idx !== -1) {
            this.drawnConsoleNumbers.splice(idx, 1)
          }
        },
        () => {} // Status callback ignored for simplicity in admin panel
      )
    },

    /**
     * Cleans up the active Admin WebSocket client.
     */
    disconnectAdminWebSocket() {
      if (this._wsAdminClient) {
        this._wsAdminClient.deactivate()
        this._wsAdminClient = null
      }
    },

    /**
     * Publishes a new draw command to the backend WebSocket app prefix.
     */
    sendDrawBall(eventId: string, ballNumber: number, sequence: number) {
      if (this._wsAdminClient && this._wsAdminClient.connected) {
        this._wsAdminClient.publish({
          destination: `/app/events/${eventId}/draw`,
          body: JSON.stringify({
            ballNumber: ballNumber,
            drawnSequence: sequence
          })
        })
      } else {
        console.error('[AdminStore] STOMP Client not connected. Failed to draw ball.')
      }
    },

    /**
     * Publishes an undo retraction command to the backend WebSocket.
     */
    sendUndoLastDraw(eventId: string) {
      if (this._wsAdminClient && this._wsAdminClient.connected) {
        this._wsAdminClient.publish({
          destination: `/app/events/${eventId}/undo`
        })
      } else {
        console.error('[AdminStore] STOMP Client not connected. Failed to undo last draw.')
      }
    }
  }
})
