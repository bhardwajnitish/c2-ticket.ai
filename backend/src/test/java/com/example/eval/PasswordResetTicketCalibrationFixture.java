package com.example.eval;

import com.example.ticket.api.dto.CreateTicketRequest;

public final class PasswordResetTicketCalibrationFixture {

    public static final CreateTicketRequest TARGET_TICKET =
            new CreateTicketRequest(
                    "Password reset email missing",
                    "User does not receive password reset email",
                    null,
                    "alice",
                    "auth");

    private PasswordResetTicketCalibrationFixture() {}
}