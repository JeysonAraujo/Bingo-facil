package com.bingoai.backend.ticket;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ActivateTicketRequest {

    @NotBlank(message = "O serial não pode estar em branco.")
    @Size(min = 6, max = 6, message = "O serial deve ter exatamente 6 caracteres.")
    private String serial;

    @NotBlank(message = "O nome não pode estar em branco.")
    @Size(max = 100, message = "O nome não pode ter mais de 100 caracteres.")
    private String playerName;

    @NotBlank(message = "O telefone não pode estar em branco.")
    @Size(max = 30, message = "O telefone não pode ter mais de 30 caracteres.")
    private String playerPhone;

    public ActivateTicketRequest() {}

    public String getSerial() { return serial; }
    public void setSerial(String serial) { this.serial = serial; }

    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }

    public String getPlayerPhone() { return playerPhone; }
    public void setPlayerPhone(String playerPhone) { this.playerPhone = playerPhone; }
}
