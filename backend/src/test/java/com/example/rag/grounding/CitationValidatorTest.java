package com.example.rag.grounding;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import org.junit.jupiter.api.Test;

class CitationValidatorTest {

    private final CitationValidator validator = new CitationValidator();

    @Test
    void rejectsTicketIdOutsideKeepSet() {
        assertThat(validator.allTicketIdsInKeepSet("See TKT-9999 for details", Set.of("TKT-1001")))
                .isFalse();
    }

    @Test
    void acceptsTicketIdsWithinKeepSet() {
        assertThat(
                        validator.allTicketIdsInKeepSet(
                                "See TKT-1001 and TKT-1002", Set.of("TKT-1001", "TKT-1002")))
                .isTrue();
    }
}