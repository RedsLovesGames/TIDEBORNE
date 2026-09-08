package com.redslovesgames.tideborne.fishing.gear;

/** Material leader progression. Iron intentionally retains the legacy steel_leader registry ID. */
public enum LeaderTier {
    COPPER("copper", 0.30D, 0.98D, 1.01D),
    IRON("iron", 0.55D, 0.95D, 1.03D),
    GOLD("gold", 0.75D, 0.90D, 1.06D),
    DIAMOND("diamond", 0.95D, 0.82D, 1.10D);

    private final String id;
    private final double protection;
    private final double catchZoneMultiplier;
    private final double fishSpeedMultiplier;

    LeaderTier(String id, double protection, double catchZoneMultiplier, double fishSpeedMultiplier) {
        this.id = id;
        this.protection = protection;
        this.catchZoneMultiplier = catchZoneMultiplier;
        this.fishSpeedMultiplier = fishSpeedMultiplier;
    }
    public String id(){ return id; }
    public double protection(){ return protection; }
    public double catchZoneMultiplier(){ return catchZoneMultiplier; }
    public double fishSpeedMultiplier(){ return fishSpeedMultiplier; }
    public static LeaderTier parse(String value) {
        if (value == null) return null;
        for (LeaderTier tier : values()) if (tier.id.equalsIgnoreCase(value)) return tier;
        return null;
    }
}
