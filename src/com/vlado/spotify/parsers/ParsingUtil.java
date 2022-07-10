package com.vlado.spotify.parsers;

import com.vlado.spotify.validations.ParameterValidator;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ParsingUtil {

    public static String getFirstWord(String input) {
        ParameterValidator.checkNull(input, "input");

        StringBuilder result = new StringBuilder();
        for (char a : input.toCharArray()) {
            if (a == ' ') {
                break;
            }
            result.append(a);
        }

        return result.toString();
    }

    public static String[] multipleWordArgsSplit(String input) {
        ParameterValidator.checkNull(input, "input");

        List<String> tokens = new ArrayList<>();
        StringBuilder sb = new StringBuilder();

        boolean insideQuote = false;

        String word;
        for (char c : input.toCharArray()) {
            if (c == '"') {
                insideQuote = !insideQuote;
            }
            if (c == ' ' && !insideQuote) {
                word = sb.toString();
                if (!word.isEmpty() && !word.isBlank()) {
                    tokens.add(word.replace("\"", ""));
                }
                sb.delete(0, sb.length());
            } else {
                sb.append(c);
            }
        }

        word = sb.toString();
        if (!word.isEmpty() && !word.isBlank()) {
            tokens.add(word.replace("\"", ""));
        }

        String[] result = new String[tokens.size()];
        return tokens.toArray(result);
    }
}
