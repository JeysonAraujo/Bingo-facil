package com.bingoai.backend.ticket;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
public class TicketGenerator {

    private static final String LETTERS_POOL = "ABCDEFGHJKLMNPQRSTUVWXYZ";
    private static final String DIGITS_POOL = "0123456789";
    private final SecureRandom random = new SecureRandom();

    public String generateSerial() {
        StringBuilder sb = new StringBuilder(6);
        for (int i = 0; i < 2; i++) {
            sb.append(LETTERS_POOL.charAt(random.nextInt(LETTERS_POOL.length())));
        }
        for (int i = 0; i < 4; i++) {
            sb.append(DIGITS_POOL.charAt(random.nextInt(DIGITS_POOL.length())));
        }
        return sb.toString();
    }

    public CardMatrix generateCard() {
        return new CardMatrix(
            generateColumn(1, 15, 5),
            generateColumn(16, 30, 5),
            generateCenterColumn(),
            generateColumn(46, 60, 5),
            generateColumn(61, 75, 5)
        );
    }

    private List<Integer> generateColumn(int min, int max, int count) {
        List<Integer> pool = IntStream.rangeClosed(min, max).boxed().collect(Collectors.toList());
        Collections.shuffle(pool, random);
        List<Integer> column = new ArrayList<>(pool.subList(0, count));
        Collections.sort(column);
        return column;
    }

    private List<Integer> generateCenterColumn() {
        List<Integer> pool = IntStream.rangeClosed(31, 45).boxed().collect(Collectors.toList());
        Collections.shuffle(pool, random);
        List<Integer> column = new ArrayList<>(pool.subList(0, 4));
        Collections.sort(column);
        column.add(2, 0); // Insert FREE space at index 2 (center)
        return column;
    }
}
