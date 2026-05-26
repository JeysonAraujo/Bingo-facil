package com.bingoai.backend.ticket;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class TicketGeneratorTest {

    private final TicketGenerator generator = new TicketGenerator();

    @Test
    void testGenerateSerial_LengthAndCharacters() {
        String serial = generator.generateSerial();
        assertNotNull(serial);
        assertEquals(6, serial.length());
        assertTrue(serial.matches("^[ABCDEFGHJKLMNPQRSTUVWXYZ23456789]{6}$"));
    }

    @Test
    void testGenerateCard_Standard75BallRules() {
        CardMatrix card = generator.generateCard();
        assertNotNull(card);

        assertColumn(card.getB(), 5, 1, 15);
        assertColumn(card.getI(), 5, 16, 30);
        
        List<Integer> n = card.getN();
        assertNotNull(n);
        assertEquals(5, n.size());
        assertEquals(0, n.get(2), "Center space must be FREE (0)");
        
        // Remove the 0 to check the rest
        List<Integer> nWithoutFree = new java.util.ArrayList<>(n);
        nWithoutFree.remove(2);
        assertTrue(nWithoutFree.stream().allMatch(val -> val >= 31 && val <= 45), "N column values out of range");
        assertTrue(isSorted(nWithoutFree), "N column must be sorted");
        
        assertColumn(card.getG(), 5, 46, 60);
        assertColumn(card.getO(), 5, 61, 75);
    }

    private void assertColumn(List<Integer> column, int expectedSize, int min, int max) {
        assertNotNull(column);
        assertEquals(expectedSize, column.size());
        assertTrue(column.stream().allMatch(val -> val >= min && val <= max), "Column values out of range");
        assertTrue(isSorted(column), "Column must be sorted");
        long uniqueCount = column.stream().distinct().count();
        assertEquals(expectedSize, uniqueCount, "Column must not have duplicates");
    }

    private boolean isSorted(List<Integer> list) {
        for (int i = 0; i < list.size() - 1; i++) {
            if (list.get(i) > list.get(i + 1)) return false;
        }
        return true;
    }
}
