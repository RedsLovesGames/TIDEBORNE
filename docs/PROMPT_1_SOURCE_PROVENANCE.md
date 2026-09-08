# Prompt 1 source provenance limitation

Updated: 2026-09-08

Prompt 1 package ownership migration is structurally complete. The established migration landed 251 production Java moves and 67 existing test Java moves, and the package-ownership architecture test is present. Active production Java ownership is unified under `com.redslovesgames.tideborne`; the historical production Java package identities `com.redslovesgames.tidetraits`, `com.redslovesgames.tideteamjournal`, and `com.redslovesgames.tideboundcompatibility`, plus the production `com.redslovesgames.tideborne.fishing.v2` package/path, are not part of the intended current Java ownership.

## Accepted recovery limitation

The exact byte-for-byte 564-file Astra baseline was **not** reconstructed. After exhausting the available recovery sources, 75 authoritative file/hash entries remain unavailable. This is an accepted source-provenance limitation, not evidence that the package migration itself still needs to be repeated.

Downstream work must not claim that byte-for-byte Astra reconstruction succeeded. It must not fabricate, approximate, regenerate, or silently replace unavailable authoritative files solely to satisfy the historical exact-baseline requirement. Persisted historical IDs and resource/network/save identifiers must not be renamed as a consequence of Java package ownership cleanup.

## Validation impact

The provenance limitation is not entirely passive in the current recovered tree. At least one unavailable authoritative source, `src/main/java/com/redslovesgames/tideborne/fishing/gametest/GearArchetypeCases.java` (authoritative SHA-256 `15b852a8918a664d7251e94fd0f790508bfd089b343bac7b3debdf18bcc89276`, recorded size 4489 bytes), is an active compile-time dependency of the existing core GameTests. The recovered tree contains only the structural package placeholder for that file, and no exact implementation was found in the available recovery sources.

Therefore Prompt 1 must not be declared fully validated while Java 21 compilation remains blocked by unavailable authoritative source. Do not weaken or delete the existing tests and do not invent an implementation to force a green result. If the authoritative source becomes available later, restore it exactly and resume the existing validation matrix without redoing the package migration.

Exact recoveries that were available from verified repository history, including `DiscoveryTotals`, `SatchelSpecimenDisplay`, and `TideTeamJournalServiceMixin`, may be restored because their source provenance is verifiable. Such recoveries do not change the accepted 75-file limitation for the unavailable Astra set.
