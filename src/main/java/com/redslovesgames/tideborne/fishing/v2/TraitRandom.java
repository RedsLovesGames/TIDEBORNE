package com.redslovesgames.tideborne.fishing.v2;

/**
 * Stateless deterministic random splitting for canonical specimen trait axes.
 *
 * <p>Each outcome is a pure function of the canonical specimen seed and a fixed salt. Adding
 * or evaluating another axis therefore cannot consume shared state or shift an existing axis.
 */
public final class TraitRandom {
    private static final double UNIT_DOUBLE_SCALE = 0x1.0p-53;

    private TraitRandom() {
    }

    /**
     * Stable salts reserved for Fishing System 2.0 trait decisions.
     *
     * <p>Never change an existing value after it has shipped. New decisions must receive a new
     * salt instead of reusing one of these constants.
     */
    public static final class Salts {
        public static final long BODY_TYPE_EVENT = 0x2D99787926D46932L;
        public static final long BODY_TYPE_VARIANT = 0xA3C59AC3C14F91D5L;
        public static final long BODY_TYPE_SIZE = 0xE7037ED1A0B428DBL;
        public static final long CONDITION_EVENT = 0x6C8E9CF570932BD5L;
        public static final long CONDITION_VARIANT = 0xB8A7E7D6F31D4A19L;
        public static final long PIGMENTATION_EVENT = 0x41C64E6DA3C59A7BL;
        public static final long PIGMENTATION_VARIANT = 0xD1B54A32D192ED03L;
        public static final long PERFECT_SPECIMEN = 0x94D049BB133111EBL;

        private Salts() {
        }
    }

    /**
     * Returns a deterministic value in {@code [0, 1)} for one named random stream.
     */
    public static double unitDouble(long specimenSeed, long salt) {
        long bits = mixedLong(specimenSeed, salt);
        return (bits >>> 11) * UNIT_DOUBLE_SCALE;
    }

    /**
     * Returns the deterministic 64-bit split value backing a trait stream.
     */
    public static long mixedLong(long specimenSeed, long salt) {
        return mix64(specimenSeed ^ salt);
    }

    private static long mix64(long value) {
        value += 0x9E3779B97F4A7C15L;
        value = (value ^ (value >>> 30)) * 0xBF58476D1CE4E5B9L;
        value = (value ^ (value >>> 27)) * 0x94D049BB133111EBL;
        return value ^ (value >>> 31);
    }
}
