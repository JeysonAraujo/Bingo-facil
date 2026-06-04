package com.bingoai.backend.game;

import com.bingoai.backend.ticket.CardMatrix;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class WinVerificationTests {

    private WinVerificationEngine engine;
    private CardMatrix card;

    @BeforeEach
    public void setUp() {
        engine = new WinVerificationEngine();
        // Setup a mock 5x5 card matrix
        card = new CardMatrix(
                Arrays.asList(1, 2, 3, 4, 5),      // B
                Arrays.asList(16, 17, 18, 19, 20),  // I
                Arrays.asList(31, 32, 0, 34, 35),   // N (FREE at index 2)
                Arrays.asList(46, 47, 48, 49, 50),  // G
                Arrays.asList(61, 62, 63, 64, 65)   // O
        );
    }

    @Test
    public void testVerifyWin_LinePattern() {
        // Row 0: 1, 16, 31, 46, 61
        Set<Integer> marked = new HashSet<>(Arrays.asList(1, 16, 31, 46, 61));
        assertTrue(engine.verifyWin(card, marked, Collections.singletonList("LINE")));

        // Row 2 (with FREE space): 3, 18, FREE, 48, 63
        Set<Integer> markedRow2 = new HashSet<>(Arrays.asList(3, 18, 48, 63)); // 0 is always marked, no need to add it
        assertTrue(engine.verifyWin(card, markedRow2, Collections.singletonList("LINE")));

        // Incomplete row
        Set<Integer> incomplete = new HashSet<>(Arrays.asList(1, 16, 31, 46));
        assertFalse(engine.verifyWin(card, incomplete, Collections.singletonList("LINE")));
    }

    @Test
    public void testVerifyWin_ColumnPattern() {
        // Column B: 1, 2, 3, 4, 5
        Set<Integer> markedColB = new HashSet<>(Arrays.asList(1, 2, 3, 4, 5));
        assertTrue(engine.verifyWin(card, markedColB, Collections.singletonList("COLUMN")));

        // Column N (with FREE space): 31, 32, FREE, 34, 35
        Set<Integer> markedColN = new HashSet<>(Arrays.asList(31, 32, 34, 35));
        assertTrue(engine.verifyWin(card, markedColN, Collections.singletonList("COLUMN")));

        // Incomplete column
        Set<Integer> incomplete = new HashSet<>(Arrays.asList(1, 2, 3, 4));
        assertFalse(engine.verifyWin(card, incomplete, Collections.singletonList("COLUMN")));
    }

    @Test
    public void testVerifyWin_DiagonalPattern() {
        // Main diagonal: 1, 17, FREE, 49, 65
        Set<Integer> mainDiag = new HashSet<>(Arrays.asList(1, 17, 49, 65));
        assertTrue(engine.verifyWin(card, mainDiag, Collections.singletonList("DIAGONAL")));

        // Anti-diagonal: 61, 47, FREE, 19, 5
        Set<Integer> antiDiag = new HashSet<>(Arrays.asList(61, 47, 19, 5));
        assertTrue(engine.verifyWin(card, antiDiag, Collections.singletonList("DIAGONAL")));

        // Incomplete diagonal
        Set<Integer> incomplete = new HashSet<>(Arrays.asList(1, 17, 49));
        assertFalse(engine.verifyWin(card, incomplete, Collections.singletonList("DIAGONAL")));
    }

    @Test
    public void testVerifyWin_OuterBorderPattern() {
        // Outer border cells:
        // Row 0: 1, 16, 31, 46, 61
        // Row 4: 5, 20, 35, 50, 65
        // Col 0: 2, 3, 4
        // Col 4: 62, 63, 64
        Set<Integer> outerBorder = new HashSet<>(Arrays.asList(
                1, 16, 31, 46, 61,
                5, 20, 35, 50, 65,
                2, 3, 4,
                62, 63, 64
        ));
        assertTrue(engine.verifyWin(card, outerBorder, Arrays.asList("OUTER_BORDER")));
        assertTrue(engine.verifyWin(card, outerBorder, Arrays.asList("JANELÃO")));

        // Incomplete outer border (missing 1 cell: 65)
        outerBorder.remove(65);
        assertFalse(engine.verifyWin(card, outerBorder, Collections.singletonList("OUTER_BORDER")));
    }

    @Test
    public void testVerifyWin_InnerBorderPattern() {
        // Inner border cells:
        // Row 1: 17, 32, 47
        // Row 2: 18, 48 (center is FREE)
        // Row 3: 19, 34, 49
        Set<Integer> innerBorder = new HashSet<>(Arrays.asList(
                17, 32, 47,
                18, 48,
                19, 34, 49
        ));
        assertTrue(engine.verifyWin(card, innerBorder, Arrays.asList("INNER_BORDER")));
        assertTrue(engine.verifyWin(card, innerBorder, Arrays.asList("JANELINHA")));

        // Incomplete inner border (missing 1 cell: 17)
        innerBorder.remove(17);
        assertFalse(engine.verifyWin(card, innerBorder, Collections.singletonList("INNER_BORDER")));
    }

    @Test
    public void testVerifyWin_FullCardPattern() {
        // All 24 numbers
        Set<Integer> allNumbers = new HashSet<>(Arrays.asList(
                1, 2, 3, 4, 5,
                16, 17, 18, 19, 20,
                31, 32, 34, 35,
                46, 47, 48, 49, 50,
                61, 62, 63, 64, 65
        ));
        assertTrue(engine.verifyWin(card, allNumbers, Collections.singletonList("FULL_CARD")));
        assertTrue(engine.verifyWin(card, allNumbers, Collections.singletonList("FULL_GRID")));

        // Missing one number (65)
        allNumbers.remove(65);
        assertFalse(engine.verifyWin(card, allNumbers, Collections.singletonList("FULL_CARD")));
    }
}
