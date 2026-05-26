package com.bingoai.backend.ticket;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ActivateTicketRequest {

    @NotBlank(message = "O serial não pode estar em branco.")
    @Size(min = 6, max = 6, message = "O serial deve ter exatamente 6 caracteres.")
    private String serial;

    public ActivateTicketRequest() {}

    public String getSerial() { return serial; }
    public void setSerial(String serial) { this.serial = serial; }
}
