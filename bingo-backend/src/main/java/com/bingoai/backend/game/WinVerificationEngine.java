package com.bingoai.backend.game;

import com.bingoai.backend.ticket.CardMatrix;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class WinVerificationEngine {

    /**
     * Checks if a card matrix satisfies any of the winning patterns with the given marked numbers.
     *
     * @param card          the 5x5 card matrix containing column lists (b, i, n, g, o)
     * @param markedNumbers the set of marked numbers (drawn numbers + coordinator overrides)
     * @param patterns      the list of winning patterns to check (e.g. LINE, COLUMN, DIAGONAL, OUTER_BORDER, INNER_BORDER, FULL_CARD)
     * @return true if the card satisfies at least one winning pattern, false otherwise
     */
    public boolean verifyWin(CardMatrix card, Set<Integer> markedNumbers, List<String> patterns) {
        if (card == null || patterns == null || patterns.isEmpty()) {
            return false;
        }

        for (String pattern : patterns) {
            if (isPatternSatisfied(card, markedNumbers, pattern)) {
                return true;
            }
        }

        return false;
    }

    private boolean isPatternSatisfied(CardMatrix card, Set<Integer> markedNumbers, String pattern) {
        if (pattern == null) return false;
        
        switch (pattern.toUpperCase()) {
            case "LINE":
            case "ANY_LINE":
                return checkLine(card, markedNumbers);
            case "COLUMN":
            case "ANY_COLUMN":
                return checkColumn(card, markedNumbers);
            case "DIAGONAL":
                return checkDiagonal(card, markedNumbers);
            case "OUTER_BORDER":
            case "JANELÃO":
            case "JANELAO":
                return checkOuterBorder(card, markedNumbers);
            case "INNER_BORDER":
            case "JANELINHA":
                return checkInnerBorder(card, markedNumbers);
            case "FULL_CARD":
            case "FULL_GRID":
            case "BINGO_CHEIO":
                return checkFullCard(card, markedNumbers);
            default:
                return false;
        }
    }

    private boolean isCellMarked(CardMatrix card, Set<Integer> markedNumbers, int row, int col) {
        if (row == 2 && col == 2) {
            return true; // Center cell is FREE (always marked)
        }
        int value = getCellValue(card, row, col);
        return markedNumbers.contains(value);
    }

    private int getCellValue(CardMatrix card, int row, int col) {
        switch (col) {
            case 0: return card.getB().get(row);
            case 1: return card.getI().get(row);
            case 2: return card.getN().get(row);
            case 3: return card.getG().get(row);
            case 4: return card.getO().get(row);
            default: throw new IllegalArgumentException("Invalid column index: " + col);
        }
    }

    private boolean checkLine(CardMatrix card, Set<Integer> marked) {
        for (int row = 0; row < 5; row++) {
            boolean rowMarked = true;
            for (int col = 0; col < 5; col++) {
                if (!isCellMarked(card, marked, row, col)) {
                    rowMarked = false;
                    break;
                }
            }
            if (rowMarked) return true;
        }
        return false;
    }

    private boolean checkColumn(CardMatrix card, Set<Integer> marked) {
        for (int col = 0; col < 5; col++) {
            boolean colMarked = true;
            for (int row = 0; row < 5; row++) {
                if (!isCellMarked(card, marked, row, col)) {
                    colMarked = false;
                    break;
                }
            }
            if (colMarked) return true;
        }
        return false;
    }

    private boolean checkDiagonal(CardMatrix card, Set<Integer> marked) {
        // Main diagonal: (0,0), (1,1), (2,2), (3,3), (4,4)
        boolean mainDiagMarked = true;
        for (int i = 0; i < 5; i++) {
            if (!isCellMarked(card, marked, i, i)) {
                mainDiagMarked = false;
                break;
            }
        }
        if (mainDiagMarked) return true;

        // Anti-diagonal: (0,4), (1,3), (2,2), (3,1), (4,0)
        boolean antiDiagMarked = true;
        for (int i = 0; i < 5; i++) {
            if (!isCellMarked(card, marked, i, 4 - i)) {
                antiDiagMarked = false;
                break;
            }
        }
        return antiDiagMarked;
    }

    private boolean checkOuterBorder(CardMatrix card, Set<Integer> marked) {
        // Row 0
        for (int col = 0; col < 5; col++) {
            if (!isCellMarked(card, marked, 0, col)) return false;
        }
        // Row 4
        for (int col = 0; col < 5; col++) {
            if (!isCellMarked(card, marked, 4, col)) return false;
        }
        // Col 0 (left column, rows 1..3)
        for (int row = 1; row <= 3; row++) {
            if (!isCellMarked(card, marked, row, 0)) return false;
        }
        // Col 4 (right column, rows 1..3)
        for (int row = 1; row <= 3; row++) {
            if (!isCellMarked(card, marked, row, 4)) return false;
        }
        return true;
    }

    private boolean checkInnerBorder(CardMatrix card, Set<Integer> marked) {
        // Row 1 (cols 1..3)
        if (!isCellMarked(card, marked, 1, 1)) return false;
        if (!isCellMarked(card, marked, 1, 2)) return false;
        if (!isCellMarked(card, marked, 1, 3)) return false;

        // Row 2 (cols 1, 3)
        if (!isCellMarked(card, marked, 2, 1)) return false;
        if (!isCellMarked(card, marked, 2, 3)) return false;

        // Row 3 (cols 1..3)
        if (!isCellMarked(card, marked, 3, 1)) return false;
        if (!isCellMarked(card, marked, 3, 2)) return false;
        if (!isCellMarked(card, marked, 3, 3)) return false;

        return true;
    }

    private boolean checkFullCard(CardMatrix card, Set<Integer> marked) {
        for (int row = 0; row < 5; row++) {
            for (int col = 0; col < 5; col++) {
                if (!isCellMarked(card, marked, row, col)) {
                    return false;
                }
            }
        }
        return true;
    }
}
