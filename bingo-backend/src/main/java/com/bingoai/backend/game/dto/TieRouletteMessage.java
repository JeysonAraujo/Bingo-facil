package com.bingoai.backend.game.dto;

import java.util.List;

public class TieRouletteMessage {
    private String eventType = "TIE_ROULETTE";
    private String timestamp = java.time.Instant.now().toString();
    private Payload payload;

    public TieRouletteMessage(List<Participant> participants, String winnerSerial, String winnerName) {
        this.payload = new Payload(participants, winnerSerial, winnerName);
    }

    public String getEventType() { return eventType; }
    public String getTimestamp() { return timestamp; }
    public Payload getPayload() { return payload; }

    public static class Payload {
        private List<Participant> participants;
        private String winnerSerial;
        private String winnerName;

        public Payload(List<Participant> participants, String winnerSerial, String winnerName) {
            this.participants = participants;
            this.winnerSerial = winnerSerial;
            this.winnerName = winnerName;
        }

        public List<Participant> getParticipants() { return participants; }
        public String getWinnerSerial() { return winnerSerial; }
        public String getWinnerName() { return winnerName; }
    }

    public static class Participant {
        private String serial;
        private String name;

        public Participant(String serial, String name) {
            this.serial = serial;
            this.name = name;
        }

        public String getSerial() { return serial; }
        public String getName() { return name; }
    }
}
