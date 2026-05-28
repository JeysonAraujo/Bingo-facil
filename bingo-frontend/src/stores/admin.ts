import { defineStore } from 'pinia'
import api, { type ApiResponse, type EventResponseDto, type CreateEventRequest } from '../services/api'

export const useAdminStore = defineStore('admin', {
  state: () => ({
    events: [] as EventResponseDto[],
    isLoading: false,
    error: null as string | null
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
    }
  }
})
