import { Client, type IMessage } from '@stomp/stompjs'
import SockJS from 'sockjs-client/dist/sockjs.min.js'

// ─── Typed WebSocket Message Interfaces ──────────────────────────────────────

/** Generic WebSocket message envelope (matches architecture.md schema) */
export interface WsMessage<T> {
  eventType: string
  timestamp: string
  payload: T
}

/** Payload for BALL_DRAWN events */
export interface WsBallDrawnPayload {
  ballNumber: number
  drawnSequence: number
}

/** Payload for BINGO_CLAIM events (used in Story 5.x) */
export interface WsBingoClaimPayload {
  ticketId: string
  sessionToken: string
}

/** Connection status type for reactive UI feedback */
export type WsConnectionStatus = 'disconnected' | 'connecting' | 'connected' | 'error'

// ─── WebSocket Service ────────────────────────────────────────────────────────

const WS_BASE_URL = 'http://localhost:8080/ws/bingo'

/**
 * Creates, configures, and activates a STOMP client connected to the bingo backend.
 *
 * Uses SockJS fallback for browsers that do not support native WebSocket or are
 * behind restrictive proxies (e.g., HTTP/1.1 load balancers that do not support
 * WebSocket upgrade).
 *
 * IMPORTANT: Import SockJS from 'sockjs-client/dist/sockjs.min.js' (not the default
 * export) to avoid CJS/ESM compatibility issues with Vite's ESM bundler.
 *
 * @param eventId       The UUID of the active bingo event to subscribe to
 * @param sessionToken  The player's session JWT (sent in CONNECT headers for future auth)
 * @param onBallDrawn   Callback invoked when a BALL_DRAWN message arrives
 * @param onStatusChange Callback invoked on connection lifecycle changes
 * @returns The activated STOMP Client instance (call client.deactivate() to disconnect)
 */
export function createWebSocketClient(
  eventId: string,
  sessionToken: string | null,
  onBallDrawn: (ball: WsBallDrawnPayload) => void,
  onBallRetracted: (ball: WsBallDrawnPayload) => void,
  onStatusChange: (status: WsConnectionStatus) => void
): Client {
  const client = new Client({
    // SockJS factory: provides HTTP long-polling fallback automatically
    webSocketFactory: () => new SockJS(WS_BASE_URL),

    // Include session token in STOMP CONNECT frame headers
    // (full JWT auth to be enforced in Story 5.x; currently permissive)
    connectHeaders: sessionToken ? { Authorization: `Bearer ${sessionToken}` } : {},

    // Reconnect automatically after 5 seconds on unexpected disconnects
    reconnectDelay: 5000,

    // Set connecting status before initiating connection/reconnection
    beforeConnect: () => {
      onStatusChange('connecting')
    },

    onConnect: () => {
      onStatusChange('connected')

      // Subscribe to the event-specific draw topic
      client.subscribe(`/topic/events/${eventId}/draws`, (msg: IMessage) => {
        try {
          const envelope: WsMessage<WsBallDrawnPayload> = JSON.parse(msg.body)
          if (envelope.eventType === 'BALL_DRAWN' && envelope.payload) {
            onBallDrawn(envelope.payload)
          } else if (envelope.eventType === 'BALL_RETRACTED' && envelope.payload) {
            onBallRetracted(envelope.payload)
          }
        } catch (e) {
          console.error('[WebSocket] Failed to parse draws message:', e)
        }
      })
    },

    onDisconnect: () => {
      onStatusChange('disconnected')
    },

    onStompError: (frame) => {
      console.error('[WebSocket] STOMP error:', frame)
      onStatusChange('error')
    },

    onWebSocketError: (event) => {
      console.error('[WebSocket] WebSocket error:', event)
      onStatusChange('error')
    }
  })

  onStatusChange('connecting')
  client.activate()

  return client
}
