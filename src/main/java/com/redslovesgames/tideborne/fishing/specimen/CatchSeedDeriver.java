package com.redslovesgames.tideborne.fishing.specimen;

/** Stable deterministic seed splitting for one server-owned catch attempt. */
public final class CatchSeedDeriver {
    private static final long SELECTION_SALT = 0x2F3A4B5C6D7E8F01L;
    private static final long SPECIMEN_SALT = 0x71C6A9D24E3B580FL;

    private CatchSeedDeriver() {
    }

    public static long selectionSeed(long catchSeed) {
        return mix64(catchSeed ^ SELECTION_SALT);
    }

    public static long specimenSeed(long catchSeed) {
        return mix64(catchSeed ^ SPECIMEN_SALT);
    }

    static long mix64(long value) {
        long z = value;
        z = (z ^ (z >>> 30)) * 0xbf58476d1ce4e5b9L;
        z = (z ^ (z >>> 27)) * 0x94d049bb133111ebL;
        return z ^ (z >>> 31);
    }
}
