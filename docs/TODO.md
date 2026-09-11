# Tideborne post-2.0 backlog

Updated: 2026-09-11

Fishing System 2.0 implementation, recovery tooling, integration polish, canonical gear identity, the dedicated Tideborne creative tab, Stage 64 record/balance work, release-versioning correction, canonical internal fishing API, canonical specimen presentation migration, Tide-targeting mixin inventory, focused Tide mixin reduction, the legacy/package cleanup pass, the existing Fishing System 2.0 gear-path audit, the P1-P8 architecture sequence, and the P9 final architecture audit are complete on `dev` or its validated final-audit integration branch as applicable.

The authoritative current state is documented in `docs/CURRENT_STATE.md`; the final integrated architecture evidence is recorded in `.github/TIDEBORNE_P9_AUDIT_LEDGER.md`.

## Fishing System 2.0

- [x] Complete canonical species, specimen, traits, Momentum, score, persistence, migration, gear, compatibility, Journal, records, and networking paths.
- [x] Guard duplicate legacy runtime calculations so canonical V2 state remains authoritative.
- [x] Validate server authority, multiplayer synchronization, optional-mod matrices, and dedicated-server behavior.
- [x] Complete Stages 59 through 64 and the later Fishing Journal and Team Top 15 presentation fixes.
- [x] Freeze the exact public 2.0.0 build identity in `docs/CURRENT_STATE.md`.

## Post-2.0 gear progression

- [x] Audit all existing Fishing System 2.0 gear paths, IDs, modifiers, effect stages, canonical specimen boundaries, duplicated logic, hard-coded checks, Tide mixins, Satchel behavior, and APIs to preserve. See `docs/GEAR_PROGRESSION_AUDIT.md`.
- [x] Freeze the coordinated design in [POST_2_0_GEAR_PROGRESSION_SPEC.md](POST_2_0_GEAR_PROGRESSION_SPEC.md), including all slot identities, seven archetypes, exact values, composed limits, and implementation conflicts. This is a documentation-only completion; gameplay remains unchanged.
- [x] Prompt 2: extend the existing modifier/effect/fight/API boundaries with complete-contribution clamps, immutable species target metadata, target/environment weighting, and specimen-surcharge-only Trophy Fight Relief. Consolidate the canonical fight composition entry point; validate with focused unit tests, build, and 59 core GameTests.
- [x] Extend and consolidate `FishingGearRegistry`, `FishingGearModifiers`, `FishingGearEffects`, `FightProfileService`, `TideborneFishingApi`, and canonical species/specimen services to implement the frozen contract. Do not introduce a parallel loadout/registry/modifier/resolver/specimen layer without current-source proof of necessity.
- [x] Implement the global limits after complete gear composition; preserve native luck/environment attribution, non-gear Momentum/Perfect Catch inputs, multiple-bait semantics, and single application of each effect.
- [x] Prompt 3: all specified rods/lines, Trophy Fight Relief, Kujira targeting, native Gold luck preservation, optional guards, and canonical protection. Preserve native casting and treat historical range/natural-break values as reference-only per the user's clarification. Validated by build, focused tests, and core GameTests.
- [x] Apply Kujira and Leviathan numeric changes, Echo/Chorus/Heart penalties and Heart defaults, and metadata-based Incandescent/Abyss/Kujira targeting. Preserve eligibility, fish-only fallback, optional gating, stable IDs, and compatible config/payload handling.
- [ ] Resolve the intended default Leviathan/boss fish roster. The 2x canonical boss modifier and extensible leviathan_targets tag are implemented; the default tag is empty because exact Tide/Myths data define no eligible boss fish classification. No new encounters are invented.
- [x] Preserve matching line/hook/leader/bobber behavior and third-party bobber fallback/config synchronization while consolidating split modifier ownership (Prompts 3-6).
- [x] Prompt 7: physical Satchel presets with six filtered autosaving slots, user names, atomic partial equip, current-equipment icons, and canonical specialization labels. Preserve catch storage/protection and migrate old references. Validated by 326 unit tests and 70 core GameTests.
- [ ] Human in-client Satchel validation: click/drag/shift-click, create/rename/delete/equip feedback, long names/tooltips, GUI scales, both hand arrangements, bait-capacity refusal, and Storage / Upgrades navigation.
- [x] Review seven complete archetype loadouts, capped canonical values, runtime/Satchel agreement and specimen authority; remove unused scalar hook-weight helpers and bait alias. Validated by 333 unit tests, build and 71 core GameTests. See the spec's current convergence table.
- [x] Complete reproducible statistical measurements for seven builds, slot comparisons, target weights, traits, protection, clamps and conditional throughput. See POST_2_0_GEAR_BALANCE_REPORT.md; measurement completion is not all-seven balance approval.
- [x] Complete final architecture/authority/persistence convergence review and unused bobber lookup cleanup; 336 unit tests, build, 71 core GameTests and the explicit balance report pass.
- [ ] Full balance sign-off: resolve the boss roster, generic Leviathan utility, routine insurance/cap saturation, Shark Tooth opportunity cost, native bait-slot/acquisition cost and measured player-dependent success/throughput. Preserve frozen values until a supported tuning decision.
- [ ] Complete POST_2_0_GEAR_MANUAL_CHECKLIST.md in Minecraft.
- [ ] Human fishing validation: compare Diamond/Netherite and each archetype's fight feel, success rate and actual throughput. Run installed-optional-mod matrices at the relevant compatibility/release gate.

