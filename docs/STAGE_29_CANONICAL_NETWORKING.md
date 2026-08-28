# Stage 29: Canonical Fishing System 2.0 networking

## Scope

Stage 29 audits fish-related custom payloads after the canonical ItemStack, entity, bucket, display, Satchel, and Journal persistence migrations. The server remains the only authority that creates or mutates Fishing System 2.0 specimens.

## Authority boundary

The audited client-to-server payloads contain commands and query parameters only:

- `SatchelRequestPayload` sends action, hand, server state token, slot/boolean arguments, feature ID, and sort rules.
- `TeamDataRequestPayload` sends page and display-filter strings.

Neither payload accepts an ItemStack, canonical specimen NBT, percentile, length, Body Type, Condition, Pigmentation, Specimen Quality, Perfect Catch state, FishScore, deterministic seed, or any trait-generation input. The server resolves the authoritative held Satchel/team state and builds all returned fish data itself.

## Canonical Journal display projection

`JournalSpecimenNetworkCodec` is the single network projection for canonical Journal specimen snapshots. It reads the existing server-owned `JournalSpecimenStore` snapshots and serializes only display data:

- canonical species ID
- canonical final percentile
- canonical final length
- Body Type
- Condition
- Pigmentation
- Specimen Quality
- Perfect Catch flag
- optional canonical raw FishScore
- optional canonical mapped FishScore

The projection deliberately excludes deterministic seed, base percentile, base length, generation provenance, and the full canonical transfer/persistence compound. No network code recalculates traits, percentile, physical size, quality, or score.

Missing legacy data remains missing. If an older journal has no canonical sidecar, or a stored canonical snapshot is incomplete/invalid for the current schema, the server omits that display entry. It never fabricates a canonical specimen from legacy aggregate journal statistics.

## Protocol compatibility and duplicate removal

Existing payload IDs and packet codecs are unchanged.

`RecordHoldersPayload` remains an NBT payload on `tide_team_journal:record_holders`, so its wire-level registration and decoder remain compatible. Before encoding, the payload now sanitizes its NBT to the two metadata roots it owns:

- `tide_team_journal_record_holders`
- optional `tide_team_journal_canonical_specimens`

The full Tide journal is no longer duplicated inside the dedicated record-holder payload. Tide's existing server-to-client journal sync remains the compatibility transport for the legacy/current Journal UI, while the new canonical sidecar is display-only metadata for migrated consumers.

The existing `RecordHolderStore.attachForClient` call site is left compatibility-stable. `RecordHolderNetworkProjectionMixin` adds the canonical display sidecar to its server-built result, and `RecordHoldersPayload` strips unrelated journal/persistence data before that dedicated packet is encoded.

## Satchel audit

The Satchel view remains server-built and server-authoritative. Its S2C view contains copies of the server's stored ItemStacks, which already use the canonical ItemStack representation established in Stage 27. The corresponding C2S request contains no ItemStack or specimen fields, so a client cannot submit generated traits or replace canonical specimen state through the Satchel protocol.

No second specimen serialization format was introduced for Satchel contents.

## Coverage

`JournalSpecimenNetworkCodecTest` verifies:

1. canonical Journal snapshots round-trip through the display projection with final percentile, length, traits, quality, Perfect Catch, and FishScore preserved;
2. deterministic seed, base percentile, base length, schema/generation persistence fields, and full transfer compounds are absent from the display projection;
3. the dedicated record-holder payload sanitizer drops duplicated legacy journal fields and server persistence roots;
4. legacy-only and incomplete stored data produce no synthesized canonical client specimen;
5. absent optional FishScore fields remain absent rather than being recalculated.

Stage 29 intentionally does not redesign Journal or Satchel UI and does not begin later migration or cleanup stages.
