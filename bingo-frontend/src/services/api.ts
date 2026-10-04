import axios from 'axios'

// Standard API Error Envelope
export interface ApiError {
  code: string
  message: string
  details: string[] | null
}

// Standard API Response Envelope
export interface ApiResponse<T> {
  success: boolean
  data: T
  error: ApiError | null
}

// Event-related interfaces
export interface PrizeResponseDto {
  id: string
  name: string
  type: string
  drawPattern: string
}

export interface RoundResponseDto {
  id: string
  roundNumber: number
  status: string
  prizes: PrizeResponseDto[]
}

export interface EventResponseDto {
  id: string
  title: string
  scheduledAt: string
  createdAt: string
  streamDelaySeconds?: number
  youtubeUrl?: string
  rounds: RoundResponseDto[]
  status?: string
  headerImageUrl?: string
  adminUsername?: string
}

export interface CreatePrizeRequest {
  name: string
  type: string
  drawPattern: string
}

export interface CreateRoundRequest {
  roundNumber: number
  prizes: CreatePrizeRequest[]
}

export interface CreateEventRequest {
  title: string
  scheduledAt: string
  rounds: CreateRoundRequest[]
  youtubeUrl?: string
}

// Ticket Activation interfaces (Story 2.1)
export interface CardMatrixDto {
  b: number[]
  i: number[]
  n: number[]
  g: number[]
  o: number[]
}

export interface TicketMatrixDto {
  cards: CardMatrixDto[]
}

export interface TicketActivationResponseDto {
  ticketId: string
  eventId: string
  sessionToken: string
  serial: string
  matrix: TicketMatrixDto
}

export interface TicketValidationResponseDto {
  serial: string
  eventId: string
  eventTitle: string
  headerImageUrl: string | null
  activated: boolean
}

export interface ActivateTicketRequest {
  serial: string
  playerName: string
  playerPhone: string
}

/**
 * Safely resolves the API Base URL, guaranteeing that /api/v1 is present.
 * Uses window.location as fallback when running locally or on dynamic networks.
 */
export function getApiBaseUrl(): string {
  let envUrl = import.meta.env.VITE_API_BASE_URL
  if (!envUrl) {
    if (typeof window !== 'undefined' && window.location) {
      const protocol = window.location.protocol === 'https:' ? 'https:' : 'http:'
      const hostname = window.location.hostname || 'localhost'
      return `${protocol}//${hostname}:8080/api/v1`
    }
    return 'http://localhost:8080/api/v1'
  }
  envUrl = envUrl.replace(/\/+$/, '')
  if (!envUrl.endsWith('/api/v1')) {
    if (envUrl.endsWith('/api')) {
      envUrl = `${envUrl}/v1`
    } else {
      envUrl = `${envUrl}/api/v1`
    }
  }
  return envUrl
}

// Pre-configured Axios instance
const api = axios.create({
  baseURL: getApiBaseUrl(),
  headers: {
    'Content-Type': 'application/json'
  }
})

// Request interceptor to attach admin token
api.interceptors.request.use(
  (config) => {
    const token = sessionStorage.getItem('admin_session_token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// Response interceptor for global error handling
api.interceptors.response.use(
  (response) => response,
  (error) => {
    console.error('[API Error]', error.response?.status, error.message)
    // Add logic here to trigger global error notifications if needed
    return Promise.reject(error)
  }
)

export default api
