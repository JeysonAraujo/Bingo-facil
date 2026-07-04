import { defineStore } from 'pinia'
import api, { type ApiResponse, type EventResponseDto, type CreateEventRequest } from '../services/api'
import { createWebSocketClient } from '../services/websocket'
import { useAuthStore } from './auth'

export const useAdminStore = defineStore('admin', {
  state: () => ({
    events: [] as EventResponseDto[],
    isLoading: false,
    error: null as string | null,

    // ── WebSocket & Game Console state (Story 3.2 & 3.3) ──────────────────────
    activeConsoleEvent: null as EventResponseDto | null,
    drawnConsoleNumbers: [] as number[],
    latencySeconds: 0,
    _wsAdminClient: null as any,
    searchedTicket: null as any, // TicketResponseDto for overrides
    claims: [] as any[],
    ticketsNaBoaCount: 0
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

    async downloadTicketsPdf(
      eventId: string,
      prizes?: string,
      customFooter?: string,
      hasCutStrip: boolean = false
    ): Promise<void> {
      this.isLoading = true
      this.error = null
      try {
        const params = new URLSearchParams()
        if (prizes) params.append('prizes', prizes)
        if (customFooter) params.append('customFooter', customFooter)
        if (hasCutStrip) params.append('hasCutStrip', 'true')
        const queryString = params.toString() ? `?${params.toString()}` : ''

        const response = await api.get(`/events/${eventId}/tickets/pdf${queryString}`, {
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

    async updateEvent(eventId: string, payload: CreateEventRequest): Promise<boolean> {
      this.isLoading = true
      this.error = null
      try {
        const response = await api.put<ApiResponse<EventResponseDto>>(`/events/${eventId}`, payload)
        if (response.data.success) {
          const index = this.events.findIndex(e => e.id === eventId)
          if (index !== -1) {
            this.events[index] = response.data.data
          }
          if (this.activeConsoleEvent && this.activeConsoleEvent.id === eventId) {
            this.activeConsoleEvent = response.data.data
          }
          return true
        } else {
          this.error = response.data.error?.message || 'Falha ao atualizar o evento'
          return false
        }
      } catch (err: any) {
        console.error(err)
        this.error = err.response?.data?.error?.message || err.message || 'Erro ao atualizar o evento'
        return false
      } finally {
        this.isLoading = false
      }
    },

    async deleteEvent(eventId: string): Promise<boolean> {
      this.isLoading = true
      this.error = null
      try {
        const response = await api.delete<ApiResponse<void>>(`/events/${eventId}`)
        if (response.data.success) {
          this.events = this.events.filter(e => e.id !== eventId)
          return true
        } else {
          this.error = response.data.error?.message || 'Falha ao excluir o evento'
          return false
        }
      } catch (err: any) {
        console.error(err)
        this.error = err.response?.data?.error?.message || err.message || 'Erro ao excluir o evento'
        return false
      } finally {
        this.isLoading = false
      }
    },

    async updateEventStatus(eventId: string, status: string): Promise<boolean> {
      this.isLoading = true
      this.error = null
      try {
        const response = await api.patch<ApiResponse<EventResponseDto>>(`/events/${eventId}/status?status=${status}`)
        if (response.data.success) {
          const index = this.events.findIndex(e => e.id === eventId)
          if (index !== -1) {
            this.events[index] = response.data.data
          }
          if (this.activeConsoleEvent && this.activeConsoleEvent.id === eventId) {
            this.activeConsoleEvent = response.data.data
          }
          return true
        } else {
          this.error = response.data.error?.message || 'Falha ao atualizar status do evento'
          return false
        }
      } catch (err: any) {
        console.error(err)
        this.error = err.response?.data?.error?.message || err.message || 'Erro ao atualizar status'
        return false
      } finally {
        this.isLoading = false
      }
    },

    async uploadHeaderImage(eventId: string, file: File): Promise<boolean> {
      this.isLoading = true
      this.error = null
      try {
        const formData = new FormData()
        formData.append('file', file)
        const response = await api.post<ApiResponse<EventResponseDto>>(`/events/${eventId}/header-image`, formData, {
          headers: {
            'Content-Type': 'multipart/form-data'
          }
        })
        if (response.data.success) {
          const index = this.events.findIndex(e => e.id === eventId)
          if (index !== -1) {
            this.events[index] = response.data.data
          }
          if (this.activeConsoleEvent && this.activeConsoleEvent.id === eventId) {
            this.activeConsoleEvent = response.data.data
          }
          return true
        } else {
          this.error = response.data.error?.message || 'Falha ao enviar cabeçalho'
          return false
        }
      } catch (err: any) {
        console.error(err)
        this.error = err.response?.data?.error?.message || err.message || 'Erro ao enviar arquivo'
        return false
      } finally {
        this.isLoading = false
      }
    },

    // ── Console Actions (Story 3.2 & 3.3) ────────────────────────────────────

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
          if (this.activeConsoleEvent) {
            this.latencySeconds = this.activeConsoleEvent.streamDelaySeconds || 0
          }
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
     * Searches a player ticket matrix by serial code.
     */
    async searchTicketBySerial(serial: string): Promise<boolean> {
      this.isLoading = true
      this.error = null
      try {
        const response = await api.get<ApiResponse<any>>(`/tickets/search?serial=${serial.toUpperCase()}`)
        if (response.data.success) {
          this.searchedTicket = response.data.data
          return true
        } else {
          this.error = response.data.error?.message || 'Cartela não encontrada.'
          this.searchedTicket = null
          return false
        }
      } catch (err: any) {
        console.error(err)
        this.error = err.response?.data?.error?.message || err.message || 'Erro ao conectar para busca.'
        this.searchedTicket = null
        return false
      } finally {
        this.isLoading = false
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

      const authStore = useAuthStore()
      this._wsAdminClient = createWebSocketClient(
        eventId,
        authStore.token, // Pass admin token for backend STOMP authentication
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
        () => {}, // Status callback ignored for simplicity in admin panel
        null, // ticketId
        undefined, // onAnnouncement
        undefined, // onTicketOverride
        undefined, // onDelayChange
        (claimMsg) => {
          if (claimMsg.eventType === 'BINGO_CLAIM') {
            const exists = this.claims.some((c) => c.claimId === claimMsg.payload.claimId)
            if (!exists) {
              this.claims.push({
                claimId: claimMsg.payload.claimId,
                ticketId: claimMsg.payload.ticketId,
                cardIndex: claimMsg.payload.cardIndex,
                status: claimMsg.payload.status,
                serial: claimMsg.payload.serial,
                createdAt: claimMsg.timestamp,
                automatic: claimMsg.payload.automatic
              })
            }
          } else if (claimMsg.eventType === 'BINGO_APPROVED' || claimMsg.eventType === 'BINGO_REJECTED') {
            const idx = this.claims.findIndex((c) => c.claimId === claimMsg.payload.claimId)
            if (idx !== -1) {
              this.claims[idx].status = claimMsg.payload.status
            }
          }
        },
        undefined, // onChatMessage
        (roundMsg) => {
          if (this.activeConsoleEvent && this.activeConsoleEvent.rounds) {
            const r = this.activeConsoleEvent.rounds.find((r: any) => r.id === roundMsg.roundId)
            if (r) {
              r.status = roundMsg.status
            }
          }
          if (roundMsg.status === 'IN_PROGRESS') {
            this.drawnConsoleNumbers = []
            this.ticketsNaBoaCount = 0
          }
        },
        undefined, // onViewersChange
        (count) => {
          this.ticketsNaBoaCount = count
        }
      )
    },

    async fetchClaims(roundId: string) {
      try {
        const response = await api.get<ApiResponse<any[]>>(`/claims/round/${roundId}`)
        if (response.data.success && response.data.data) {
          this.claims = response.data.data
        }
      } catch (err) {
        console.error('[AdminStore] Failed to fetch initial claims:', err)
      }
    },

    async startRound(roundId: string): Promise<boolean> {
      try {
        const response = await api.post(`/rounds/${roundId}/start`)
        return response.data.success
      } catch (err) {
        console.error('[AdminStore] Failed to start round:', err)
        return false
      }
    },

    async finishRound(roundId: string): Promise<boolean> {
      try {
        const response = await api.post(`/rounds/${roundId}/finish`)
        return response.data.success
      } catch (err) {
        console.error('[AdminStore] Failed to finish round:', err)
        return false
      }
    },

    async cancelRound(roundId: string): Promise<boolean> {
      try {
        const response = await api.post(`/rounds/${roundId}/cancel`)
        return response.data.success
      } catch (err) {
        console.error('[AdminStore] Failed to cancel round:', err)
        return false
      }
    },

    async deleteRound(roundId: string): Promise<boolean> {
      try {
        const response = await api.delete(`/rounds/${roundId}`)
        return response.data.success
      } catch (err) {
        console.error('[AdminStore] Failed to delete round:', err)
        return false
      }
    },

    async createRound(eventId: string, payload: any): Promise<boolean> {
      this.isLoading = true
      this.error = null
      try {
        const response = await api.post<ApiResponse<EventResponseDto>>(`/events/${eventId}/rounds`, payload)
        if (response.data.success) {
          const index = this.events.findIndex(e => e.id === eventId)
          if (index !== -1) {
            this.events[index] = response.data.data
          }
          if (this.activeConsoleEvent && this.activeConsoleEvent.id === eventId) {
            this.activeConsoleEvent = response.data.data
          }
          return true
        } else {
          this.error = response.data.error?.message || 'Falha ao criar rodada'
          return false
        }
      } catch (err: any) {
        console.error(err)
        this.error = err.response?.data?.error?.message || err.message || 'Erro ao criar rodada'
        return false
      } finally {
        this.isLoading = false
      }
    },

    async updateRound(eventId: string, roundId: string, payload: any): Promise<boolean> {
      this.isLoading = true
      this.error = null
      try {
        const response = await api.put<ApiResponse<EventResponseDto>>(`/events/${eventId}/rounds/${roundId}`, payload)
        if (response.data.success) {
          const index = this.events.findIndex(e => e.id === eventId)
          if (index !== -1) {
            this.events[index] = response.data.data
          }
          if (this.activeConsoleEvent && this.activeConsoleEvent.id === eventId) {
            this.activeConsoleEvent = response.data.data
          }
          return true
        } else {
          this.error = response.data.error?.message || 'Falha ao atualizar rodada'
          return false
        }
      } catch (err: any) {
        console.error(err)
        this.error = err.response?.data?.error?.message || err.message || 'Erro ao atualizar rodada'
        return false
      } finally {
        this.isLoading = false
      }
    },

    async approveClaim(claimId: string): Promise<boolean> {
      try {
        const response = await api.post<ApiResponse<any>>(`/claims/${claimId}/approve`)
        if (response.data.success) {
          const idx = this.claims.findIndex((c) => c.claimId === claimId)
          if (idx !== -1) {
            this.claims[idx].status = 'APPROVED'
          }
          return true
        }
        return false
      } catch (err) {
        console.error('[AdminStore] Failed to approve claim:', err)
        return false
      }
    },

    async rejectClaim(claimId: string): Promise<boolean> {
      try {
        const response = await api.post<ApiResponse<any>>(`/claims/${claimId}/reject`)
        if (response.data.success) {
          const idx = this.claims.findIndex((c) => c.claimId === claimId)
          if (idx !== -1) {
            this.claims[idx].status = 'REJECTED'
          }
          return true
        }
        return false
      } catch (err) {
        console.error('[AdminStore] Failed to reject claim:', err)
        return false
      }
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
    },

    /**
     * Publishes an administrative global text announcement to players.
     */
    sendAnnouncement(eventId: string, text: string) {
      if (this._wsAdminClient && this._wsAdminClient.connected) {
        this._wsAdminClient.publish({
          destination: `/app/events/${eventId}/announcement`,
          body: JSON.stringify({ text: text })
        })
      } else {
        console.error('[AdminStore] STOMP Client not connected. Failed to send announcement.')
      }
    },

    /**
     * Publishes a forced cell marking override command for a target ticket.
     */
    sendTicketOverride(ticketId: string, ballNumber: number, action: 'MARK' | 'UNMARK') {
      if (this._wsAdminClient && this._wsAdminClient.connected) {
        this._wsAdminClient.publish({
          destination: `/app/tickets/${ticketId}/override`,
          body: JSON.stringify({
            ballNumber: ballNumber,
            action: action
          })
        })

        // Optimistically update local searchedTicket state overrides list
        if (this.searchedTicket && this.searchedTicket.id === ticketId) {
          const overrides = [...(this.searchedTicket.markedOverrides || [])]
          if (action === 'MARK') {
            if (!overrides.includes(ballNumber)) {
              overrides.push(ballNumber)
            }
          } else {
            const idx = overrides.indexOf(ballNumber)
            if (idx !== -1) {
              overrides.splice(idx, 1)
            }
          }
          this.searchedTicket.markedOverrides = overrides
        }
      } else {
        console.error('[AdminStore] STOMP Client not connected. Failed to send ticket override.')
      }
    },

    /**
     * Updates the stream delay for the event on the server and broadcasts the update.
     */
    async updateStreamDelay(eventId: string, delay: number) {
      try {
        const response = await api.put(`/events/${eventId}/delay?delay=${delay}`)
        if (response.status === 200) {
          this.latencySeconds = delay
          if (this.activeConsoleEvent) {
            this.activeConsoleEvent.streamDelaySeconds = delay
          }
        }
      } catch (err: any) {
        console.error('[AdminStore] Failed to update stream delay:', err)
        this.error = err.response?.data?.error?.message || err.message || 'Falha ao atualizar o delay da transmissão'
      }
    }
  }
})
