package com.hanout.search;

import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.regex.Pattern;

@Component
public class SearchNormalizer {

    private static final Pattern ARABIC_DIACRITICS = Pattern.compile("[\\u064B-\\u065F\\u0670]");
    private static final Pattern FRENCH_ACCENTS = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");

    public String normalizeArabic(String input) {
        if (input == null) return "";
        // Strip tashkeel/diacritics
        String stripped = ARABIC_DIACRITICS.matcher(input).replaceAll("");
        // Unify alef variants
        stripped = stripped.replace('أ', 'ا')
                .replace('إ', 'ا')
                .replace('آ', 'ا')
                .replace('ٱ', 'ا');
        // Unify ta marbuta
        stripped = stripped.replace('ة', 'ه');
        // Unify alif maqsura
        stripped = stripped.replace('ى', 'ي');
        return stripped.trim().toLowerCase();
    }

    public String normalizeFrench(String input) {
        if (input == null) return "";
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        String stripped = FRENCH_ACCENTS.matcher(normalized).replaceAll("");
        return stripped.toLowerCase().trim();
    }

    /**
     * Map Tunisian Arabizi numerals to Arabic characters:
     * 3 -> ع
     * 7 -> ح
     * 9 -> ق
     * 5 -> خ
     * 2 -> ء
     */
    public String normalizeArabizi(String input) {
        if (input == null) return "";
        String s = input.toLowerCase().trim();
        return s.replace('3', 'ع')
                .replace('7', 'ح')
                .replace('9', 'ق')
                .replace('5', 'خ')
                .replace('2', 'ء');
    }

    public String normalizeAll(String input) {
        if (input == null) return "";
        String french = normalizeFrench(input);
        return normalizeArabic(french);
    }
}
