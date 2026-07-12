-- 1) Adiciona colunas para identificar e limitar o acesso do jogador
ALTER TABLE tickets ADD COLUMN player_name VARCHAR(100);
ALTER TABLE tickets ADD COLUMN player_phone VARCHAR(30);

-- 2) Evita claims duplicados da mesma cartela/rodada/card.
DELETE FROM claims WHERE id IN (
  SELECT id FROM (
    SELECT id, ROW_NUMBER() OVER (PARTITION BY ticket_id, round_id, card_index ORDER BY created_at ASC) as rn
    FROM claims
  ) sub WHERE sub.rn > 1
);

ALTER TABLE claims
  ADD CONSTRAINT uq_claim_ticket_round_card
  UNIQUE (ticket_id, round_id, card_index);


-- 3) Garante número único de rodada no evento.
DELETE FROM rounds WHERE id IN (
  SELECT id FROM (
    SELECT id, ROW_NUMBER() OVER (PARTITION BY event_id, round_number ORDER BY status DESC) as rn
    FROM rounds
  ) sub WHERE sub.rn > 1
);

ALTER TABLE rounds
  ADD CONSTRAINT uq_round_event_number
  UNIQUE (event_id, round_number);

-- 4) Garante faixas de domínio básicas.
ALTER TABLE draws ADD CONSTRAINT chk_draw_ball_range CHECK (ball_number BETWEEN 1 AND 75);
ALTER TABLE draws ADD CONSTRAINT chk_draw_sequence_positive CHECK (drawn_sequence > 0);

ALTER TABLE events
  ADD CONSTRAINT chk_stream_delay_nonnegative CHECK (stream_delay_seconds >= 0);

ALTER TABLE claims
  ADD CONSTRAINT chk_claim_card_index_nonnegative CHECK (card_index >= 0);

-- 5) Acelera limpeza/consulta de sessões expiradas.
CREATE INDEX IF NOT EXISTS idx_admin_sessions_expires_at
  ON admin_sessions(expires_at);
