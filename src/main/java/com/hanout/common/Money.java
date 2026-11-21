package com.hanout.common;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

@JsonSerialize(using = Money.MoneyJsonSerializer.class)
@JsonDeserialize(using = Money.MoneyJsonDeserializer.class)
public record Money(long millimes) implements Comparable<Money> {

    public static final Money ZERO = new Money(0L);

    public Money {
        // Millimes is standard for Tunisian Dinar (1 TND = 1000 millimes)
    }

    public static Money ofMillimes(long millimes) {
        return new Money(millimes);
    }

    public static Money ofDinar(BigDecimal dinars) {
        if (dinars == null) return ZERO;
        long millimes = dinars.multiply(BigDecimal.valueOf(1000))
                .setScale(0, RoundingMode.HALF_UP)
                .longValueExact();
        return new Money(millimes);
    }

    public static Money parse(String value) {
        if (value == null || value.isBlank()) {
            return ZERO;
        }
        BigDecimal dinars = new BigDecimal(value.trim());
        return ofDinar(dinars);
    }

    public Money plus(Money other) {
        Objects.requireNonNull(other, "other money must not be null");
        return new Money(Math.addExact(this.millimes, other.millimes));
    }

    public Money minus(Money other) {
        Objects.requireNonNull(other, "other money must not be null");
        return new Money(Math.subtractExact(this.millimes, other.millimes));
    }

    public Money multiply(long factor) {
        return new Money(Math.multiplyExact(this.millimes, factor));
    }

    public Money percent(BigDecimal pct) {
        Objects.requireNonNull(pct, "percentage must not be null");
        return new Money(BigDecimal.valueOf(millimes)
                .multiply(pct)
                .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP)
                .longValueExact());
    }

    /**
     * Given TTC (tax inclusive) amount and VAT rate (e.g., 19 for 19%),
     * derive HT (tax exclusive): HT = TTC / (1 + vatRate/100)
     */
    public Money deriveHt(BigDecimal vatRatePct) {
        if (vatRatePct == null || vatRatePct.compareTo(BigDecimal.ZERO) == 0) {
            return this;
        }
        BigDecimal divisor = BigDecimal.ONE.add(vatRatePct.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP));
        long htMillimes = BigDecimal.valueOf(millimes)
                .divide(divisor, 0, RoundingMode.HALF_UP)
                .longValueExact();
        return new Money(htMillimes);
    }

    public boolean isGreaterThan(Money other) {
        return this.millimes > other.millimes;
    }

    public boolean isGreaterThanOrEqualTo(Money other) {
        return this.millimes >= other.millimes;
    }

    public boolean isLessThan(Money other) {
        return this.millimes < other.millimes;
    }

    public boolean isLessThanOrEqualTo(Money other) {
        return this.millimes <= other.millimes;
    }

    public boolean isPositive() {
        return this.millimes > 0;
    }

    public String toDinarString() {
        BigDecimal dinars = BigDecimal.valueOf(millimes).divide(BigDecimal.valueOf(1000), 3, RoundingMode.UNNECESSARY);
        return dinars.toPlainString();
    }

    @Override
    public int compareTo(Money o) {
        return Long.compare(this.millimes, o.millimes);
    }

    @Override
    public String toString() {
        return toDinarString();
    }

    public static class MoneyJsonSerializer extends JsonSerializer<Money> {
        @Override
        public void serialize(Money value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            if (value == null) {
                gen.writeNull();
            } else {
                gen.writeString(value.toDinarString());
            }
        }
    }

    public static class MoneyJsonDeserializer extends JsonDeserializer<Money> {
        @Override
        public Money deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            String text = p.getText();
            return Money.parse(text);
        }
    }
}
