package com.hanout.search;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SearchTest {

    private SearchNormalizer normalizer;

    @BeforeEach
    void setUp() {
        normalizer = new SearchNormalizer();
    }

    @Test
    void testArabicNormalization() {
        // Diacritics/tashkeel + alef variants + ta marbuta
        String raw = "زَيْتُ زَيْتُونٍ إِكْسْتْرَا أَبْيَضْ هَرِيسَةٌ";
        String normalized = normalizer.normalizeArabic(raw);

        assertThat(normalized).doesNotContain("َ", "ُ", "ِ", "ٍ", "ْ", "ّ", "ٌ");
        assertThat(normalized).doesNotContain("إ", "أ", "ة");
        assertThat(normalized).contains("زيت", "زيتون", "هريسه");
    }

    @Test
    void testFrenchUnaccent() {
        String raw = "Huile d'olive extra-vièrge préparée à la créme";
        String normalized = normalizer.normalizeFrench(raw);

        assertThat(normalized).doesNotContain("è", "é", "à");
        assertThat(normalized).contains("vierge", "preparee", "a", "la", "creme");
    }

    @Test
    void testArabiziNormalization() {
        // 3 -> ع, 7 -> ح, 9 -> ق, 5 -> خ
        String arabizi = "7rissa 3arbi 9albi 5obz";
        String normalized = normalizer.normalizeArabizi(arabizi);

        assertThat(normalized).contains("حrissa");
        assertThat(normalized).contains("عarbi");
        assertThat(normalized).contains("قalbi");
        assertThat(normalized).contains("خobz");
    }
}
