package com.bingoai.backend.ticket;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class TicketJsonMappingTest {

    @Test
    void testTicketMatrixAndOverridesConfiguration() {
        Ticket ticket = new Ticket();
        TicketMatrix matrix = new TicketMatrix(List.of());
        ticket.setMatrix(matrix);
        ticket.setMarkedOverrides(List.of(1, 2, 3));

        assertNotNull(ticket.getMatrix());
        assertEquals(3, ticket.getMarkedOverrides().size());
        assertTrue(ticket.getMarkedOverrides().contains(2));
    }
}
