package com.example.eval;

import com.example.ticket.api.dto.CreateTicketRequest;

public final class RetrievalCalibrationDecoyFixture {

    public static final CreateTicketRequest DECOY_TICKET =
            new CreateTicketRequest(
                    "Billing invoice export failure",
                    "Quarterly billing reconciliation CSV export",
                    null,
                    "bob",
                    "billing");

    public static final CreateTicketRequest SSO_TICKET =
            new CreateTicketRequest(
                    "SSO login loop",
                    "User login failure after SSO provider change",
                    null,
                    "carol",
                    "auth");

    public static final CreateTicketRequest BILLING_DECOY =
            new CreateTicketRequest(
                    "Quarterly billing reconciliation",
                    "Finance needs March totals",
                    null,
                    "dana",
                    "billing");

    private RetrievalCalibrationDecoyFixture() {}
}