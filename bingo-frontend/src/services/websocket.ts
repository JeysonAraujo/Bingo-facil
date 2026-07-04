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
  timestamp?: string
}

/** Payload for BINGO_CLAIM events (used in Story 5.x) */
export interface WsBingoClaimPayload {
  ticketId: string
  sessionToken: string
}

/** Payload for announcements */
export interface WsAnnouncementPayload {
  text: string
}

/** Payload for forced ticket overrides */
export interface WsTicketOverridePayload {
  ticketId: string
  ballNumber: number
  action: 'MARK' | 'UNMARK'
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
 * @param eventId         The UUID of the active bingo event to subscribe to
 * @param sessionToken    The player's session JWT (sent in CONNECT headers for future auth)
 * @param onBallDrawn     Callback invoked when a BALL_DRAWN message arrives
 * @param onBallRetracted Callback invoked when a BALL_RETRACTED message arrives
 * @param onStatusChange  Callback invoked on connection lifecycle changes
 * @param ticketId        The player's ticket ID (optional, used to subscribe to overrides)
 * @param onAnnouncement  Callback invoked when a new administrative announcement is broadcasted
 * @param onTicketOverride Callback invoked when a cell override is forced on this ticket
 * @param onDelayChange   Callback invoked when a stream delay update is broadcasted
 * @returns The activated STOMP Client instance (call client.deactivate() to disconnect)
 */
export function createWebSocketClient(
  eventId: string,
  sessionToken: string | null,
  onBallDrawn: (ball: WsBallDrawnPayload) => void,
  onBallRetracted: (ball: WsBallDrawnPayload) => void,
  onStatusChange: (status: WsConnectionStatus) => void,
  ticketId?: string | null,
  onAnnouncement?: (announcement: WsAnnouncementPayload) => void,
  onTicketOverride?: (override: WsTicketOverridePayload) => void,
  onDelayChange?: (delaySeconds: number) => void,
  onClaim?: (claim: any) => void,
  onChatMessage?: (chat: any) => void,
  onRoundStatusChange?: (round: any) => void,
  onViewersChange?: (viewers: number) => void,
  onNaBoa?: (count: number) => void,
  onEventStatus?: (status: string) => void
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
            envelope.payload.timestamp = envelope.timestamp
            onBallDrawn(envelope.payload)
          } else if (envelope.eventType === 'BALL_RETRACTED' && envelope.payload) {
            onBallRetracted(envelope.payload)
          }
        } catch (e) {
          console.error('[WebSocket] Failed to parse draws message:', e)
        }
      })

      // Subscribe to the event-specific announcements topic if callback is provided
      if (onAnnouncement) {
        client.subscribe(`/topic/events/${eventId}/announcements`, (msg: IMessage) => {
          try {
            const envelope: WsMessage<WsAnnouncementPayload> = JSON.parse(msg.body)
            if (envelope.eventType === 'ANNOUNCEMENT' && envelope.payload) {
              onAnnouncement(envelope.payload)
            }
          } catch (e) {
            console.error('[WebSocket] Failed to parse announcements message:', e)
          }
        })
      }

      // Subscribe to ticket-specific overrides if ticketId and callback are provided
      if (ticketId && onTicketOverride) {
        client.subscribe(`/topic/tickets/${ticketId}/overrides`, (msg: IMessage) => {
          try {
            const envelope: WsMessage<WsTicketOverridePayload> = JSON.parse(msg.body)
            if (envelope.eventType === 'TICKET_OVERRIDE' && envelope.payload) {
              onTicketOverride(envelope.payload)
            }
          } catch (e) {
            console.error('[WebSocket] Failed to parse overrides message:', e)
          }
        })
      }

      // Subscribe to event delay topic if callback is provided
      if (onDelayChange) {
        client.subscribe(`/topic/events/${eventId}/delay`, (msg: IMessage) => {
          try {
            const envelope = JSON.parse(msg.body)
            if (envelope.eventType === 'DELAY_UPDATED' && envelope.payload) {
              onDelayChange(envelope.payload.streamDelaySeconds)
            }
          } catch (e) {
            console.error('[WebSocket] Failed to parse delay message:', e)
          }
        })
      }

      // Subscribe to event claims topic if callback is provided
      if (onClaim) {
        client.subscribe(`/topic/events/${eventId}/claims`, (msg: IMessage) => {
          try {
            const envelope = JSON.parse(msg.body)
            onClaim(envelope)
          } catch (e) {
            console.error('[WebSocket] Failed to parse claims message:', e)
          }
        })
      }

      // Subscribe to chat topic if callback is provided
      if (onChatMessage) {
        client.subscribe(`/topic/events/${eventId}/chat`, (msg: IMessage) => {
          try {
            const envelope = JSON.parse(msg.body)
            onChatMessage(envelope)
          } catch (e) {
            console.error('[WebSocket] Failed to parse chat message:', e)
          }
        })
      }

      // Subscribe to rounds topic if callback is provided
      if (onRoundStatusChange) {
        client.subscribe(`/topic/events/${eventId}/rounds`, (msg: IMessage) => {
          try {
            const envelope = JSON.parse(msg.body)
            onRoundStatusChange(envelope)
          } catch (e) {
            console.error('[WebSocket] Failed to parse round status message:', e)
          }
        })
      }

      // Subscribe to viewers topic if callback is provided
      if (onViewersChange) {
        client.subscribe(`/topic/events/${eventId}/viewers`, (msg: IMessage) => {
          try {
            const envelope = JSON.parse(msg.body)
            onViewersChange(envelope.viewerCount)
          } catch (e) {
            console.error('[WebSocket] Failed to parse viewers message:', e)
          }
        })
      }

      if (onNaBoa) {
        client.subscribe(`/topic/events/${eventId}/na-boa`, (msg: IMessage) => {
          try {
            const envelope = JSON.parse(msg.body)
            if (envelope && envelope.count !== undefined) {
              onNaBoa(envelope.count)
            }
          } catch (e) {
            console.error('[WebSocket] Failed to parse na-boa message:', e)
          }
        })
      }

      if (onEventStatus) {
        client.subscribe(`/topic/events/${eventId}/status`, (msg: IMessage) => {
          try {
            const envelope = JSON.parse(msg.body)
            if (envelope && envelope.status) {
              onEventStatus(envelope.status)
            }
          } catch (e) {
            console.error('[WebSocket] Failed to parse event status message:', e)
          }
        })
      }
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
