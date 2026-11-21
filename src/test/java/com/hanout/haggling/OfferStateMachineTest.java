package com.hanout.haggling;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class OfferStateMachineTest {

    @ParameterizedTest(name = "{0} canMoveTo {1} should be {2}")
    @CsvSource({
            // PENDING transitions
            "PENDING, ACCEPTED, true",
            "PENDING, REJECTED, true",
            "PENDING, COUNTERED, true",
            "PENDING, EXPIRED, true",
            "PENDING, WITHDRAWN, true",
            "PENDING, CONVERTED, false",

            // COUNTERED transitions
            "COUNTERED, ACCEPTED, true",
            "COUNTERED, REJECTED, true",
            "COUNTERED, PENDING, true",
            "COUNTERED, WITHDRAWN, true",
            "COUNTERED, EXPIRED, true",
            "COUNTERED, CONVERTED, false",

            // ACCEPTED transitions
            "ACCEPTED, CONVERTED, true",
            "ACCEPTED, EXPIRED, true",
            "ACCEPTED, PENDING, false",
            "ACCEPTED, COUNTERED, false",
            "ACCEPTED, REJECTED, false",
            "ACCEPTED, WITHDRAWN, false",

            // Terminal states
            "REJECTED, PENDING, false",
            "REJECTED, ACCEPTED, false",
            "EXPIRED, PENDING, false",
            "EXPIRED, ACCEPTED, false",
            "WITHDRAWN, PENDING, false",
            "CONVERTED, PENDING, false",
            "CONVERTED, ACCEPTED, false"
    })
    @DisplayName("Verify valid and invalid state machine transitions")
    void verifyTransitions(OfferStatus from, OfferStatus to, boolean expected) {
        assertThat(from.canMoveTo(to)).isEqualTo(expected);
    }
}
