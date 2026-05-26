package com.bingoai.backend.ticket;

import java.util.List;

public class TicketMatrix {
    private List<CardMatrix> cards;

    public TicketMatrix() {}

    public TicketMatrix(List<CardMatrix> cards) {
        this.cards = cards;
    }

    public List<CardMatrix> getCards() { return cards; }
    public void setCards(List<CardMatrix> cards) { this.cards = cards; }
}
