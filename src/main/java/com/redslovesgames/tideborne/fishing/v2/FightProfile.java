package com.redslovesgames.tideborne.fishing.v2;

/** Normalized values consumed by the existing Tide marker and catch-region minigame. */
public record FightProfile(double strength, double tempo, double catchZoneArea, String behavior) {
    public FightProfile {
        if (!Double.isFinite(strength) || strength < 0.0) {
            throw new IllegalArgumentException("strength must be finite and nonnegative");
        }
        if (!Double.isFinite(tempo) || tempo < 0.0) {
            throw new IllegalArgumentException("tempo must be finite and nonnegative");
        }
        if (!Double.isFinite(catchZoneArea) || catchZoneArea <= 0.0 || catchZoneArea > 1.0) {
            throw new IllegalArgumentException("catchZoneArea must be in (0, 1]");
        }
        if (behavior == null || behavior.isBlank()) {
            throw new IllegalArgumentException("behavior is required");
        }
    }
}

