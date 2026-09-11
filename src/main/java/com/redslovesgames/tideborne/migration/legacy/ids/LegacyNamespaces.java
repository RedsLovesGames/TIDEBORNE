package com.redslovesgames.tideborne.migration.legacy.ids;

import net.minecraft.util.Identifier;

/** Historical Tideborne namespace identities retained only for external compatibility. */
public final class LegacyNamespaces {
    public static final String TIDE_TRAITS = "tide_traits";
    public static final String TIDE_TEAM_JOURNAL = "tide_team_journal";
    public static final String TIDEBOUND_COMPATIBILITY = "tidebound_compatibility";

    private LegacyNamespaces() {
    }

    public static Identifier tideTraits(String path) {
        return Identifier.of(TIDE_TRAITS, path);
    }

    public static Identifier tideTeamJournal(String path) {
        return Identifier.of(TIDE_TEAM_JOURNAL, path);
    }

    public static Identifier tideboundCompatibility(String path) {
        return Identifier.of(TIDEBOUND_COMPATIBILITY, path);
    }
}
