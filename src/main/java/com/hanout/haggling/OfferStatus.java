package com.hanout.haggling;

import java.util.Map;
import java.util.Set;

public enum OfferStatus {
    PENDING,
    COUNTERED,
    ACCEPTED,
    REJECTED,
    EXPIRED,
    WITHDRAWN,
    CONVERTED;

    private static final Map<OfferStatus, Set<OfferStatus>> ALLOWED = Map.of(
            PENDING, Set.of(ACCEPTED, REJECTED, COUNTERED, EXPIRED, WITHDRAWN),
            COUNTERED, Set.of(ACCEPTED, REJECTED, PENDING, WITHDRAWN, EXPIRED),
            ACCEPTED, Set.of(CONVERTED, EXPIRED),
            REJECTED, Set.of(),
            EXPIRED, Set.of(),
            WITHDRAWN, Set.of(),
            CONVERTED, Set.of()
    );

    public boolean canMoveTo(OfferStatus next) {
        if (next == null) return false;
        Set<OfferStatus> targets = ALLOWED.get(this);
        return targets != null && targets.contains(next);
    }
}
