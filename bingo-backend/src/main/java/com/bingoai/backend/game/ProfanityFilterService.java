package com.bingoai.backend.game;

import org.springframework.stereotype.Service;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class ProfanityFilterService {

    private static final List<String> BAD_WORDS = Arrays.asList(
            "caralho", "porra", "foder", "foda", "merda", "puta", "viado", "cu", 
            "filho da puta", "filhodaputa", "corno", "bosta", "buceta", "pica", 
            "cuzao", "cuzão", "cacete", "viadinho", "arrombado", "bicha"
    );

    public String filter(String text) {
        if (text == null || text.trim().isEmpty()) {
            return text;
        }
        String filteredText = text;
        for (String word : BAD_WORDS) {
            String regex = "(?i)\\b" + Pattern.quote(word) + "\\b";
            filteredText = filteredText.replaceAll(regex, "****");
            
            if (word.length() > 4) {
                String fallbackRegex = "(?i)" + Pattern.quote(word);
                filteredText = filteredText.replaceAll(fallbackRegex, "****");
            }
        }
        return filteredText;
    }
}
