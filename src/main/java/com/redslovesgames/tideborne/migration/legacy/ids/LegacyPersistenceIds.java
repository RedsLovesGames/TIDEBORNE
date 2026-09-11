package com.redslovesgames.tideborne.migration.legacy.ids;

/** Frozen historical save keys and filenames retained for old-world/config compatibility. */
public final class LegacyPersistenceIds {
    public static final String TEAM_JOURNAL_ROOT = "tide_team_journal";
    public static final String SHARED_DISCOVERY_TRAITS = "tide_traits";
    public static final String JOURNAL_CANONICAL_SPECIMENS = "tide_team_journal_canonical_specimens";
    public static final String JOURNAL_RECORD_HOLDERS = "tide_team_journal_record_holders";

    public static final String TRAITS_CONFIG = "tide_traits.json";
    public static final String JOURNAL_SERVER_CONFIG = "tide_team_journal-server.json";
    public static final String JOURNAL_CLIENT_CONFIG = "tide_team_journal-client.json";
    public static final String TIDEBOUND_SERVER_CONFIG = "tidebound_compatibility.json";
    public static final String TIDEBOUND_CLIENT_CONFIG = "tidebound_compatibility-client.json";

    private LegacyPersistenceIds() {
    }
}
