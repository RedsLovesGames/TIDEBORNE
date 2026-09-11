package com.redslovesgames.tideborne.migration.legacy.ids;

import net.minecraft.util.Identifier;

/** Frozen historical payload identifiers used for wire compatibility. */
public final class LegacyNetworkIds {
    public static final Identifier DISCOVERY_SYNC = LegacyNamespaces.tideTraits("discovery_sync");
    public static final Identifier SHARED_DISCOVERY_REQUEST = LegacyNamespaces.tideTraits("shared_discovery_request");
    public static final Identifier SHARED_DISCOVERY_SYNC = LegacyNamespaces.tideTraits("shared_discovery_sync");
    public static final Identifier SATCHEL_REQUEST = LegacyNamespaces.tideTraits("anglers_satchel_request");
    public static final Identifier SATCHEL_VIEW = LegacyNamespaces.tideTraits("anglers_satchel_view");

    public static final Identifier RECORD_HOLDERS = LegacyNamespaces.tideTeamJournal("record_holders");
    public static final Identifier TEAM_DATA = LegacyNamespaces.tideTeamJournal("team_data");
    public static final Identifier RECORD_EVENT = LegacyNamespaces.tideTeamJournal("record_event");
    public static final Identifier BOBBER_SETTINGS = LegacyNamespaces.tideTeamJournal("bobber_settings");
    public static final Identifier OPEN_TEAM_RECORDS = LegacyNamespaces.tideTeamJournal("open_team_records");
    public static final Identifier TEAM_DATA_REQUEST = LegacyNamespaces.tideTeamJournal("team_data_request");

    public static final Identifier TIDEBOUND_SETTINGS = LegacyNamespaces.tideboundCompatibility("settings");
    public static final Identifier TIDEBOUND_SETTINGS_RESULT = LegacyNamespaces.tideboundCompatibility("settings_result");
    public static final Identifier TIDEBOUND_SETTINGS_UPDATE = LegacyNamespaces.tideboundCompatibility("settings_update");
    public static final Identifier SHARK_CATCH_LOSS = LegacyNamespaces.tideboundCompatibility("shark_catch_loss");

    private LegacyNetworkIds() {
    }
}
