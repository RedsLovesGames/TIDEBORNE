package com.redslovesgames.tideborne.fishing.v2;

/** Canonical rarity shared by Tide and compatibility species. */
public enum CanonicalRarity {
    ONE_STAR(1, 0.00),
    TWO_STAR(2, 0.08),
    THREE_STAR(3, 0.16),
    FOUR_STAR(4, 0.24),
    FIVE_STAR(5, 0.32);

    private final int stars;
    private final double fishingLuckCoefficient;

    CanonicalRarity(int stars, double fishingLuckCoefficient) {
        this.stars = stars;
        this.fishingLuckCoefficient = fishingLuckCoefficient;
    }

    public int stars() {
        return stars;
    }

    public double fishingLuckCoefficient() {
        return fishingLuckCoefficient;
    }

    /**
     * Applies W' = W * (1 + C_R * sqrt(max(L, 0))).
     * Negative Fishing Luck is intentionally neutral in Fishing System 2.0.
     */
    public double fishingLuckMultiplier(double fishingLuck) {
        requireFinite("fishingLuck", fishingLuck);
        return 1.0 + fishingLuckCoefficient * Math.sqrt(Math.max(fishingLuck, 0.0));
    }

    public static CanonicalRarity fromStars(int stars) {
        for (CanonicalRarity rarity : values()) {
            if (rarity.stars == stars) {
                return rarity;
            }
        }
        throw new IllegalArgumentException("Canonical rarity must be between 1 and 5 stars: " + stars);
    }

    private static void requireFinite(String name, double value) {
        if (!Double.isFinite(value)) {
            throw new IllegalArgumentException(name + " must be finite");
        }
    }
}

