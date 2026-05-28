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
  rounds: RoundResponseDto[]
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
  sessionToken: string
  serial: string
  matrix: TicketMatrixDto
}

export interface ActivateTicketRequest {
  serial: string
}

// Pre-configured Axios instance
const api = axios.create({
  baseURL: 'http://localhost:8080/api/v1',
  headers: {
    'Content-Type': 'application/json'
  }
})

export default api