## Post-2.0 architecture goals

- [x] Create a small canonical Tideborne fishing API layer for specimen reads, stored FishScore reads, gear modifier queries, species profiles, and record reads. Boundary documented in `docs/TIDEBORNE_INTERNAL_API.md`.
- [x] Create one canonical specimen presentation layer for trait names/order, colors, length/percentile formatting, FishScore formatting, and rarity stars, then migrate covered UI consumers without rebuilding trait state per screen.
- [x] Inventory all active Tide-targeting mixins, separate Tide coupling from vanilla/Tideborne-owned/optional compatibility hooks, classify fragility, and document the genuinely version-sensitive remainder in `docs/TIDE_MIXIN_INVENTORY.md`.
- [x] Replace or consolidate avoidable Tide mixins using stable Tideborne-owned/shared integration paths where behavior can be preserved. Journal specimen sync now uses a Tideborne-owned payload, Team Journal catch bookkeeping is centralized in `TeamJournalCatchBridge`, and the remaining Tide hooks are explicitly version-sensitive adapters.
- [x] Remove high-confidence dead legacy implementation while preserving required migration reads, remove the obsolete FishScore self-mixin and inactive Satchel mixin source, then unify active Java ownership under `com.redslovesgames.tideborne` while preserving serialized compatibility identities. See `docs/ARCHITECTURE.md`.
- [x] Complete P1-P8 ownership consolidation and P9 final audit: unified Tideborne entrypoints/config, explicit canonical-vs-legacy specimen boundaries, feature-owned fishing gear/Tide/ecosystem runtime, P7 resource aliases, P8 historical ID owners, consolidated Satchel ownership, and final architecture regression evidence. See `.github/TIDEBORNE_P9_AUDIT_LEDGER.md`.

The architecture sequence is now complete. Future cleanup should follow the unified Tideborne feature ownership established by the internal API, canonical presentation layer, mixin inventory, shared integration services, and P9 audit ledger. Serialized namespace/resource migration remains separate work and is not implied by Java package ownership.

- [x] Finish behavior-preserving cleanup B01-B15 in [CODE_CLEANUP_QUEUE.md](CODE_CLEANUP_QUEUE.md): direct canonical TeamProgress ownership, shared physical record tolerance, immutable gear contribution reuse, touched-code readability and contract tests. Final Java 21 build and 78 core GameTests pass; manual Minecraft visual/multiplayer checks remain the documented handoff.

## Release versioning

- [x] Stop publishing or refreshing a GitHub Release on normal `dev` pushes.
- [x] Advance `dev` to semantic version `2.0.1` so the frozen 2.0.0 artifact is no longer reused by development builds.
- [x] Require exact `MAJOR.MINOR.PATCH` versions in `gradle.properties`.
- [x] Publish only from an explicit numeric semantic-version tag matching `mod_version` exactly.
- [x] Build and validate the release artifact from the tagged commit itself.
- [x] Refuse to modify, retarget, upload over, or `--clobber` an existing GitHub Release.
- [x] Record the release commit SHA and artifact SHA-256 in release metadata.
- [x] Keep normal `dev`, `main`, reconstruction, pull-request, and manual-dispatch runs CI-only.
- [ ] Enable GitHub repository-level Immutable Releases for server-enforced protection against manual asset replacement or tag movement. The workflow already refuses replacement; this GitHub repository setting adds platform-level enforcement.

## Remaining non-Fishing-System-2.0 work

- [ ] Decide and document the long-term source-distribution license. The repository root currently uses MIT while historical packaged metadata contains an All Rights Reserved declaration.
- [ ] Handle future Minecraft, Fabric, Tide, or optional-mod version upgrades as separate compatibility work.

- [x] Prompt 4: native hooks and normal bait preserved; canonical specialty/warm/deep targeting and native bait luck attribution validated by build and 61 core GameTests.

- [x] Prompt 5: all 32 bobbers consolidated into one per-cast server contribution; default/config migration, third-party fallback, display synchronization, and single application validated.

- [x] Prompt 6 mechanics/persistence committed in 4cdfd74; the unresolved default boss roster is tracked above.
