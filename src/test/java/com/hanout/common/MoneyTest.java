package com.hanout.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class MoneyTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void testMillimesArithmetic() {
        Money m1 = Money.ofMillimes(12500); // 12.500 TND
        Money m2 = Money.ofMillimes(2500);  // 2.500 TND

        assertThat(m1.plus(m2)).isEqualTo(Money.ofMillimes(15000));
        assertThat(m1.minus(m2)).isEqualTo(Money.ofMillimes(10000));
        assertThat(m2.multiply(4)).isEqualTo(Money.ofMillimes(10000));
    }

    @Test
    void testPercent() {
        Money total = Money.ofMillimes(100000); // 100.000 TND
        Money tenPercent = total.percent(BigDecimal.valueOf(10.0));
        assertThat(tenPercent).isEqualTo(Money.ofMillimes(10000));

        Money ninetyFive = total.percent(BigDecimal.valueOf(95.0));
        assertThat(ninetyFive).isEqualTo(Money.ofMillimes(95000));
    }

    @Test
    void testDeriveHtFromTtc() {
        // At 19% VAT, 119.000 TND TTC -> 100.000 TND HT
        Money ttc = Money.ofMillimes(119000);
        Money ht = ttc.deriveHt(BigDecimal.valueOf(19.0));
        assertThat(ht.millimes()).isEqualTo(100000L);
    }

    @Test
    void testJsonSerialization() throws Exception {
        record Sample(Money price) {}

        Sample s = new Sample(Money.ofMillimes(12500));
        String json = mapper.writeValueAsString(s);
        assertThat(json).contains("\"price\":\"12.500\"");

        Sample deserialized = mapper.readValue("{\"price\":\"12.500\"}", Sample.class);
        assertThat(deserialized.price()).isEqualTo(Money.ofMillimes(12500));
    }
}
